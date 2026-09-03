# LibraCore — 1.1.3 Engineering Handoff

**Audit/update date:** 2026-09-03  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Published stable release:** `v1.1.2`  
**Active release target:** `v1.1.3`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published `v1.0.0`, `v1.1.0`, `v1.1.1`, and `v1.1.2` history must not be rewritten or force-moved.

## v1.1.2 publication record

`v1.1.2` is a published stable GitHub release. The release record is present on GitHub and its published release body is based on the repository-managed `docs/release-notes/v1.1.2.md`. Release existence and successful completion of every release workflow job are separate facts and must be verified independently.

## v1.1.3 work completed in this pass

Focused, reviewable commits have opened the next maintenance line:

- `5a3efbd2ea390a91203cad9b1fd5d32ad77e3345` — `chore(release): advance backend to 1.1.3`
- `05069a0932143960dcdeebcfe37603be815044ac` — `chore(release): advance frontend to 1.1.3`
- `4ec1653346f352771550e031f738134d7f181402` — `docs(release): add v1.1.3 release notes`
- `3a995409a901a160531b3e3aa7c5ccafe3474103` — `docs(changelog): open 1.1.3 maintenance line`
- `5717a688db8dcda103859e1e8559c0cf4c237d24` — `docs(roadmap): open 1.1.3 maintenance closure`
- `405edabf1f095a5c2ee5d4683b2a40cdb5eda623` — `docs(release): move publication procedure to 1.1.3`
- this handoff commit records the current release state and exact next gates.

All commits use `Sanskar <sanskarin@outlook.in>`.

## Manifest and lockfile state

The executable manifests are aligned to `1.1.3`:

- `backend/pom.xml` → `1.1.3`;
- `frontend/package.json` → `1.1.3`;
- `frontend/package-lock.json` → must be regenerated/synchronized to `1.1.3` by the supported npm workflow before tagging.

The lockfile must not be hand-synthesized. The repository's lockfile synchronization workflow should generate and commit the exact dependency metadata for the `1.1.3` frontend manifest.

`scripts/check-version.mjs` is the executable guard for all three version-bearing manifests.

## Current release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.2.0 → ...
```

The old `2.0.12` and temporary `0.1.1` preparation lines remain historical engineering work and are not active release targets.

## Release workflow

`.github/workflows/release.yml` is tag-scoped and fail-closed. It checks the committed lockfile, validates the tag against backend/frontend/lockfile versions, verifies the packaged backend with PostgreSQL and `/actuator/health`, installs frontend dependencies with `npm ci`, runs frontend quality checks, builds release artifacts, creates SHA-256 checksums, and publishes the prepared release notes for the tagged version.

## Recovery and reliability

The automated disposable PostgreSQL Recovery Drill remains part of the release gate. It exercises the actual backup/restore scripts, verifies migration history and marker data, then starts the packaged backend against the restored database.

## Current verification status

The `1.1.3` version/documentation commits have triggered fresh CI. They must be allowed to conclude before tagging.

At this handoff:

- `v1.1.2` is published.
- `1.1.3` backend and frontend manifests are updated.
- `frontend/package-lock.json` still needs supported-toolchain synchronization to `1.1.3` if the current synchronization workflow has not yet completed.
- Current main-branch release readiness is **not yet established**.
- No `v1.1.3` tag should be created until the exact final source has successful release-blocking evidence.

## Remaining v1.1.3 release gates

1. confirm the `1.1.3` frontend lockfile synchronization commit is present;
2. run `node scripts/check-version.mjs 1.1.3` against the final checkout;
3. pass current Backend CI;
4. pass current Frontend CI;
5. pass Version Sync;
6. pass CodeQL;
7. pass Dependency Review/security checks;
8. pass the current Recovery Drill;
9. complete role-based browser smoke journeys;
10. complete the documented manual accessibility review;
11. review repository links/configuration and tracked secrets;
12. confirm branch-protection/rules status and document any host-level limitation;
13. identify the exact final verified commit;
14. create `v1.1.3` only from that exact commit;
15. confirm the tag-scoped release workflow succeeds;
16. review generated release artifacts and SHA-256 checksums.

## Local verification commands

From the repository root:

```bash
node scripts/check-version.mjs 1.1.3
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

Do not create `v1.1.3` until the exact final source has passed all release-blocking gates.

```bash
git checkout main
git pull --ff-only
node scripts/check-version.mjs 1.1.3
git tag -a v1.1.3 -m "LibraCore v1.1.3"
git push origin v1.1.3
```

Never force-move `v1.0.0`, `v1.1.0`, `v1.1.1`, or `v1.1.2`.

## Next engineering priorities after v1.1.3

After release closure, continue with meaningful product/operational work rather than version-only commits:

- operational observability and diagnostics;
- representative performance/load measurement;
- deeper branch-level workflows and reporting;
- deployment/environment validation;
- accessibility and internationalization improvements;
- further interoperability and library integrations.
