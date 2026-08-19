# Release Process

LibraCore does not call a release ready merely because source files exist. A release requires reproducible verification evidence.

## Pre-release checklist

1. Start from a clean checkout of the intended commit.
2. Review `CHANGELOG.md`, `ROADMAP.md`, and `what_changed.md`.
3. Confirm no real secrets/private data are tracked.
4. Verify backend:
   ```bash
   cd backend
   mvn clean verify
   ```
5. Verify frontend:
   ```bash
   cd frontend
   npm ci
   npm run lint
   npm run typecheck
   npm run test:run
   npm run build
   ```
6. Start PostgreSQL from an empty disposable volume and start the packaged backend; confirm Flyway and `/actuator/health`.
7. Perform the primary-role smoke journeys documented in `docs/testing.md`.
8. Perform the manual accessibility checks in `docs/accessibility.md`.
9. Run dependency/static-security automation and review failures.
10. Perform or review a current backup/restore drill.
11. Check documentation links/configuration against the actual tree.
12. Confirm version numbers and release notes are consistent.

## Versioning

Use Semantic Versioning once stable public contracts begin. During `0.x`, minor versions may include substantial changes, but migrations and release notes must still make upgrades explicit.

## Tagging

After the release commit passes gates:

```bash
git tag -a vX.Y.Z -m "LibraCore vX.Y.Z"
git push origin vX.Y.Z
```

Do not move a published release tag. Correct mistakes with a new version.

## Artifacts

A release may include the backend JAR, frontend production bundle/archive, checksums, and source archives. Artifacts must be produced from the tagged commit by the release workflow or another documented reproducible process.

## Migration and rollback

Before deployment, inspect migrations introduced since the currently deployed version and assess lock/downtime/data implications. Application rollback is not equivalent to database rollback: a newer schema may not be compatible with an older binary. Prefer forward-fix migrations and tested backup recovery over destructive ad-hoc SQL.

## Release notes

Include user-visible features/fixes, security-relevant changes, migration/configuration changes, known limitations, upgrade steps, and verification summary. Never claim a check passed if it was not actually run.
