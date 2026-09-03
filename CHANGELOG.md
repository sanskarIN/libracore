# Changelog

All notable LibraCore changes are recorded here. The project follows Keep a Changelog conventions and uses Semantic Versioning for release identifiers.

## [Unreleased]

### Release hardening

- Rebased the executable frontend/backend release manifests onto the `0.1.1` release line.
- Retained the committed npm-generated frontend lockfile as the canonical dependency graph; a lockfile regeneration is required if dependency metadata changes.
- Keep release publication fail-closed on version mismatch, missing lockfile, backend verification, packaged-service health, frontend quality, security, and recovery evidence.
- Continue using the automated PostgreSQL Recovery Drill to validate the real backup/restore path and packaged backend startup against restored data.

### Release gates

- Complete current Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and Recovery Drill evidence on the intended release source.
- Complete role-based browser smoke and manual accessibility evidence.
- Enable `main` branch protection/code-owner enforcement after stable required-check names are confirmed.
- Create and publish `v0.1.1` only after every release-blocking gate is closed.

## [0.1.1] - 2026-09-03 — release candidate

### Changed

- Rebased the backend Maven version from `2.0.12` to `0.1.1`.
- Rebased the frontend npm package version from `2.0.12` to `0.1.1`.
- Updated the documented release process to use `v0.1.1`.

### Verification

- The existing release workflow previously demonstrated that tag/manifest validation is active by rejecting `v1.0.0` when source manifests still declared `2.0.12`.
- The new `0.1.1` release line must still pass the complete tag-triggered release workflow before publication.

### Release status

- `v0.1.1` is a release candidate until the complete release gates pass.
- No claim of a published stable `v0.1.1` release should be made until the GitHub release workflow succeeds.

## [2.0.12] - 2026-08-19 — release candidate

### Added

- Responsive React/TypeScript application with authenticated role-aware navigation.
- Staff and member dashboards.
- Catalog search, book/copy administration, barcode/QR/accession lookup, and branch-aware availability.
- Member administration and account linkage workflows.
- Administrator staff-account management for account creation, role filtering, enable/disable control, password reset, and active-session visibility.
- Issue, return, renewal, reservations/waitlists, fine assessment/settlement, and configurable circulation policies.
- Reporting dashboard, overdue views, audit views, and CSV import/export workflows.
- Mock-safe and SMTP notification adapters.
- PostgreSQL schema managed through Flyway migrations.
- Opaque bearer-session authentication and server-side role authorization.
- Backup and restore scripts.
- Light/dark/system appearance support and responsive accessibility-oriented design system.
- Frontend utility/unit tests and backend domain/parser tests.
- Backend/frontend GitHub Actions quality gates, CodeQL, pull-request dependency review, Dependabot, tagged release automation, and version synchronization automation.
- Explicit Frontend Lockfile Bootstrap workflow for maintainers.
- Cross-manifest `scripts/check-version.mjs` guard for frontend/backend/tag version consistency.
- Security, privacy, threat-model, support, contribution, operations, architecture, testing, release, branch-protection, and exhaustive repository-reference documentation.
- Structured issue forms, pull-request checklist, funding metadata, and repository governance guidance.

### Changed

- Backend Maven and frontend npm package versions were previously aligned to `2.0.12`; the active release line is now `0.1.1`.
- The Settings page derives its displayed web version from build-time package metadata rather than a hard-coded value.
- Frontend CI is read-only and requires a committed lockfile for reproducible `npm ci` verification.
- Release automation validates tag/manifest consistency before packaging.
- Release and backend CI discover the packaged backend JAR without embedding a historical version in workflow source.
- README, release process, and roadmap previously described the 2.0.12 release-candidate line; the release process is now rebaselined for 0.1.1.

### Fixed

- Restored the missing React application entry point and completed previously dead application routes during the final frontend implementation pass.
- Fixed a strict TypeScript `exactOptionalPropertyTypes` incompatibility in login error props.
- Preserved the staff-account creation form element safely across asynchronous submission before resetting it.
- Kept every role-authorized navigation destination reachable on mobile instead of truncating the bottom navigation to five routes.
- Corrected setup/testing/release documentation so clean clones do not instruct `npm ci` before a lockfile exists.
- Exact-pinned direct frontend dependency/tool versions and added one aggregate frontend quality command.
- Fixed backend CI/release packaging paths that still referenced `libracore-backend-0.1.0-SNAPSHOT.jar` after the 2.0.12 version change.

### Security

- Password hashing, session expiry/revocation, bounded validation, parameterized data access, server-side authorization, CORS configuration, audit logging, and secret-safe configuration.
- Documented trust boundaries, residual risks, private vulnerability reporting, privacy responsibilities, production boundaries, and branch-protection expectations.
- Release verification refuses to publish without the committed frontend dependency lockfile.

## [0.1.0] - 2026-08-19

### Added

- Initial LibraCore repository foundation and modular-monolith backend.
- PostgreSQL development environment, initial schema, and core library domain model.
- Core catalog, member, circulation, security, reporting, exchange, and notification modules.

[Unreleased]: https://github.com/sanskarIN/libracore/compare/v0.1.1...HEAD
[0.1.1]: https://github.com/sanskarIN/libracore/releases/tag/v0.1.1
[2.0.12]: https://github.com/sanskarIN/libracore/releases/tag/v2.0.12
[0.1.0]: https://github.com/sanskarIN/libracore/releases/tag/v0.1.0
