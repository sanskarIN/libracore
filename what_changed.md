# LibraCore — 1.2.0 Engineering Handoff

**Audit/update date:** 2026-09-04  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Active release target:** `v1.2.0`  
**Immediate preceding implementation line:** `v1.1.10`  
**Previous confirmed published stable release:** `v1.1.7`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published release history must not be rewritten or force-moved. The historical v1.1.3 workflow failure remains audit history.

## v1.2.0 implementation

The v1.2.0 preparation line is now open on `main`. This minor release starts the platform-maturity phase after the 1.1.x verification and release-hardening work.

Completed v1.2.0 preparation commits include:

- `chore(release): start v1.2.0 backend version line`
- `chore(release): start v1.2.0 frontend version line`
- `docs(release): add v1.2.0 release notes`
- `docs(api): establish 1.2.x compatibility policy`
- `docs(release): add reusable v1.2.0 release contract checklist`
- `docs(ops): add v1.2.0 operator deployment notes`
- `docs(changelog): open v1.2.0 release line`
- `docs(roadmap): open v1.2.0 platform maturity line`

All release commits use `Sanskar <sanskarin@outlook.in>`.

## v1.2.0 scope

v1.2.0 establishes a contract-driven platform baseline: explicit API compatibility rules, reusable release verification, deployment guidance, and stronger continuity documentation. Application behavior changes should be introduced only when they can be covered by the existing security, persistence, accessibility, performance, and recovery gates.

## Manifest state

- `backend/pom.xml` → `1.2.0`;
- `frontend/package.json` → `1.2.0`;
- `frontend/package-lock.json` → must be synchronized to `1.2.0` before final tagging;
- active release documentation → `v1.2.0`.

## Operational invariants

- Actuator exposure: `health,info`;
- health details: hidden;
- environment metadata: disabled;
- Git metadata: disabled;
- liveness: application liveness state;
- readiness: readiness state plus database health;
- performance fixture writes: explicitly gated and isolated from production databases;
- published tags: never force-moved or rewritten.

## v1.2.0 release gates

1. synchronize and verify the frontend lockfile to `1.2.0`;
2. pass the release-manifest audit for `1.2.0`;
3. pass repository version guards;
4. pass Backend CI and Frontend CI;
5. pass Version Sync;
6. pass CodeQL and dependency/security checks;
7. pass Recovery Drill;
8. pass Performance Fixture CI and Performance Thresholds CI;
9. verify packaged startup, liveness, readiness, and build metadata;
10. complete browser smoke and accessibility validation;
11. review deployment configuration and tracked secrets;
12. review artifact and SHA-256 checksum output;
13. identify the exact final verified commit;
14. create `v1.2.0` only from that exact commit;
15. confirm tag-scoped validation succeeds;
16. publish `v1.2.0` as stable/latest.

## Release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → v1.1.5 → v1.1.6 → v1.1.7 → v1.1.8 → v1.1.9 → v1.1.10 → v1.2.0
```

The historical `2.0.12` and temporary `0.1.1` preparation lines remain audit history and are not active release targets.
