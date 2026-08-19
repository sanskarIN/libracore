# Troubleshooting

## Backend cannot connect to PostgreSQL

Check Compose/service health and effective database environment variables:

```bash
docker compose -f compose.yml ps
```

Confirm the database, user, password, host/port, and JDBC URL match. Do not paste real passwords into public issues.

## Flyway migration fails

Read the first migration error, not only the final Spring startup failure. Verify the database was created from a supported prior schema and that historic migration files were not edited after being applied. For disposable local data, recreate the volume; for valuable data, stop and follow an approved recovery/migration procedure.

## Backend health endpoint is unavailable

Run:

```bash
cd backend
mvn clean verify
mvn spring-boot:run
```

Then check `http://localhost:8080/actuator/health`. A port conflict, database failure, invalid environment value, or Java version mismatch commonly prevents startup.

## Login fails

- Confirm an application account exists and is enabled.
- Confirm the configured bootstrap admin actually ran only if you intentionally enabled it.
- Verify email spelling and account role/status.
- Clear browser `sessionStorage` if an old/corrupted session is suspected.
- Inspect sanitized server logs without exposing credentials or bearer tokens.

## Browser reports network error

Verify the backend origin/API base and CORS configuration. The frontend default is `http://localhost:8080/api`; `VITE_API_BASE_URL` is compiled into the frontend build. Ensure the browser origin is included in `APP_CORS_ALLOWED_ORIGINS`.

## `npm ci` fails

Confirm `frontend/package-lock.json` exists and Node/npm versions satisfy `frontend/package.json`. If `package.json` changed without regenerating the lockfile, update the lock in a controlled dependency-maintenance change and commit both together.

## TypeScript/lint/build errors

Run each stage separately to isolate the failure:

```bash
cd frontend
npm ci
npm run lint
npm run typecheck
npm run test:run
npm run build
```

Fix the first deterministic error before suppressing diagnostics. Strict TypeScript options are intentional.

## Copy lookup fails

The circulation desk accepts the supported accession/barcode/QR lookup contract. Confirm the physical copy exists and the entered/scanned value matches stored metadata exactly after trimming expected scanner suffixes.

## Issue/renew/return is rejected

Check member status/expiry, copy state, active circulation policy, renewal count, reservations/holds, and current loan state. Server-side business rules are authoritative even if the UI previously displayed an action.

## CSV import fails

Check UTF-8 encoding, required headers, row limits, required values, UUID/code formats, duplicates, and authorization. Do not bypass validation by editing the database directly.

## Notification email is not sent

Development commonly uses mock notification mode. If SMTP mode is intended, verify host/port/TLS/auth/sender settings privately. Never post SMTP passwords in issues.

## Backup/restore questions

Follow [`backup-restore.md`](backup-restore.md). Always test restoration into an isolated database first.
