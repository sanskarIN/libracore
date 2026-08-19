#!/usr/bin/env bash
set -euo pipefail

umask 077

: "${PGDATABASE:?Set PGDATABASE to the LibraCore database name}"
: "${PGUSER:?Set PGUSER to a database role with backup access}"

if ! command -v pg_dump >/dev/null 2>&1; then
  echo "pg_dump is required but was not found on PATH." >&2
  exit 1
fi

backup_dir="${LIBRACORE_BACKUP_DIR:-backups}"
mkdir -p "$backup_dir"

timestamp="$(date -u +'%Y%m%dT%H%M%SZ')"
output="${1:-${backup_dir}/libracore-${timestamp}.dump}"

if [[ -e "$output" ]]; then
  echo "Refusing to overwrite existing backup: $output" >&2
  exit 1
fi

pg_dump \
  --format=custom \
  --no-owner \
  --no-acl \
  --file "$output" \
  "$PGDATABASE"

if [[ ! -s "$output" ]]; then
  echo "Backup file was not created correctly." >&2
  exit 1
fi

sha256sum "$output" > "${output}.sha256"
chmod 600 "$output" "${output}.sha256"

echo "Backup created: $output"
echo "Checksum: ${output}.sha256"
