# LibraCore — 1.1.10 Engineering Handoff

**Audit/update date:** 2026-09-04  
**Repository:** `sanskarIN/libracore`  
**Branch:** `release/v1.1.10`  
**Active release target:** `v1.1.10`  
**Immediate preceding implementation line:** `v1.1.9`  
**Previous published stable release:** `v1.1.7`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published release history must not be rewritten or force-moved. The historical v1.1.3 workflow failure remains audit history.

## v1.1.10 implementation

The v1.1.10 release-preparation branch was created from the current `main` line and now contains the release version and documentation work required to begin final verification.

Completed preparation commits:

- `chore(release): start v1.1.10 backend version line`
- `chore(release): start v1.1.10 frontend version line`
- `ci(release): advance manifest audit default to v1.1.10`
- `docs(release): add v1.1.10 release notes`
- `docs(changelog): open v1.1.10 release line`
- `docs(roadmap): add v1.1.10 verification release line`

All release commits use `Sanskar <sanskarin@outlook.in>`.

## v1.1.10 scope

v1.1.10 is focused on verification, reproducibility, release-readiness automation, and maintenance of the 1.1.x operational safety model. It is not intended to introduce a breaking application API change.

## Manifest state

- `backend/pom.xml` → `1.1.10`;
- `frontend/package.json` → `1.1.10`;
- `frontend/package-lock.json` → must be synchronized to `1.1.10` by the repository lockfile workflow before publication;
- `.github/workflows/release-manifest-audit.yml` → manual-audit default `1.1.10`.

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

## v1.1.10 release gates

1. synchronize frontend lockfile to `1.1.10`;
2. pass the release-manifest audit for `1.1.10`;
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
14. create `v1.1.10` only from that exact commit;
15. confirm tag-scoped validation succeeds;
16. publish `v1.1.10` as stable/latest.

## Important release-order constraint

The repository currently has a release-preparation sequence in progress. `v1.1.8` was preserved as an exact release candidate and `v1.1.9` is the preceding implementation line, while `v1.1.7` is the last confirmed published stable release. Do not claim `v1.1.8`, `v1.1.9`, or `v1.1.10` as published until GitHub shows the corresponding release/tag and its required tag-scoped validation has succeeded.

## Release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → v1.1.5 → v1.1.6 → v1.1.7 → v1.1.8 → v1.1.9 → v1.1.10
```

The historical `2.0.12` and temporary `0.1.1` preparation lines remain audit history and are not active release targets.
