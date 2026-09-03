# LibraCore — 1.1.2 Engineering Handoff

**Audit/update date:** 2026-09-03  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Published stable release:** `v1.1.1`  
**Active release target:** `v1.1.2`  
**Commit identity used for project commits:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. The published `v1.0.0`, `v1.1.0`, and `v1.1.1` history is preserved and must not be rewritten or force-moved.

## v1.1.1 publication record

`v1.1.1` is now a published stable GitHub release. Its release tag targets commit `dc55695f78fff89ac56c2d2ff8549673619a9d6f`, and the tag-scoped release workflow was started against that exact source.

The repository release record is published at the GitHub release page for `v1.1.1`. The release body uses the repository-managed `docs/release-notes/v1.1.1.md` content.

The previous handoff incorrectly described `v1.1.1` as unpublished after the release had already been created. This continuation corrects that documentation state rather than rewriting the published release.

## v1.1.2 work completed in this pass

The next maintenance line has been opened with focused, reviewable commits:

- `2a95babe` — `chore(release): advance backend to 1.1.2`
- `936088ad` — `chore(release): advance frontend to 1.1.2`
- `f3042ec0` — `docs(readme): advance release line to 1.1.2`
- `b56f09ea` — `docs(changelog): open 1.1.2 maintenance line`
- `0b8d0cc6` — `docs(release): add v1.1.2 release notes`
- `155ecd04` — `docs(roadmap): open 1.1.2 maintenance closure`
- `05aa0301` — `docs(release): move publication procedure to 1.1.2`
- this handoff update records the new maintenance target and the corrected `v1.1.1` publication state.

All project commits use the requested identity `Sanskar <sanskarin@outlook.in>`.

## Manifest and lockfile state

The executable manifests are being advanced to `1.1.2`:

- `backend/pom.xml` → `1.1.2`;
- `frontend/package.json` → `1.1.2`;
- `frontend/package-lock.json` → must be regenerated/synchronized to `1.1.2` by the repository workflow before tagging.

`scripts/check-version.mjs` remains the executable guard for all three version-bearing manifests.

The lockfile must not be hand-synthesized. The supported npm synchronization workflow should generate and commit the exact lockfile that belongs with the `1.1.2` frontend manifest.

## Current release sequence

The intended stable sequence is:

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.2.0 → ...
```

The old `2.0.12` and temporary `0.1.1` preparation lines remain historical engineering work and are not active release targets.

## Release workflow

`.github/workflows/release.yml` is tag-scoped and fail-closed. It checks for the committed lockfile, validates the tag against backend/frontend/lockfile versions, verifies the packaged backend with PostgreSQL and `/actuator/health`, installs frontend dependencies with `npm ci`, runs the frontend quality gate, builds release artifacts, creates SHA-256 checksums, and publishes the prepared release notes for the tagged version.

## Recovery and reliability

An automated disposable PostgreSQL Recovery Drill exists at `.github/workflows/recovery-drill.yml`. It exercises the actual backup/restore scripts, verifies migration history and marker data, then starts the packaged backend against the restored database.

The repository also retains PostgreSQL readiness and packaged-backend startup diagnostics introduced after an earlier recovery-drill failure.

## Current verification status

Recent changes have triggered fresh CI runs. They must be allowed to conclude before `v1.1.2` is tagged.

At the time of this handoff:

- CodeQL for the `1.1.1` final source completed successfully.
- The `v1.1.1` tag-scoped Release workflow was still in progress while this handoff was being written; its packaged backend health check had passed startup but the health verification step was still running.
- New `1.1.2` main-branch CI runs are queued/in progress because the manifests and release documentation have just changed.
- No claim of complete `v1.1.2` readiness should be made until the final source has current successful release-blocking evidence.

## Remaining v1.1.2 release gates

1. confirm the `1.1.2` frontend lockfile synchronization commit is present on `main`;
2. run `node scripts/check-version.mjs 1.1.2` against the final checkout;
3. pass current Backend CI;
4. pass current Frontend CI;
5. pass Version Sync;
6. pass CodeQL;
7. pass Dependency Review/security checks;
8. pass the current Recovery Drill on the intended release source;
9. complete role-based browser smoke journeys;
10. complete the documented manual accessibility review;
11. review repository links/configuration and release artifacts;
12. confirm no secrets or private data are tracked;
13. confirm `main` branch-protection/rules status and document any host-level limitation;
14. identify the exact final verified commit;
15. create `v1.1.2` only from that exact commit;
16. confirm the tag-scoped release workflow succeeds and generated artifacts/checksums are valid.

## Local verification commands

From the repository root:

```bash
node scripts/check-version.mjs 1.1.2
```

Backend:

```bash
cd backend
mvn clean verify
```

Frontend:

```bash
cd frontend
npm ci --ignore-scripts --no-audit --no-fund
npm run check
```

## Tagging rule

Do not create `v1.1.2` until the exact final source has passed all release-blocking gates.

```bash
git checkout main
git pull --ff-only
node scripts/check-version.mjs 1.1.2
git tag -a v1.1.2 -m "LibraCore v1.1.2"
git push origin v1.1.2
```

Do not force-move `v1.0.0`, `v1.1.0`, or `v1.1.1`.

## Next engineering priorities after v1.1.2

After release closure, continue with meaningful product/operational work rather than version-only commits:

- operational observability and diagnostics;
- representative performance/load measurement;
- deeper branch-level workflows and reporting;
- deployment/environment validation;
- accessibility and internationalization improvements;
- further interoperability and library integrations.
