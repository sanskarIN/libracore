# LibraCore Roadmap

The roadmap describes direction, not a promise that an unchecked item already exists. `what_changed.md` is the implementation handoff and `CHANGELOG.md` records delivered behavior.

## 1.2.0 — Platform maturity and operational contracts

- [x] Advance backend executable manifest to `1.2.0`.
- [x] Advance frontend executable manifest to `1.2.0`.
- [x] Add repository-managed `v1.2.0` release notes.
- [x] Establish an explicit API compatibility/versioning policy for `/api`.
- [x] Add a reusable v1.2.0 release-contract checklist.
- [x] Add operator deployment and post-deployment smoke guidance.
- [x] Record the 1.2.0 release sequence and publication gates.
- [ ] Synchronize and verify the frontend lockfile to `1.2.0`.
- [ ] Run the complete release-blocking CI suite against the final source.
- [ ] Complete repository version-guard validation.
- [ ] Complete CodeQL and dependency/security validation.
- [ ] Verify packaged startup, liveness, readiness, and `/actuator/info` behavior.
- [ ] Complete browser smoke journeys and accessibility evidence.
- [ ] Review deployment configuration and tracked secrets.
- [ ] Review artifact and SHA-256 checksum output.
- [ ] Identify the exact final verified commit.
- [ ] Create `v1.2.0` only from the exact verified commit.
- [ ] Confirm tag-scoped validation succeeds.
- [ ] Publish stable/latest `v1.2.0`.

## 1.1.10 — Verification, reproducibility, and release readiness

- [x] Advance backend executable manifest to `1.1.10`.
- [x] Advance frontend executable manifest to `1.1.10`.
- [x] Synchronize and verify the frontend lockfile to `1.1.10`.
- [x] Add repository-managed `v1.1.10` release notes.
- [x] Create a dedicated `release/v1.1.10` preparation branch.
- [x] Advance the release-manifest audit default to `1.1.10`.
- [ ] Run the complete release-blocking CI suite against the final source.
- [ ] Complete repository version-guard validation.
- [ ] Complete CodeQL and dependency/security validation.
- [ ] Verify packaged startup, liveness, readiness, and `/actuator/info` behavior.
- [ ] Complete browser smoke journeys and accessibility evidence.
- [ ] Review deployment configuration and tracked secrets.
- [ ] Review artifact and SHA-256 checksum output.
- [ ] Identify the exact final verified commit.
- [ ] Create `v1.1.10` only from the exact verified commit.
- [ ] Confirm tag-scoped validation succeeds.
- [ ] Publish stable/latest `v1.1.10`.

## 1.1.9 — Release automation, testability, and maintenance

- [x] Advance backend executable manifest to `1.1.9`.
- [x] Advance frontend executable manifest to `1.1.9`.
- [x] Synchronize and verify the frontend lockfile to `1.1.9`.
- [x] Add repository-managed `v1.1.9` release notes.
- [x] Add a deterministic release-readiness audit for version, documentation, and release metadata.
- [x] Strengthen CI failure diagnostics without weakening release gates.
- [x] Verify backend/frontend test commands remain reproducible on supported toolchain versions.
- [ ] Review dependency drift and security advisories.
- [ ] Review deployment and rollback documentation for 1.1.x consistency.
- [ ] Complete release-blocking CI and tag-scoped validation.
- [ ] Create and publish stable `v1.1.9` only after all gates pass.

## 1.1.8 — Release integrity and developer experience

- [x] Advance backend executable manifest to `1.1.8`.
- [x] Advance frontend executable manifest to `1.1.8`.
- [x] Add repository-managed `v1.1.8` release notes.
- [x] Add a release-manifest audit workflow.
- [x] Synchronize and verify the frontend lockfile to `1.1.8`.
- [ ] Run the complete release-blocking CI suite against the final source.
- [ ] Complete CodeQL and dependency/security validation.
- [ ] Verify packaged startup, liveness, readiness, and `/actuator/info` behavior.
- [ ] Complete browser smoke journeys and accessibility evidence.
- [ ] Review deployment configuration and tracked secrets.
- [ ] Review artifact and checksum output.
- [ ] Create and publish `v1.1.8` only from the exact verified commit.

## 1.1.7 — Reliability and release hardening

- [x] Advance backend executable manifest to `1.1.7`.
- [x] Advance frontend executable manifest to `1.1.7`.
- [x] Add repository-managed `v1.1.7` release notes.
- [x] Record the release line in the changelog.
- [x] Carry forward database-aware readiness and safe Actuator boundaries from `1.1.6`.
- [x] Define release-integrity validation requirements.
- [x] Synchronize the frontend lockfile to `1.1.7`.
- [x] Complete release-blocking validation and publish stable `v1.1.7`.

## 1.1.6 — Performance and deployment validation

- [x] Advance backend executable manifest to `1.1.6`.
- [x] Advance frontend executable manifest to `1.1.6`.
- [x] Add explicit Spring Boot liveness and readiness probes.
- [x] Keep readiness tied to database health while preserving restricted Actuator exposure.
- [x] Add representative performance measurement scenarios over the isolated PostgreSQL fixture.
- [x] Define repeatable query latency acceptance thresholds.
- [x] Add Performance Thresholds CI validation.
- [x] Synchronize frontend lockfile to `1.1.6`.
- [x] Complete release-blocking CI and tag-scoped publication.
- [x] Verify packaged startup/readiness/build-info behavior.
- [x] Publish stable `v1.1.6`.

## 1.1.5 — Operational observability and diagnostics

- [x] Preserve the published `v1.1.4` history and earlier release history.
- [x] Add Spring Boot build metadata to packaged backend artifacts.
- [x] Enable non-sensitive build information through `/actuator/info`.
- [x] Keep actuator exposure limited to `health,info`.
