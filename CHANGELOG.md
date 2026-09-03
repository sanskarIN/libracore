# Changelog

All notable LibraCore changes are recorded here. The project follows Keep a Changelog conventions and uses Semantic Versioning for release identifiers.

## [Unreleased]

### Next

- Future work belongs on the next planned release line after `1.1.1`.
- Do not treat unreleased source changes as part of a published release until the corresponding tag and release workflow succeed.

## [1.1.1] - 2026-09-03 — release candidate

### Changed

- Advanced the backend Maven project version to `1.1.1`.
- Advanced the frontend npm package version to `1.1.1`.
- Synchronized the generated frontend lockfile with the release version through automated npm lockfile generation.
- Extended the executable release-version guard to validate the frontend lockfile root metadata.
- Added repository-managed `v1.1.1` release notes and configured release automation to publish those prepared notes instead of generating an unrelated automatic description.
- Preserved all earlier release tags and historical engineering work without rewriting published history.

### Verification

- The release workflow validates the tag against frontend, backend, and lockfile versions before build work.
- The tagged release workflow must pass backend verification, packaged PostgreSQL startup and health checks, reproducible frontend installation, frontend quality checks, artifact packaging, and checksum generation before publication.
- Security, dependency, recovery, browser smoke, and accessibility evidence remain release-blocking where required by the repository's release checklist.

### Release status

- `v1.1.1` is the active release target.
- It must not be described as published until a successful tag-triggered release workflow creates the GitHub release.

## Historical 1.1.0 release line

The repository previously prepared the `1.1.0` release line. Its source and CI history remain available for auditability. The current continuation target is `1.1.1`.

## Historical 2.0.12 engineering line

The repository previously used `2.0.12` as a release-candidate engineering line. Its implementation work is retained in Git history and documentation, but it is no longer an active release target.

## Historical 0.1.1 preparation

A temporary `0.1.1` release-rebaseline pass was superseded after confirming the intended post-`v1.0.0` release sequence. Those preparation commits remain in Git history for auditability and were never intended to become a published release.

### Product capabilities already present in the engineering line

- Responsive React/TypeScript application with authenticated role-aware navigation.
- Staff and member dashboards.
- Catalog search, book/copy administration, barcode/QR/accession lookup, and branch-aware availability.
- Member administration and account linkage workflows.
- Administrator staff-account management for account creation, role filtering, enable/disable control, password reset, and active-session visibility.
- Issue, return, renewal, reservations/waitlists, fine assessment/settlement, and configurable circulation policies.
- Reporting dashboard, overdue views, audit views, and CSV import/export workflows.
- Mock-safe and SMTP notification adapters.
- PostgreSQL schema managed through Flyway migrations.
