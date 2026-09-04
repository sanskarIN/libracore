# Changelog

All notable LibraCore changes are recorded here. The project follows Keep a Changelog conventions and uses Semantic Versioning for release identifiers.

## [Unreleased]

### Next

- Future work belongs on the next planned release line after `1.1.6`.
- Do not treat unreleased source changes as part of a published release until the corresponding tag and release workflow succeed.

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

### Verification

- Version Sync must confirm backend, frontend, and lockfile root metadata are aligned to `1.1.6`.
- Performance Fixture CI must pass before release publication.
- Performance Thresholds CI must pass its representative query latency thresholds.
- Packaged startup/readiness behavior must be verified in the release workflow.
- Artifact and checksum verification remain publication gates.

### Release status

- `v1.1.6` is the active release target.
- `v1.1.5` is the published stable observability release.
- The attempted `v1.1.3` workflow failure remains immutable audit history.

## [1.1.5] - 2026-09-04 — operational observability and diagnostics

### Added

- Added Spring Boot build-info generation to packaged backend artifacts.
- Enabled the existing `/actuator/info` endpoint to report non-sensitive build metadata for deployment identification.

### Security and operations

- Kept actuator web exposure limited to `health` and `info`.
- Kept health details hidden with `show-details: never`.
- Explicitly disabled environment and Git metadata contributors so `/actuator/info` does not expose environment values, credentials, datasource configuration, or repository metadata.

### Changed

- Advanced the backend Maven project version to `1.1.5`.
- Advanced the frontend npm package version to `1.1.5`.
- Continued the committed frontend lockfile synchronization policy.
- Added repository-managed `v1.1.5` release notes.

### Release status

- `v1.1.5` is published as a stable release.
- `v1.1.4` was the previous published stable maintenance release.
- The attempted `v1.1.3` workflow failure remains immutable audit history.

## [1.1.4] - 2026-09-04 — published maintenance and performance reliability release

### Changed

- Advanced the backend Maven project version to `1.1.4`.
- Advanced the frontend npm package version to `1.1.4`.
- Added a deterministic release-notes path based on the release tag.
- Extended the packaged-backend health retry window after the v1.1.3 release attempt exposed a startup-readiness timing failure.
- Added automatic frontend lockfile synchronization when the package manifest changes on `main`.
- Added repository and issue metadata to the frontend package manifest.
- Added repeatable PostgreSQL performance fixtures with explicit write and database-name safety gates.
- Added CI verification for fixture syntax, migration loading, invariant checks, and repeatability.

### Release status

- `v1.1.4` is published as a stable release.
- The `v1.1.3` tagged release workflow failure is retained as diagnostic history; existing tags and published releases must not be rewritten.
