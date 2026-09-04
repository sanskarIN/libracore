# LibraCore Roadmap

The roadmap describes direction, not a promise that an unchecked item already exists. `what_changed.md` is the implementation handoff and `CHANGELOG.md` records delivered behavior.

## 1.1.8 — Release integrity and developer experience

- [x] Advance backend executable manifest to `1.1.8`.
- [x] Advance frontend executable manifest to `1.1.8`.
- [x] Add repository-managed `v1.1.8` release notes.
- [x] Add a release-manifest audit workflow.
- [ ] Synchronize and verify the frontend lockfile to `1.1.8`.
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
- [x] Keep health details hidden.
- [x] Disable environment and Git metadata exposure.
- [x] Advance backend and frontend manifests to `1.1.5`.
- [x] Synchronize and verify the frontend lockfile.
- [x] Complete release-blocking CI and publication.
- [x] Publish stable `v1.1.5` as latest.

## Published history

The `v1.1.7` and `v1.1.6` releases are published stable releases and must remain immutable. The historical `v1.1.3` workflow failure is retained for auditability and must not be rewritten.

## Future

- Additional library integrations and interoperability.
- Expanded analytics and reporting.
- Further accessibility and internationalization improvements.
- Optional service decomposition only when operational boundaries justify it.
