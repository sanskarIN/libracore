# LibraCore — 1.1.4 Engineering Handoff

**Audit/update date:** 2026-09-04  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Latest observed main commit:** `32e75d2be175d1045331426ccba98e12d067676e`  
**Published stable release:** `v1.1.2`  
**Active release target:** `v1.1.4`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published release history must not be rewritten or force-moved. The attempted `v1.1.3` release workflow failure is retained as audit history.

## v1.1.4 work completed

Focused, reviewable commits have opened and extended the maintenance line:

- `ci(release): harden packaged health gate and release notes path`
- `chore(release): advance backend to 1.1.4`
- `chore(release): advance frontend to 1.1.4`
- `docs(release): add v1.1.4 release notes`
- `docs(package): add frontend repository metadata`
- `ci(lockfile): automate frontend lockfile synchronization`
- `docs(changelog): open v1.1.4 maintenance line`
- `docs(roadmap): move maintenance closure to 1.1.4`
- `docs(release): target v1.1.4 publication procedure`
- `docs(release): record v1.1.4 engineering handoff`
- `docs(release): correct v1.1.4 handoff record`
- `docs(release): remove unverified commit identifiers`
- `perf: add repeatable large-dataset fixtures`
- `docs(release): update v1.1.4 notes with performance verification`
- `docs(changelog): record v1.1.4 performance fixture closure work`

All listed release-engineering commits use `Sanskar <sanskarin@outlook.in>`.

## Latest engineering addition

The latest mainline change adds repeatable PostgreSQL performance fixtures and a dedicated CI workflow. The workflow checks shell syntax, applies current migrations, verifies an explicit write gate, rejects unsafe database names, loads the fixture, checks invariants, reloads the fixture, and checks deterministic shape again. This provides a repeatable baseline for performance/load measurement without weakening database safety boundaries.

## Manifest and lockfile state

The executable manifests are aligned to `1.1.4`:

- `backend/pom.xml` → `1.1.4`;
- `frontend/package.json` → `1.1.4`;
- `frontend/package-lock.json` → root and package-root version `1.1.4`.

`scripts/check-version.mjs` remains the executable guard for all three version-bearing manifests.

## Current release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → ...
```

The old `2.0.12` and temporary `0.1.1` preparation lines remain historical engineering work and are not active release targets.

## Current verification status

The repository contains the intended v1.1.4 source and release-preparation changes, but the available GitHub status query for the latest observed main commit returned no commit statuses/workflow runs. Therefore the release cannot honestly be marked fully verified from this handoff alone.

No `v1.1.4` tag should be created until the exact final source has successful release-blocking evidence.

## Remaining v1.1.4 release gates

1. run `node scripts/check-version.mjs 1.1.4` against the final checkout;
2. pass current Backend CI;
3. pass current Frontend CI;
4. pass Version Sync;
5. pass CodeQL;
6. pass Dependency Review/security checks;
7. pass Recovery Drill;
8. pass Performance Fixture CI;
9. complete role-based browser smoke journeys;
10. complete the documented manual accessibility review;
11. review repository links/configuration and tracked secrets;
12. confirm branch-protection/rules status and document any host-level limitation;
13. identify the exact final verified commit;
14. create `v1.1.4` only from that exact commit;
15. confirm the tag-scoped release workflow succeeds;
16. review generated release artifacts and SHA-256 checksums;
17. confirm the GitHub release is published as stable/latest without rewriting previous releases.

## Release workflow expectations

`.github/workflows/release.yml` is tag-scoped and fail-closed. It validates release versions, verifies the backend with PostgreSQL, uses the bounded packaged-health retry window, installs frontend dependencies reproducibly, runs frontend quality checks, packages artifacts, creates SHA-256 checksums, selects release notes from `GITHUB_REF_NAME`, and prints the temporary backend log on failure.

## Local verification commands

From the repository root:

```bash
node scripts/check-version.mjs 1.1.4
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

Do not create `v1.1.4` until the exact final source has passed all release-blocking gates.

```bash
git checkout main
git pull --ff-only
node scripts/check-version.mjs 1.1.4
git tag -a v1.1.4 -m "LibraCore v1.1.4"
git push origin v1.1.4
```

Never force-move previous published release tags.

## Next engineering priorities after v1.1.4

After release closure, continue with meaningful product/operational work rather than version-only commits:

- operational observability and diagnostics;
- representative performance/load measurement using the new repeatable fixtures;
- deeper branch-level workflows and reporting;
- deployment/environment validation;
- accessibility and internationalization improvements;
- further interoperability and library integrations.

## Project links

- GitHub: https://github.com/sanskarIN/libracore
- Maintainer: https://github.com/sanskarIN
- BuyMeACoffee: https://buymeacoffee.com/sanskarIN
