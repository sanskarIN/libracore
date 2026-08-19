# Changelog

All notable LibraCore changes are recorded here. The project follows Keep a Changelog conventions and intends to use Semantic Versioning for stable releases.

## [Unreleased]

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
- Backend/frontend GitHub Actions quality gates, CodeQL, pull-request dependency review, Dependabot, and tagged release automation.
- Security, privacy, threat-model, support, contribution, operations, architecture, testing, release, branch-protection, and exhaustive repository-reference documentation.
- Structured issue forms, pull-request checklist, funding metadata, and repository governance guidance.

### Fixed

- Restored the missing React application entry point and completed previously dead application routes during the final frontend implementation pass.
- Fixed a strict TypeScript `exactOptionalPropertyTypes` incompatibility in login error props.
- Preserved the staff-account creation form element safely across asynchronous submission before resetting it.
- Kept every role-authorized navigation destination reachable on mobile instead of truncating the bottom navigation to five routes.
- Corrected setup/testing/release documentation so clean clones use `npm install` until a synchronized frontend lockfile is committed; documentation no longer instructs an impossible `npm ci` path.
- Exact-pinned direct frontend dependency/tool versions and added one aggregate frontend quality command.

### Security

- Password hashing, session expiry/revocation, bounded validation, parameterized data access, server-side authorization, CORS configuration, audit logging, and secret-safe configuration.
- Documented trust boundaries, residual risks, private vulnerability reporting, privacy responsibilities, production boundaries, and branch-protection expectations.

## [0.1.0] - 2026-08-19

### Added

- Initial LibraCore repository foundation and modular-monolith backend.
- PostgreSQL development environment, initial schema, and core library domain model.
- Core catalog, member, circulation, security, reporting, exchange, and notification modules.

[Unreleased]: https://github.com/sanskarIN/libracore/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/sanskarIN/libracore/releases/tag/v0.1.0
