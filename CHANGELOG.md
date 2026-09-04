# Changelog

All notable LibraCore changes are recorded here. The project follows Keep a Changelog conventions and uses Semantic Versioning for release identifiers.

## [Unreleased]

### Next

- Future work belongs on the next planned release line after `1.1.10`.
- Do not treat unreleased source changes as part of a published release until the corresponding tag and release workflow succeed.

## [1.1.10] - 2026-09-04 — verification, reproducibility and release readiness

### Added

- Added repository-managed `v1.1.10` release notes.
- Added a dedicated `v1.1.10` release branch for controlled release preparation.

### Changed

- Advanced the backend Maven project version to `1.1.10`.
- Advanced the frontend npm package version to `1.1.10`.
- Continued the committed frontend lockfile synchronization policy.
- Advanced the release-manifest audit workflow default to `1.1.10` for manual release audits.
- Continued the release-integrity and operational safeguards from the 1.1.x line.

### Verification

- The v1.1.10 release contract requires backend, frontend, and lockfile versions to agree.
- Release documentation must identify the same release version.
- Release-blocking CI, security validation, packaged behavior, browser/accessibility checks, artifact integrity, and checksum review remain publication gates.

### Release status

- `v1.1.10` is the active release target.
- `v1.1.9` remains the immediately preceding implementation line until the release sequence is formally promoted.
- Earlier published release history remains immutable.

## [1.1.9] - 2026-09-04 — release automation, testability and maintenance

### Added

- Added repository-managed `v1.1.9` release notes.
- Added a dedicated roadmap line for release automation, reproducibility, diagnostics, and maintenance.

### Changed

- Advanced the backend Maven project version to `1.1.9`.
- Advanced the frontend npm package version to `1.1.9`.
- Continued the committed frontend lockfile synchronization policy.
- Continued the release-integrity and operational safeguards from `v1.1.8`.

### Verification

- The v1.1.9 release contract requires backend, frontend, and lockfile versions to agree.
- Release documentation must identify the same release version.
- Release-blocking CI, security validation, packaged behavior, browser/accessibility checks, artifact integrity, and checksum review remain publication gates.

### Release status

- `v1.1.9` is the active release target.
- `v1.1.8` remains the immediately preceding release line until it is published stable.
- Earlier published release history remains immutable.

## [1.1.8] - 2026-09-04 — release integrity and developer experience

### Added

- Added repository-managed `v1.1.8` release notes.
- Added a release-manifest audit workflow for synchronized versions and release documentation.

### Changed

- Advanced the backend Maven project version to `1.1.8`.
- Advanced the frontend npm package version to `1.1.8`.
- Synchronized the committed frontend lockfile to `1.1.8`.
- Continued the v1.1.7 liveness, database-aware readiness, performance-fixture isolation, and safe Actuator model.

### Verification

- Release-manifest validation checks backend, frontend, and lockfile root versions as a single release contract.
- Repository-managed release notes are checked before release promotion.
- Complete release-blocking CI, packaged behavior, security validation, browser/accessibility checks, artifact integrity, and checksum review remain publication gates.

### Release status

- `v1.1.8` is the active release target.
- `v1.1.7` is the previous published stable reliability and release-hardening release.
- Earlier published release history remains immutable.

## [1.1.7] - 2026-09-04 — reliability and release hardening

### Added

- Added repository-managed `v1.1.7` release notes.
- Added an explicit release-integrity validation focus covering synchronized manifests, packaged behavior, artifact integrity, and rollback evidence.

### Changed

- Advanced the backend Maven project version to `1.1.7`.
- Advanced the frontend npm package version to `1.1.7`.
- Continued the committed frontend lockfile synchronization policy.
- Continued the v1.1.6 liveness, database-aware readiness, performance-fixture isolation, and safe Actuator model.

### Security and operations

- Actuator exposure remains limited to `health` and `info`.
- Health details remain hidden.
- Environment and Git metadata remain disabled.
- Performance-fixture writes remain explicitly gated and isolated from production databases.
- Release validation must review tracked secrets before publication.

### Verification

- Backend, frontend, and lockfile versions must resolve to `1.1.7`.
- Release-blocking CI must pass before publication.
- Packaged startup, health, readiness, and build metadata must be verified.
- Artifact and SHA-256 checksum verification remain publication gates.
- The final tag must point to the exact verified commit.

### Release status

- `v1.1.7` is a published stable release.
- `v1.1.6` is the previous published stable performance/deployment validation release.
- Earlier published release history remains immutable.

## [1.1.6] - 2026-09-04 — performance and deployment validation

### Added

- Added explicit Spring Boot liveness and readiness probe configuration.
- Added deterministic PostgreSQL latency-threshold checks for representative catalog, circulation, and reservation queries.
- Added a dedicated Performance Thresholds CI workflow over the isolated performance fixture.
- Added repository-managed `v1.1.6` release notes.

### Security and operations

- Readiness includes the database health component so deployment orchestration does not treat an unavailable database as ready.
- Liveness is limited to application liveness state.
- Actuator exposure remains limited to `health` and `info`.
- Health details remain hidden.
- Environment and Git metadata remain disabled.
- Performance fixtures require a dedicated `_perf` or `_benchmark` database convention and an explicit write gate.

### Changed

- Advanced the backend Maven project version to `1.1.6`.
- Advanced the frontend npm package version to `1.1.6`.
- Continued the committed frontend lockfile synchronization policy.

### Release status

- `v1.1.6` is published as a stable release.
- `v1.1.5` is the previous stable observability release.

## [1.1.5] - 2026-09-04 — operational observability and diagnostics

### Added

- Added Spring Boot build-info generation to packaged backend artifacts.
- Enabled the existing `/actuator/info` endpoint to report non-sensitive build metadata.

### Security and operations

- Kept actuator web exposure limited to `health` and `info`.
- Kept health details hidden with `show-details: never`.
- Explicitly disabled environment and Git metadata contributors.

### Changed

- Advanced the backend Maven project version to `1.1.5`.
- Advanced the frontend npm package version to `1.1.5`.
- Continued the committed frontend lockfile synchronization policy.

### Release status

- `v1.1.5` is published as a stable release.
- `v1.1.4` was the previous published stable maintenance release.

## [1.1.4] - 2026-09-04 — published maintenance and performance reliability release

### Changed

- Advanced the backend Maven project version to `1.1.4`.
- Preserved the established release verification and performance reliability gates.

## Historical release notes

- `v1.1.3` remains immutable historical audit history because its tag-scoped workflow failed during packaged backend health verification.
- `v1.1.2` is a published stable release.
- `v1.1.1` is a published stable release.
