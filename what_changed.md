# LibraCore — 1.1.6 Engineering Handoff

**Audit/update date:** 2026-09-04  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Active release target:** `v1.1.6`  
**Previous published stable release:** `v1.1.5`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published release history must not be rewritten or force-moved. The attempted `v1.1.3` release workflow failure remains audit history.

## v1.1.5 closure

`v1.1.5` is now published as the stable/latest release. Its observability work remains immutable.

## v1.1.6 implementation completed so far

Focused commits for the performance and deployment-validation line:

- `chore(release): advance backend to 1.1.6`
- `feat(operations): add safe startup and readiness probes`
- `fix(config): keep readiness probes under one health endpoint configuration`
- `chore(release): advance frontend to 1.1.6`
- `test(performance): add repeatable database latency thresholds`
- `ci(performance): enforce repeatable latency thresholds on safe fixture`
- `docs(release): add v1.1.6 performance and deployment notes`
- `docs(changelog): record v1.1.6 performance and deployment work`
- `docs(roadmap): move performance validation into 1.1.6`

All listed commits use `Sanskar <sanskarin@outlook.in>`.

## Performance validation

The existing isolated PostgreSQL performance fixture is now paired with deterministic latency checks for representative catalog, circulation, and reservation queries.

Default acceptance threshold:

- maximum representative query execution time: `250 ms`;
- override: `PERF_MAX_QUERY_MS`;
- benchmark database convention: database name must end in `_perf` or `_benchmark`;
- fixture writes require `PERF_FIXTURE_ALLOW_WRITE=YES`.

Production databases must never be used for the benchmark workflow.

## Startup and readiness

The backend retains the restricted `health,info` Actuator surface and now enables Spring Boot health probes.

- liveness: `livenessState`;
- readiness: `readinessState,db`;
- health details remain hidden;
- environment metadata remains disabled;
- Git metadata remains disabled.

The readiness configuration is intentionally database-aware so an unavailable database cannot be reported as ready.

## Manifest state

- `backend/pom.xml` → `1.1.6`;
- `frontend/package.json` → `1.1.6`;
- `frontend/package-lock.json` → must be synchronized to `1.1.6` by the repository's supported lockfile automation before publication.

`scripts/check-version.mjs` remains the executable guard for all three version-bearing manifests.

## v1.1.6 remaining release gates

1. confirm frontend lockfile root and package-root metadata are `1.1.6`;
2. run `node scripts/check-version.mjs 1.1.6`;
3. pass Backend CI;
4. pass Frontend CI;
5. pass Version Sync;
6. pass CodeQL;
7. pass Dependency Review/security checks;
8. pass Recovery Drill;
9. pass Performance Fixture CI;
10. pass Performance Thresholds CI;
11. verify packaged `/actuator/info` contains build metadata for `1.1.6`;
12. verify liveness and readiness behavior;
13. verify readiness reflects database availability;
14. complete role-based browser smoke journeys;
15. complete accessibility review;
16. review deployment configuration and tracked secrets;
17. identify the exact final verified commit;
18. create `v1.1.6` only from that exact commit;
19. confirm the tag-scoped release workflow succeeds;
20. review generated release artifacts and SHA-256 checksums;
21. confirm the GitHub release is published as stable/latest.

## Release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → v1.1.5 → v1.1.6
```

The historical `2.0.12` and temporary `0.1.1` preparation lines remain audit history and are not active release targets.

## Verification commands

From the repository root:

```bash
node scripts/check-version.mjs 1.1.6
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

Performance fixture:

```bash
bash scripts/load-performance-fixture.sh
bash scripts/check-performance-fixture.sh
bash scripts/check-performance-thresholds.sh
```

## Tagging rule

Do not create `v1.1.6` until the exact final source has passed all release-blocking gates. Do not rewrite or force-move `v1.1.6` after publication.

## Project links

- GitHub: https://github.com/sanskarIN/libracore
- Maintainer: https://github.com/sanskarIN
- BuyMeACoffee: https://buymeacoffee.com/sanskarIN