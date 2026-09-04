#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "Performance threshold check failed: $*" >&2
  exit 1
}

[[ -n "${PERF_DATABASE_URL:-}" ]] || fail "PERF_DATABASE_URL is required."
[[ "${PERF_DATABASE_URL}" == *_perf || "${PERF_DATABASE_URL}" == *_benchmark ]] || fail "PERF_DATABASE_URL must target a database ending in _perf or _benchmark."
command -v psql >/dev/null 2>&1 || fail "psql is required."

max_ms="${PERF_MAX_QUERY_MS:-250}"
[[ "${max_ms}" =~ ^[0-9]+$ ]] || fail "PERF_MAX_QUERY_MS must be an integer."

psql_base=(psql -X --no-psqlrc --set=ON_ERROR_STOP=1 "${PERF_DATABASE_URL}")

measure() {
  local label="$1"
  local sql="$2"
  local output
  output="$("${psql_base[@]}" --tuples-only --no-align --command="EXPLAIN (ANALYZE, FORMAT JSON) ${sql}")"
  local actual_ms
  actual_ms="$(printf '%s\n' "${output}" | python3 -c 'import json,sys; print(round(json.load(sys.stdin)[0]["Execution Time"], 2))')"
  python3 - "$actual_ms" "$max_ms" "$label" <<'PY'
import sys
actual = float(sys.argv[1])
maximum = float(sys.argv[2])
label = sys.argv[3]
if actual > maximum:
    print(f"{label}: {actual} ms > {maximum} ms", file=sys.stderr)
    raise SystemExit(1)
print(f"{label}: {actual} ms <= {maximum} ms")
PY
}

measure "catalog lookup" "SELECT b.id, b.title, b.isbn13, b.publication_year FROM book b ORDER BY b.title, b.id LIMIT 25"
measure "member circulation lookup" "SELECT m.id, m.library_card_number, l.id, l.status, l.due_at FROM member m JOIN loan l ON l.member_id = m.id WHERE m.id::text LIKE 'f7f7f7f7-%' ORDER BY l.due_at DESC LIMIT 25"
measure "reservation queue lookup" "SELECT r.id, r.book_id, r.member_id, r.status, r.requested_at FROM reservation r WHERE r.book_id::text LIKE 'f5f5f5f5-%' AND r.status IN ('WAITING','READY') ORDER BY r.requested_at LIMIT 25"

echo "Performance thresholds passed (max query time: ${max_ms} ms)."