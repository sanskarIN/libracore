# Backup and Restore

Application CSV export and PostgreSQL backup serve different purposes. CSV is for scoped interoperability; database backup is for recovery.

## Backup

Review `scripts/backup.sh` before first use. Supply database connection settings through the environment rather than editing credentials into the script/repository. Store backups outside the application host when possible.

A usable backup process records at least:

- source environment/database;
- creation timestamp;
- PostgreSQL/tool version;
- schema/application version or commit;
- file size/checksum;
- encryption/storage location and retention class;
- most recent successful restore-test evidence.

Do not assume a successful `pg_dump` exit means the recovery plan is proven.

## Automated disposable Recovery Drill

`.github/workflows/recovery-drill.yml` provides a repeatable safety net for the repository backup/restore path. It is intentionally isolated from valuable data and uses fictional marker content only.

The workflow:

1. starts an ephemeral PostgreSQL 18 service;
2. packages the LibraCore backend;
3. starts that packaged JAR against a disposable source database so Flyway creates the real application schema;
4. requires `/actuator/health` to become healthy;
5. writes a fictional recovery marker into the disposable database;
6. records the Flyway migration-history count;
7. invokes the repository's actual `scripts/backup.sh` with PostgreSQL 18 client tools;
8. creates a second empty restore target;
9. invokes the actual destructive-guarded `scripts/restore.sh` with `LIBRACORE_ALLOW_RESTORE=yes` only for that disposable target;
10. verifies the backup checksum, restored migration-history count, and fictional marker value;
11. starts the same packaged backend against the restored database and requires `/actuator/health` again;
12. removes the temporary logical dump regardless of success/failure.

The workflow runs on relevant migration/recovery-script changes, can be invoked manually, and has a scheduled recurring drill. It does not upload database dumps as artifacts.

Passing this workflow demonstrates the repository's logical backup/restore mechanism and application startup against a restored schema. It does **not** replace an operator's environment-specific recovery exercise, encryption/key validation, RPO/RTO measurement, or representative production-like data checks.

## Manual restore drill

For an environment-specific drill:

1. Choose an isolated disposable PostgreSQL instance.
2. Verify target credentials/database name and ensure it is not production.
3. Record the backup checksum before restoration.
4. Run `scripts/restore.sh` using the documented environment configuration.
5. Start the matching LibraCore backend.
6. Confirm Flyway/schema health and `/actuator/health`.
7. Validate representative catalog, member, loan, reservation, fine, and audit records.
8. Record elapsed time, warnings, application version, data checks, and recovery-point limitations.
9. Destroy the temporary restored copy according to privacy policy.

## Production safeguards

- Protect backup access independently from ordinary application credentials when feasible.
- Encrypt sensitive backup storage/transport according to organizational requirements.
- Rotate and test keys/credentials needed during recovery.
- Keep retention explicit; indefinite copies create privacy and breach risk.
- Do not restore over a running production database without an approved incident/recovery procedure.
- Verify any point-in-time/log-based recovery strategy separately from logical dumps.

## Migration compatibility

Restore the backup to the schema/application version it represents, then use normal Flyway forward migrations. Never edit historic Flyway migrations to make an old backup fit a new binary.

## Recovery objectives

Operators should define Recovery Point Objective (acceptable data loss) and Recovery Time Objective (acceptable restoration duration). LibraCore cannot infer these from backup frequency alone.
