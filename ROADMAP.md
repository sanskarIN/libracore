# LibraCore Roadmap

The roadmap describes direction, not a promise that an unchecked item already exists. `what_changed.md` is the implementation handoff and `CHANGELOG.md` records delivered behavior.

## 1.1.5 — Operational observability and diagnostics

- [x] Preserve the published `v1.1.4` history and all earlier release history.
- [x] Add Spring Boot build metadata to packaged backend artifacts.
- [x] Enable non-sensitive build information through the existing `/actuator/info` endpoint.
- [x] Keep actuator exposure limited to `health,info`.
- [x] Keep health details hidden.
- [x] Explicitly disable environment and Git metadata exposure through actuator info.
- [x] Advance backend and frontend executable manifests to `1.1.5`.
- [x] Add repository-managed `v1.1.5` release notes.
- [ ] Synchronize and verify the committed frontend lockfile at `1.1.5`.
- [ ] Run the complete release-blocking CI suite against the final source.
- [ ] Verify packaged `/actuator/info` contains the expected build version and does not expose environment metadata.
- [ ] Complete role-based browser smoke testing and accessibility evidence.
- [ ] Review repository configuration, tracked secrets, and release artifacts.
- [ ] Tag and publish `v1.1.5` only after every release gate is closed.

## 1.1.6 — Performance and deployment validation

After `v1.1.5` release closure, the next maintenance increment will focus on turning the repeatable performance fixture baseline into actionable performance measurements and strengthening deployment/environment validation.

- [ ] Add representative performance measurement scenarios using the existing safe PostgreSQL fixtures.
- [ ] Define repeatable latency/throughput acceptance thresholds for selected backend operations.
- [ ] Add deployment configuration validation for required runtime settings without exposing secret values.
- [ ] Improve operational startup/readiness diagnostics while preserving safe actuator boundaries.
- [ ] Document environment-specific validation and rollback evidence.

## Published 1.1.4 release line

The repository's `v1.1.4` release is published and remains immutable. The attempted `v1.1.3` release workflow failure is retained for auditability and must not be rewritten.

## Historical 1.1.3 release line

The repository prepared and tagged `v1.1.3`, but its release workflow failed during packaged backend health verification. The source, tag, and failed workflow evidence remain intact.

## Historical 1.1.2 release line

The repository's `v1.1.2` release is published and remains immutable.

## Historical 1.1.1 release line

The repository's `v1.1.1` release is published and remains immutable.

## Historical 1.1.0 release line

The repository's `v1.1.0` release preparation and publication history are retained for auditability. Its tag is historical and must not be rewritten.

## Historical 2.0.12 engineering line

The repository previously used `2.0.12` as a release-candidate engineering line. Its implementation work is retained in Git history and changelog documentation, but it is no longer an active release target.

## Historical 0.1.1 rebaseline attempt

A temporary `0.1.1` release-rebaseline pass was superseded after confirming the intended post-`v1.0.0` release sequence. Those preparation commits remain in Git history as auditable work and are not a published `v0.1.1` release.

## Future

- Additional library integrations and interoperability.
- Expanded analytics and reporting.
- Further accessibility and internationalization improvements.
- Optional service decomposition only when operational boundaries justify it.
