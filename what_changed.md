# LibraCore — 1.1.9 Engineering Handoff

**Audit/update date:** 2026-09-04  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Active release target:** `v1.1.9`  
**Immediate preceding release line:** `v1.1.8`  
**Previous published stable release:** `v1.1.7`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published release history must not be rewritten or force-moved. The historical v1.1.3 workflow failure remains audit history.

## v1.1.8 current state

The v1.1.8 release line has its backend, frontend, and lockfile manifests aligned to `1.1.8`, and repository-managed release documentation is present. The release-manifest audit initially exposed an invalid XML state in `backend/pom.xml`; the audit workflow was hardened to report XML parse errors clearly. v1.1.8 must still pass the complete release-blocking CI and tag-scoped validation before it can be considered published.

## v1.1.9 implementation

Focused commits for the release-automation, testability, and maintenance line:

- `chore(release): start v1.1.9 backend version line`
- `chore(release): start v1.1.9 frontend version line`
- `docs(release): add v1.1.9 release notes`
- `docs(changelog): open v1.1.9 release line`
- `docs(roadmap): add v1.1.9 maintenance release line`

All release commits use `Sanskar <sanskarin@outlook.in>`.

## v1.1.9 scope

v1.1.9 is focused on deterministic release automation, reproducible frontend verification, clearer failure diagnostics, dependency/security review, and continued operational consistency. It is not intended to introduce a breaking application API change.

## Operational invariants

The existing production-safety model remains in force:

- Actuator exposure: `health,info`;
- health details: hidden;
- environment metadata: disabled;
- Git metadata: disabled;
- liveness: application liveness state;
- readiness: readiness state plus database health;
- performance fixture writes: explicitly gated and isolated from production databases;
- published tags: never force-moved or rewritten.

## Manifest state

- `backend/pom.xml` → `1.1.9`;
- `frontend/package.json` → `1.1.9`;
- `frontend/package-lock.json` → must be synchronized to `1.1.9` by the repository lockfile workflow before publication.

## v1.1.8 release gates

1. verify the lockfile root metadata is `1.1.8`;
2. pass the release-manifest audit;
3. pass repository version guards;
4. pass Backend CI and Frontend CI;
5. pass Version Sync;
6. pass CodeQL and dependency/security checks;
7. pass Recovery Drill;
8. pass Performance Fixture CI and Performance Thresholds CI;
9. verify packaged startup, liveness, readiness, and build metadata;
10. complete browser smoke and accessibility validation;
11. review deployment configuration and tracked secrets;
12. review artifacts and SHA-256 checksums;
13. identify the exact final verified commit;
14. create and publish `v1.1.8` only from that exact commit;
15. confirm tag-scoped release validation succeeds.

## v1.1.9 release gates

1. synchronize frontend lockfile to `1.1.9`;
2. pass the release-manifest audit for `1.1.9`;
3. pass repository version guards;
4. pass Backend CI and Frontend CI;
5. pass Version Sync;
6. pass CodeQL and dependency/security checks;
7. pass Recovery Drill;
8. pass Performance Fixture CI and Performance Thresholds CI;
9. verify packaged startup, liveness, readiness, and build metadata;
10. complete browser smoke and accessibility validation;
11. review deployment configuration and tracked secrets;
12. review artifact and checksum output;
13. identify the exact final verified commit;
14. create `v1.1.9` only from that exact commit;
15. confirm tag-scoped release validation succeeds;
16. publish `v1.1.9` as stable/latest.

## Release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → v1.1.5 → v1.1.6 → v1.1.7 → v1.1.8 → v1.1.9
```

The historical `2.0.12` and temporary `0.1.1` preparation lines remain audit history and are not active release targets.
