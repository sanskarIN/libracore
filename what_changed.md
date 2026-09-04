# LibraCore — 1.1.7 Engineering Handoff

**Audit/update date:** 2026-09-04  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Active release target:** `v1.1.7`  
**Previous published stable release:** `v1.1.6`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published release history must not be rewritten or force-moved. The historical v1.1.3 workflow failure remains audit history.

## v1.1.6 closure

`v1.1.6` is published as a stable release. Its performance and deployment-validation work remains immutable.

## v1.1.7 implementation

Focused commits for the reliability and release-hardening line:

- `chore(release): advance backend to 1.1.7`
- `chore(release): advance frontend to 1.1.7`
- `docs(release): add v1.1.7 reliability hardening notes`
- `docs(changelog): record v1.1.7 release-hardening line`
- `docs(roadmap): move reliability hardening into 1.1.7`

All listed commits use `Sanskar <sanskarin@outlook.in>`.

## Reliability scope

v1.1.7 is a maintenance and release-hardening line. It preserves the operational behavior established in v1.1.6 while making release promotion more deterministic and auditable.

Required validation includes:

- synchronized backend, frontend, and lockfile versions;
- exact tag-to-commit correspondence;
- complete release-blocking CI;
- packaged startup, liveness, readiness, and build-info verification;
- browser smoke and accessibility evidence;
- deployment configuration review;
- tracked-secret review;
- artifact and SHA-256 checksum review;
- rollback evidence.

## Operational invariants

The v1.1.6 operational model remains in force:

- Actuator exposure: `health,info`;
- health details: hidden;
- environment metadata: disabled;
- Git metadata: disabled;
- liveness: application liveness state;
- readiness: readiness state plus database health;
- performance fixture writes: explicitly gated and isolated from production databases.

## Manifest state

- `backend/pom.xml` → `1.1.7`;
- `frontend/package.json` → `1.1.7`;
- `frontend/package-lock.json` → must be synchronized to `1.1.7` by the repository's version/lockfile automation.

## v1.1.7 remaining release gates

1. confirm the lockfile root metadata is `1.1.7`;
2. run the repository version guard;
3. pass Backend CI;
4. pass Frontend CI;
5. pass Version Sync;
6. pass CodeQL;
7. pass Dependency Review/security checks;
8. pass Recovery Drill;
9. pass Performance Fixture CI;
10. pass Performance Thresholds CI;
11. verify packaged startup and liveness;
12. verify database-aware readiness;
13. verify `/actuator/info` contains only expected non-sensitive build metadata;
14. complete browser smoke journeys;
15. complete accessibility validation;
16. review deployment configuration and tracked secrets;
17. identify the exact final verified commit;
18. create `v1.1.7` only from that exact commit;
19. confirm the tag-scoped release workflow succeeds;
20. review artifacts and SHA-256 checksums;
21. publish `v1.1.7` as stable/latest.

## Release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → v1.1.5 → v1.1.6 → v1.1.7
```

The historical `2.0.12` and temporary `0.1.1` preparation lines remain audit history and are not active release targets.
