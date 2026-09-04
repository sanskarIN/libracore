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

The v1.2.0 preparation line is open on `main`. The backend, frontend, lockfile, release documentation, API compatibility policy, release checklist, operator notes, CI guards, and artifact-verification guidance have been prepared.

Completed v1.2.0 preparation commits include:

- `chore(release): start v1.2.0 backend version line`
- `chore(release): start v1.2.0 frontend version line`
- `docs(release): add v1.2.0 release notes`
- `docs(api): establish 1.2.x compatibility policy`
- `docs(release): add reusable v1.2.0 release contract checklist`
- `docs(ops): add v1.2.0 operator deployment notes`
- `docs(changelog): open v1.2.0 release line`
- `docs(roadmap): open v1.2.0 platform maturity line`
- `ci(release): advance manifest audit default to v1.2.0`
- `ci(api): add 1.2.x API contract policy guard`
- `ci(release): add v1.2.0 preflight workflow`
- `fix(ci): harden v1.2.0 preflight invariant checks`
- `docs(release): add artifact provenance verification guide`
- `docs(roadmap): record v1.2.0 lockfile and CI preparation`
- `docs(handoff): establish v1.2.0 engineering checkpoint`

The frontend lockfile now reports `1.2.0` at both the lockfile root and workspace package entry.

## v1.2.0 scope

v1.2.0 establishes a contract-driven platform baseline: explicit API compatibility rules, reusable release verification, deployment guidance, artifact provenance expectations, and stronger continuity documentation. Application behavior changes should be introduced only when they can be covered by the existing security, persistence, accessibility, performance, and recovery gates.

## Manifest state

- `backend/pom.xml` → `1.2.0`;
- `frontend/package.json` → `1.2.0`;
- `frontend/package-lock.json` → `1.2.0` at the root and workspace package entry;
- release-manifest audit default → `1.2.0`;
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

1. verify all three executable manifests are synchronized to `1.2.0`;
2. pass the release-manifest audit for `1.2.0`;
3. pass repository version guards;
4. pass Backend CI and Frontend CI;
5. pass Version Sync;
6. pass API contract policy validation;
7. pass CodeQL and dependency/security checks;
8. pass Recovery Drill;
9. pass Performance Fixture CI and Performance Thresholds CI;
10. verify packaged startup, liveness, readiness, and build metadata;
11. complete browser smoke and accessibility validation;
12. review deployment configuration and tracked secrets;
13. review artifact and SHA-256 checksum output;
14. identify the exact final verified commit;
15. create `v1.2.0` only from that exact commit;
16. confirm tag-scoped validation succeeds;
17. publish `v1.2.0` as stable/latest.

## Current publication status

`v1.2.0` is **prepared but not yet claimed as published stable**. The repository changes are on `main`, but the release must not be called stable until the complete release gates, exact tag, and tag-scoped validation succeed.

## Release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → v1.1.5 → v1.1.6 → v1.1.7 → v1.1.8 → v1.1.9 → v1.1.10 → v1.2.0
```

The historical `2.0.12` and temporary `0.1.1` preparation lines remain audit history and are not active release targets.
