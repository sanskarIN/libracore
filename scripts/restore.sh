#!/usr/bin/env bash
set -euo pipefail

umask 077

if [[ "${LIBRACORE_ALLOW_RESTORE:-no}" != "yes" ]]; then
  echo "Restore is destructive to the target database state." >&2
  echo "Set LIBRACORE_ALLOW_RESTORE=yes after confirming the target is correct." >&2
  exit 1
fi

: "${PGDATABASE:?Set PGDATABASE to the empty target LibraCore database name}"
: "${PGUSER:?Set PGUSER to a database role with restore access}"

if ! command -v pg_restore >/dev/null 2>&1; then
  echo "pg_restore is required but was not found on PATH." >&2
  exit 1
fi

backup="${1:-}"
if [[ -z "$backup" || ! -f "$backup" ]]; then
  echo "Usage: LIBRACORE_ALLOW_RESTORE=yes $0 /path/to/libracore.dump" >&2
  exit 1
fi

if [[ -f "${backup}.sha256" ]]; then
  sha256sum --check "${backup}.sha256"
else
  echo "Warning: no checksum file found next to the backup." >&2
fi

pg_restore \
  --exit-on-error \
  --single-transaction \
  --no-owner \
  --no-acl \
  --dbname "$PGDATABASE" \
  "$backup"

echo "Restore completed into database: $PGDATABASE"
echo "Run the documented post-restore verification before reopening service traffic."
