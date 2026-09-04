# LibraCore — 1.1.5 Engineering Handoff

**Audit/update date:** 2026-09-04  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Active release target:** `v1.1.5`  
**Previous published stable release:** `v1.1.4`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published release history must not be rewritten or force-moved. The attempted `v1.1.3` release workflow failure remains audit history.

## v1.1.5 work completed

Focused, reviewable commits have opened the operational observability release line:

- `feat(observability): add safe build metadata`
- `feat(observability): expose non-sensitive runtime info`
- `fix(config): preserve session TTL environment override`
- `chore(release): advance backend to 1.1.5`
- `chore(release): advance frontend to 1.1.5`
- `docs(release): add v1.1.5 release notes`
- `docs(changelog): record v1.1.5 observability work`
- `docs(roadmap): move observability into v1.1.5`

All listed commits use `Sanskar <sanskarin@outlook.in>`.

## Observability implementation

The backend now invokes the Spring Boot Maven `build-info` goal during packaging. This creates build metadata that the existing Actuator info endpoint can expose for deployment identification.

The actuator surface remains intentionally narrow:

- web exposure: `health,info`;
- health details: hidden;
- environment metadata: disabled;
- Git metadata: disabled.

This design allows operators to identify the deployed release without exposing environment values, credentials, datasource settings, or repository metadata through `/actuator/info`.

## Manifest state

- `backend/pom.xml` → `1.1.5`;
- `frontend/package.json` → `1.1.5`;
- `frontend/package-lock.json` → must be synchronized to `1.1.5` by the supported npm automation before release publication.

`scripts/check-version.mjs` remains the executable guard for all three version-bearing manifests.

## v1.1.5 remaining release gates

1. confirm frontend lockfile root and package-root metadata are `1.1.5`;
2. run `node scripts/check-version.mjs 1.1.5`;
3. pass Backend CI;
4. pass Frontend CI;
5. pass Version Sync;
6. pass CodeQL;
7. pass Dependency Review/security checks;
8. pass Recovery Drill;
9. pass Performance Fixture CI;
10. verify packaged `/actuator/info` contains build metadata for `1.1.5`;
11. verify `/actuator/info` does not expose environment or Git metadata;
12. complete role-based browser smoke journeys;
13. complete accessibility review;
14. review repository links/configuration and tracked secrets;
15. confirm branch-protection/rules status and document any host-level limitation;
16. identify the exact final verified commit;
17. create `v1.1.5` only from that exact commit;
18. confirm the tag-scoped release workflow succeeds;
19. review generated release artifacts and SHA-256 checksums;
20. confirm the GitHub release is published as stable/latest.

## v1.1.6 preparation

`v1.1.6` is intentionally not tagged or published yet. Its prepared direction is performance and deployment validation after `v1.1.5` closes.

Planned work:

- representative backend performance measurements using the existing safe PostgreSQL fixture baseline;
- repeatable latency/throughput acceptance thresholds for selected operations;
- deployment configuration validation for required runtime settings without exposing secret values;
- stronger startup/readiness diagnostics while preserving safe actuator boundaries;
- environment-specific validation and rollback documentation.

No `v1.1.6` version bump should be made until `v1.1.5` has completed its release gates.

## Release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → v1.1.5 → v1.1.6
```

The historical `2.0.12` and temporary `0.1.1` preparation lines remain audit history and are not active release targets.

## Local verification commands

From the repository root:

```bash
node scripts/check-version.mjs 1.1.5
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

Do not create `v1.1.5` until the exact final source has passed all release-blocking gates. Do not create `v1.1.6` until `v1.1.5` is published successfully.

## Project links

- GitHub: https://github.com/sanskarIN/libracore
- Maintainer: https://github.com/sanskarIN
- BuyMeACoffee: https://buymeacoffee.com/sanskarIN
