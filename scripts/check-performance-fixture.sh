#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "Performance fixture check failed: $*" >&2
  exit 1
}

[[ -n "${PERF_DATABASE_URL:-}" ]] || fail "PERF_DATABASE_URL is required."
command -v psql >/dev/null 2>&1 || fail "psql is required."

expected_books="${PERF_BOOKS:-5000}"
expected_members="${PERF_MEMBERS:-5000}"
expected_copies_per_book="${PERF_COPIES_PER_BOOK:-2}"
expected_loans="${PERF_LOANS:-4000}"
expected_reservations="${PERF_RESERVATIONS:-1000}"
expected_copies=$(( expected_books * expected_copies_per_book ))

psql_base=(psql -X --no-psqlrc --set=ON_ERROR_STOP=1 "${PERF_DATABASE_URL}")
scalar() {
  "${psql_base[@]}" --tuples-only --no-align --command="$1"
}

assert_count() {
  local label="$1"
  local sql="$2"
  local expected="$3"
  local actual
  actual="$(scalar "${sql}")"
  [[ "${actual}" == "${expected}" ]] || fail "${label}: expected ${expected}, got ${actual}."
}

assert_zero() {
  assert_count "$1" "$2" 0
}

assert_count "fixture books" "SELECT count(*) FROM book WHERE id::text LIKE 'f5f5f5f5-%';" "${expected_books}"
assert_count "fixture copies" "SELECT count(*) FROM book_copy WHERE id::text LIKE 'f6f6f6f6-%';" "${expected_copies}"
assert_count "fixture members" "SELECT count(*) FROM member WHERE id::text LIKE 'f7f7f7f7-%';" "${expected_members}"
assert_count "fixture loans" "SELECT count(*) FROM loan WHERE id::text LIKE 'f9f9f9f9-%';" "${expected_loans}"
assert_count "fixture reservations" "SELECT count(*) FROM reservation WHERE id::text LIKE 'fafafafa-%';" "${expected_reservations}"
assert_count "fixture branch" "SELECT count(*) FROM branch WHERE id = 'f0f0f0f0-0000-0000-0000-000000000001' AND code = 'PERF';" 1
assert_count "fixture policy" "SELECT count(*) FROM fine_rule WHERE id = 'fcfcfcfc-0000-0000-0000-000000000001' AND branch_id = 'f0f0f0f0-0000-0000-0000-000000000001' AND active = TRUE;" 1
assert_count "disabled fixture staff identity" "SELECT count(*) FROM app_user WHERE id = 'f8f8f8f8-0000-0000-0000-000000000001' AND enabled = FALSE;" 1

assert_zero "enabled fixture accounts" "SELECT count(*) FROM app_user WHERE id::text LIKE 'f8f8f8f8-%' AND enabled = TRUE;"
assert_zero "non-fictional fixture emails" "SELECT count(*) FROM member WHERE id::text LIKE 'f7f7f7f7-%' AND email NOT LIKE '%@example.invalid';"
assert_zero "open loans whose copies are not ON_LOAN" "SELECT count(*) FROM loan l JOIN book_copy c ON c.id = l.copy_id WHERE l.id::text LIKE 'f9f9f9f9-%' AND l.status = 'OPEN' AND c.status <> 'ON_LOAN';"
assert_zero "available copies with open fixture loans" "SELECT count(*) FROM book_copy c JOIN loan l ON l.copy_id = c.id WHERE l.id::text LIKE 'f9f9f9f9-%' AND l.status = 'OPEN' AND c.status = 'AVAILABLE';"
assert_zero "fixture loans using a non-fixture circulation policy" "SELECT count(*) FROM loan WHERE id::text LIKE 'f9f9f9f9-%' AND fine_rule_id <> 'fcfcfcfc-0000-0000-0000-000000000001';"
assert_zero "duplicate active fixture reservation pairs" "SELECT count(*) FROM (SELECT book_id, member_id, count(*) FROM reservation WHERE id::text LIKE 'fafafafa-%' AND status IN ('WAITING','READY') GROUP BY book_id, member_id HAVING count(*) > 1) duplicates;"

open_loans="$(scalar "SELECT count(*) FROM loan WHERE id::text LIKE 'f9f9f9f9-%' AND status = 'OPEN';")"
returned_loans="$(scalar "SELECT count(*) FROM loan WHERE id::text LIKE 'f9f9f9f9-%' AND status = 'RETURNED';")"
fines="$(scalar "SELECT count(*) FROM fine_charge WHERE id::text LIKE 'fbfbfbfb-%';")"

echo "Performance fixture checks passed."
echo "books=${expected_books} copies=${expected_copies} members=${expected_members} loans=${expected_loans} (open=${open_loans}, returned=${returned_loans}) reservations=${expected_reservations} fines=${fines}"
