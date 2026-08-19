# Release Process

LibraCore does not call a release ready merely because source files exist. A release requires reproducible verification evidence. The current source manifests are prepared for **2.0.12**.

## Pre-release checklist

1. Start from a clean checkout of the intended commit.
2. Review `CHANGELOG.md`, `ROADMAP.md`, and `what_changed.md`.
3. Confirm no real secrets/private data are tracked.
4. Verify release-version synchronization from the repository root:
   ```bash
   node scripts/check-version.mjs 2.0.12
   ```
5. Verify backend:
   ```bash
   cd backend
   mvn clean verify
   ```
6. Require a committed `frontend/package-lock.json`. If it is missing, do **not** tag a release. Generate it locally with the supported Node/npm toolchain or run the repository's **Frontend Lockfile Bootstrap** workflow, review the generated lockfile, and commit it.
7. Verify frontend reproducibly:
   ```bash
   cd frontend
   npm ci --ignore-scripts
   npm run check
   ```
8. Start PostgreSQL from an empty disposable volume and start the packaged backend; confirm all Flyway migrations and `/actuator/health`.
9. Perform the primary-role smoke journeys documented in `docs/testing.md`.
10. Perform the manual accessibility checks in `docs/accessibility.md`.
11. Run dependency/static-security automation and review failures.
12. Perform or review a current backup/restore drill.
13. Check documentation links/configuration against the actual tree.
14. Confirm the intended tag exactly matches both executable manifests. For 2.0.12 the release tag is `v2.0.12`.
15. Do not cut a stable release while required verification remains unobserved or a release-blocking limitation remains open in `what_changed.md`.

## Versioning

LibraCore uses Semantic Versioning for release identifiers. The backend Maven project version and frontend npm package version must always match. `scripts/check-version.mjs` is the executable guard for that invariant, and `.github/workflows/version-sync.yml` enforces it on relevant changes.

## Tagging

After the release commit passes every gate:

```bash
git tag -a v2.0.12 -m "LibraCore v2.0.12"
git push origin v2.0.12
```

The release workflow verifies that the pushed tag, `backend/pom.xml`, and `frontend/package.json` all represent the same version. Do not move a published release tag. Correct mistakes with a new version.

## Release workflow guarantees

`.github/workflows/release.yml` intentionally refuses to publish when the frontend lockfile is absent. It also:

- runs the backend Maven verification against PostgreSQL;
- validates tag/manifest version synchronization;
- installs frontend dependencies through `npm ci`;
- runs the aggregate frontend quality gate;
- discovers the packaged backend JAR without a hard-coded historical version filename;
- packages the frontend production build;
- generates SHA-256 checksums;
- publishes artifacts from the tagged source.

## Artifacts

A release may include the backend JAR, frontend production bundle/archive, checksums, and source archives. Artifacts must be produced from the tagged commit by the release workflow or another documented reproducible process.

## Migration and rollback

Before deployment, inspect migrations introduced since the currently deployed version and assess lock/downtime/data implications. Application rollback is not equivalent to database rollback: a newer schema may not be compatible with an older binary. Prefer forward-fix migrations and tested backup recovery over destructive ad-hoc SQL.

## Release notes

Include user-visible features/fixes, security-relevant changes, migration/configuration changes, known limitations, upgrade steps, and verification summary. Never claim a check passed if it was not actually run.
