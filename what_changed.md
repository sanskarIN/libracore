# LibraCore — 1.1.10 Engineering Handoff

**Audit/update date:** 2026-09-04  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Active release target:** `v1.1.10`  
**Immediate preceding implementation line:** `v1.1.9`  
**Previous confirmed published stable release:** `v1.1.7`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published release history must not be rewritten or force-moved. The historical v1.1.3 workflow failure remains audit history.

## v1.1.10 implementation

The v1.1.10 release-preparation work has been merged into `main` through PR #25. The frontend lockfile synchronization automation subsequently aligned the committed lockfile metadata to `1.1.10`.

Completed preparation commits include:

- `chore(release): start v1.1.10 backend version line`
- `chore(release): start v1.1.10 frontend version line`
- `ci(release): advance manifest audit default to v1.1.10`
- `docs(release): add v1.1.10 release notes`
- `docs(changelog): open v1.1.10 release line`
- `docs(roadmap): add v1.1.10 verification release line`
- `docs(handoff): prepare v1.1.10 release engineering`
- merge of PR #25: `release: prepare v1.1.10`
- `chore(release): synchronize frontend lockfile`

All release commits use `Sanskar <sanskarin@outlook.in>`.

## v1.1.10 scope

v1.1.10 is focused on verification, reproducibility, release-readiness automation, and maintenance of the 1.1.x operational safety model. It is not intended to introduce a breaking application API change.

## Manifest state

- `backend/pom.xml` → `1.1.10`;
- `frontend/package.json` → `1.1.10`;
- `frontend/package-lock.json` → `1.1.10` at the root and workspace package entry;
- `.github/workflows/release-manifest-audit.yml` → manual-audit default `1.1.10`.

The lockfile was synchronized by the repository automation rather than by hand. The current main-branch lockfile root and package entry both report `1.1.10`.

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

1. verify frontend lockfile is synchronized to `1.1.10`;
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

`v1.1.8` remains preserved as an exact release candidate and `v1.1.9` remains the preceding implementation line. `v1.1.7` is the last release whose stable publication is confirmed in the project handoff. Do not claim `v1.1.8`, `v1.1.9`, or `v1.1.10` as published until GitHub shows the corresponding release/tag and its required tag-scoped validation has succeeded.

## Release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → v1.1.5 → v1.1.6 → v1.1.7 → v1.1.8 → v1.1.9 → v1.1.10
```

The historical `2.0.12` and temporary `0.1.1` preparation lines remain audit history and are not active release targets.
