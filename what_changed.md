# LibraCore — 1.1.8 Engineering Handoff

**Audit/update date:** 2026-09-04  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Active release target:** `v1.1.8`  
**Previous published stable release:** `v1.1.7`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published release history must not be rewritten or force-moved. The historical v1.1.3 workflow failure remains audit history.

## v1.1.7 closure

`v1.1.7` is published as a stable release. Its reliability and release-hardening work remains immutable.

## v1.1.8 implementation

Focused commits for the release-integrity and developer-experience line:

- `chore(release): advance backend to 1.1.8`
- `chore(release): advance frontend to 1.1.8`
- `fix(release): restore backend manifest and set 1.1.8`
- `fix(release): restore frontend manifest and set 1.1.8`
- `docs(release): add v1.1.8 release notes`
- `ci(release): add release manifest audit workflow`
- `docs(roadmap): open v1.1.8 release-integrity line`

All release commits use `Sanskar <sanskarin@outlook.in>`.

## Release-integrity scope

v1.1.8 focuses on deterministic version alignment and earlier detection of release-documentation drift. The new release-manifest audit checks the backend version, frontend version, frontend lockfile root version, and repository-managed release notes as a single release contract.

## Operational invariants

The v1.1.7 operational model remains in force:

- Actuator exposure: `health,info`;
- health details: hidden;
- environment metadata: disabled;
- Git metadata: disabled;
- liveness: application liveness state;
- readiness: readiness state plus database health;
- performance fixture writes: explicitly gated and isolated from production databases.

## Manifest state

- `backend/pom.xml` → `1.1.8`;
- `frontend/package.json` → `1.1.8`;
- `frontend/package-lock.json` → must be synchronized to `1.1.8` before publication.

## v1.1.8 release gates

1. confirm the lockfile root metadata is `1.1.8`;
2. pass the release-manifest audit;
3. pass the repository version guard;
4. pass Backend CI;
5. pass Frontend CI;
6. pass Version Sync;
7. pass CodeQL;
8. pass Dependency Review/security checks;
9. pass Recovery Drill;
10. pass Performance Fixture CI;
11. pass Performance Thresholds CI;
12. verify packaged startup and liveness;
13. verify database-aware readiness;
14. verify `/actuator/info` contains only expected non-sensitive build metadata;
15. complete browser smoke journeys;
16. complete accessibility validation;
17. review deployment configuration and tracked secrets;
18. identify the exact final verified commit;
19. create `v1.1.8` only from that exact commit;
20. confirm the tag-scoped release workflow succeeds;
21. review artifacts and SHA-256 checksums;
22. publish `v1.1.8` as stable/latest.

## Release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → v1.1.5 → v1.1.6 → v1.1.7 → v1.1.8
```

The historical `2.0.12` and temporary `0.1.1` preparation lines remain audit history and are not active release targets.
