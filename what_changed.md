# LibraCore — 1.1.1 Engineering Handoff

**Audit/update date:** 2026-09-03  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Active release line:** `1.1.1`  
**Commit identity used for project commits:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. The next release target is **`v1.1.1`**. The existing `v1.0.0` and `v1.1.0` history is preserved and must not be rewritten or force-moved.

## Current release state

The active release target is **`v1.1.1`**.

Completed in this release-preparation pass:

- backend Maven version advanced to `1.1.1`;
- frontend npm version advanced to `1.1.1`;
- frontend lockfile synchronization is automated through npm on `main`;
- release version validation now checks backend, frontend, and frontend lockfile root metadata;
- repository-managed release notes were added at `docs/release-notes/v1.1.1.md`;
- release automation now publishes the prepared `v1.1.1` release notes instead of generating an unrelated automatic description;
- README, changelog, roadmap, and release procedure were moved to the `1.1.1` release line;
- the temporary `0.1.1` preparation remains historical work and is not a release target.

## Release commits from this pass

The release correction was intentionally split into reviewable commits:

- `59f1d13d` — `chore(release): advance backend to 1.1.1`
- `cdb6fd5c` — `chore(release): advance frontend to 1.1.1`
- `e3b7fc4a` — `ci(release): validate frontend lockfile version`
- `1e34522b` — `docs(release): add v1.1.1 release notes`
- `96b1a8cd` — `ci(release): publish prepared v1.1.1 notes`
- `b91a0e0a` — `docs(release): move roadmap to 1.1.1`
- `e88f2942` — `docs(changelog): prepare 1.1.1 release`
- `4ef93f9b` — `docs(release): document 1.1.1 publication flow`
- `8efdb3db` — `docs(release): mark 1.1.1 release candidate`
- this handoff update records the current release evidence and remaining gates.

All project commits use the requested identity `Sanskar <sanskarin@outlook.in>`.

## Manifest and lockfile state

The executable manifests are intended to be aligned to `1.1.1`:

- `backend/pom.xml` → `1.1.1`;
- `frontend/package.json` → `1.1.1`;
- `frontend/package-lock.json` → must be generated/synchronized to `1.1.1` by the repository workflow before tagging.

`scripts/check-version.mjs` is now the executable guard for all three version-bearing manifests.

The frontend lockfile workflow has already completed successfully for an earlier manifest update. The new `1.1.1` frontend update has triggered a fresh synchronization run; its resulting commit must be confirmed before the release tag is created.

## Release workflow

`.github/workflows/release.yml` is tag-scoped and fail-closed. It checks for the committed lockfile, validates the tag against backend/frontend/lockfile versions, verifies the packaged backend with PostgreSQL and `/actuator/health`, installs frontend dependencies with `npm ci`, runs the frontend quality gate, packages the backend and frontend, creates SHA-256 checksums, and publishes the repository-managed `docs/release-notes/v1.1.1.md`.

The previous `v1.1.0` release attempt failed at version validation because the tagged source did not match the requested tag. That failure is historical evidence that the guard works, not evidence of `v1.1.1` readiness.

## Recovery Drill

An automated disposable PostgreSQL Recovery Drill exists at `.github/workflows/recovery-drill.yml`. It exercises the actual backup/restore scripts, verifies migration history and marker data, then starts the packaged backend against the restored database.

A prior drill failed during source-database migration, after which PostgreSQL readiness and backend startup diagnostics were hardened. A current successful Recovery Drill on the exact final release source remains required before claiming release readiness.

## Current CI evidence

At the beginning of this continuation, the repository's lockfile synchronization workflow had a successful earlier run, while the old `v1.1.0` tag-triggered Release run failed during version validation. The new `1.1.1` source changes have started fresh CI runs.

Do not claim Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, or Recovery Drill success until their current runs conclude successfully for the final release source.

## Product implementation already present

LibraCore contains the intended end-to-end library-management implementation across:

- Spring Boot modular-monolith backend;
- PostgreSQL persistence and Flyway migrations;
- catalog metadata, branches, shelves, physical copies, accession/barcode/QR lookup, and search;
- members and account linkage;
- administrator/librarian/member authentication and authorization;
- staff-account lifecycle management;
- issue, return, renewal, reservations/waitlists;
- circulation policy and fine assessment/settlement;
- dashboard, overdue reporting, and audit search;
- bounded CSV import/export;
- notification scheduling and mock/SMTP gateways;
- responsive React/TypeScript UI with role-aware navigation;
- light/dark/system themes;
- backup/restore helpers and automated recovery verification;
- security/privacy/threat-model/governance documentation;
- backend/frontend/version/security/dependency/recovery/release automation.

## Remaining release gates

Before tagging `v1.1.1`, complete and directly verify:

1. confirm the `1.1.1` frontend lockfile synchronization commit is present on `main`;
2. run `node scripts/check-version.mjs 1.1.1` against the final checkout;
3. pass current Backend CI;
4. pass current Frontend CI;
5. pass Version Sync;
6. pass CodeQL;
7. pass Dependency Review;
8. pass the current Recovery Drill on the intended release source;
9. complete role-based browser smoke journeys;
10. complete the documented manual accessibility review;
11. review repository links/configuration and release artifacts;
12. confirm no secrets or private data are tracked;
13. confirm `main` branch-protection/code-owner enforcement status and document the host-level limitation if it remains disabled;
14. create `v1.1.1` only from the exact final verified commit.

## Tagging rule

The intended release tag is:

```text
v1.1.1
```

Create it only from the exact final verified release source:

```bash
git checkout main
git pull --ff-only
node scripts/check-version.mjs 1.1.1
git tag -a v1.1.1 -m "LibraCore v1.1.1"
git push origin v1.1.1
```

Do not create the tag early. Do not force-move a published release tag. If a pre-publication error is found, fix the source and create a new release version rather than moving a published tag.

## Planned v1.1.1 release contents

The release should describe the mature library-management functionality already present, together with the release-engineering improvements in this pass: synchronized version metadata, lockfile validation, prepared release notes, fail-closed release automation, PostgreSQL-backed packaged-service verification, frontend reproducibility, artifact checksums, and recovery/security automation.

## Release publication status

**`v1.1.1` is not yet published.**

The repository is in release-candidate preparation. Publication is allowed only after the final source passes the release-blocking gates and the tag-triggered release workflow succeeds.

## Continuation rule

The next work session should continue from this file using `v1.1.1` as the sole active release target. Prioritize confirmation of lockfile synchronization, current CI/security/recovery evidence, final-source consistency, and release publication. Do not reopen the superseded `0.1.1` plan or rewrite the existing `v1.0.0`/`v1.1.0` history.
