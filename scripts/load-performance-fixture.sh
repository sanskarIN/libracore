#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
fixture_sql="${repo_root}/scripts/performance-fixture.sql"

fail() {
  echo "Performance fixture refused: $*" >&2
  exit 2
}

require_integer() {
  local name="$1"
  local value="$2"
  local min="$3"
  local max="$4"
  if [[ ! "${value}" =~ ^[0-9]+$ ]]; then
    fail "${name} must be an integer between ${min} and ${max}."
  fi
  if (( value < min || value > max )); then
    fail "${name} must be between ${min} and ${max}."
  fi
}

[[ "${PERF_FIXTURE_ALLOW_WRITE:-}" == "YES" ]] || fail "set PERF_FIXTURE_ALLOW_WRITE=YES for a disposable benchmark database."
[[ -n "${PERF_DATABASE_URL:-}" ]] || fail "PERF_DATABASE_URL is required."
command -v psql >/dev/null 2>&1 || fail "psql is required."
[[ -f "${fixture_sql}" ]] || fail "missing ${fixture_sql}."

books="${PERF_BOOKS:-5000}"
members="${PERF_MEMBERS:-5000}"
copies_per_book="${PERF_COPIES_PER_BOOK:-2}"
loans="${PERF_LOANS:-4000}"
reservations="${PERF_RESERVATIONS:-1000}"

require_integer PERF_BOOKS "${books}" 1 100000
require_integer PERF_MEMBERS "${members}" 1 100000
require_integer PERF_COPIES_PER_BOOK "${copies_per_book}" 1 10
require_integer PERF_LOANS "${loans}" 0 200000
require_integer PERF_RESERVATIONS "${reservations}" 0 100000

copy_count=$(( books * copies_per_book ))
if (( copy_count > 500000 )); then
  fail "book/copy settings would create more than 500000 copies."
fi
if (( loans > copy_count )); then
  fail "PERF_LOANS cannot exceed the generated copy count (${copy_count})."
fi
if (( loans > members )); then
  fail "PERF_LOANS cannot exceed PERF_MEMBERS (${members})."
fi
if (( reservations > books )); then
  fail "PERF_RESERVATIONS cannot exceed PERF_BOOKS (${books}) because active book/member pairs must remain unique."
fi
if (( reservations > members )); then
  fail "PERF_RESERVATIONS cannot exceed PERF_MEMBERS (${members}) because active book/member pairs must remain unique."
fi

psql_base=(psql -X --no-psqlrc --set=ON_ERROR_STOP=1 "${PERF_DATABASE_URL}")

database_name="$("${psql_base[@]}" --tuples-only --no-align --command='SELECT current_database();')"
case "${database_name}" in
  *_perf|*_benchmark) ;;
  *) fail "database '${database_name}' must end in _perf or _benchmark." ;;
esac

schema_ready="$("${psql_base[@]}" --tuples-only --no-align --command="SELECT CASE WHEN to_regclass('public.book') IS NOT NULL AND to_regclass('public.member') IS NOT NULL AND to_regclass('public.loan') IS NOT NULL AND to_regclass('public.flyway_schema_history') IS NOT NULL THEN 'yes' ELSE 'no' END;")"
[[ "${schema_ready}" == "yes" ]] || fail "target database does not contain a Flyway-managed LibraCore schema. Run the packaged/current backend migrations first."

failed_migrations="$("${psql_base[@]}" --tuples-only --no-align --command='SELECT count(*) FROM flyway_schema_history WHERE success = FALSE;')"
[[ "${failed_migrations}" == "0" ]] || fail "target database contains failed Flyway migration history."

echo "Loading deterministic LibraCore performance fixture into database '${database_name}'."
echo "books=${books} copies=${copy_count} members=${members} loans=${loans} reservations=${reservations}"

"${psql_base[@]}" \
  --set=books="${books}" \
  --set=members="${members}" \
  --set=copies_per_book="${copies_per_book}" \
  --set=loans="${loans}" \
  --set=reservations="${reservations}" \
  --file="${fixture_sql}"

echo "Performance fixture loaded successfully."
