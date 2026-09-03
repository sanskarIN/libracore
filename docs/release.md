# Release Process

LibraCore does not call a release ready merely because source files exist. A release requires reproducible verification evidence. The current source manifests are prepared for **1.1.1**, and the npm-generated `frontend/package-lock.json` remains committed.

## Pre-release checklist

1. Start from a clean checkout of the intended commit.
2. Review `CHANGELOG.md`, `ROADMAP.md`, and `what_changed.md`.
3. Confirm no real secrets/private data are tracked.
4. Verify release-version synchronization from the repository root:
   ```bash
   node scripts/check-version.mjs 1.1.1
   ```
5. Verify backend:
   ```bash
   cd backend
   mvn clean verify
   ```
6. Confirm `frontend/package-lock.json` is present and generated from the same frontend manifest. Never hand-edit or synthesize the lockfile.
7. Verify frontend reproducibly:
   ```bash
   cd frontend
   npm ci --ignore-scripts --no-audit --no-fund
   npm run check
   ```
8. Start PostgreSQL from an empty disposable volume and start the **packaged backend JAR**, not only the application from source; confirm all Flyway migrations and `/actuator/health`.
9. Perform the primary-role smoke journeys documented in `docs/testing.md`.
10. Perform the manual accessibility checks in `docs/accessibility.md`.
11. Run dependency/static-security automation and review failures.
12. Perform or review a current backup/restore drill.
13. Check documentation links/configuration against the actual tree.
14. Confirm the intended tag exactly matches frontend, backend, and lockfile versions. For this release the tag is `v1.1.1`.
15. Do not cut a stable release while required verification remains unobserved or a release-blocking limitation remains open in `what_changed.md`.

## Versioning

LibraCore uses Semantic Versioning for release identifiers. The backend Maven project version, frontend npm package version, and frontend lockfile root version must match. `scripts/check-version.mjs` is the executable guard for that invariant, and `.github/workflows/version-sync.yml` enforces manifest synchronization on relevant changes.

## Tagging

After the release commit passes every gate:

```bash
git tag -a v1.1.1 -m "LibraCore v1.1.1"
git push origin v1.1.1
```

The release workflow verifies that the pushed tag and all three version-bearing frontend/backend manifests represent the same version. Do not move a published release tag. Correct mistakes with a new version.

## Release workflow guarantees

`.github/workflows/release.yml` intentionally refuses to publish when the frontend lockfile is absent or inconsistent. It also:

- validates the release tag against frontend, backend, and lockfile versions before expensive build work;
- runs backend Maven verification against PostgreSQL;
- starts the packaged backend JAR produced by the build;
- verifies the packaged service reaches `/actuator/health` successfully against PostgreSQL before publication;
- installs frontend dependencies through reproducible `npm ci` with lifecycle scripts disabled;
- runs the aggregate frontend quality gate;
- discovers the packaged backend JAR without a hard-coded historical version filename;
- packages the frontend production build;
- generates SHA-256 checksums;
- publishes the repository-managed `docs/release-notes/v1.1.1.md` as the release description;
- always attempts to stop the temporary packaged backend process and prints its log when the release job fails.

The release checkout does not persist Git credentials after checkout. Read-only verification workflows follow the same least-privilege pattern; only the explicit lockfile synchronization workflow retains credentials because it may need to push a generated lockfile to `main`.

## CI freshness

Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and the lockfile synchronization workflow use workflow concurrency so superseded runs do not consume runner capacity or report stale results as the newest verification. Release publication itself remains tag-scoped and is never silently substituted by a branch build.

## Artifacts

A release may include the backend JAR, frontend production bundle/archive, checksums, and source archives. Artifacts must be produced from the tagged commit by the release workflow or another documented reproducible process.

The lockfile synchronization workflow's generated lockfile is source metadata, **not** a release artifact. The canonical dependency state remains the reviewed `frontend/package-lock.json` committed to the release source tree.

## Migration and rollback

Before deployment, inspect migrations introduced since the currently deployed version and assess lock/downtime/data implications. Application rollback is not equivalent to database rollback: a newer schema may not be compatible with an older binary. Prefer forward-fix migrations and tested backup recovery over destructive ad-hoc SQL.
