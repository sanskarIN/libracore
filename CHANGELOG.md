# Changelog

All notable LibraCore changes are recorded here. The project follows Keep a Changelog conventions and intends to use Semantic Versioning for stable releases.

## [Unreleased]

### Added

- Responsive React/TypeScript application with authenticated role-aware navigation.
- Staff and member dashboards.
- Catalog search, book/copy administration, barcode/QR/accession lookup, and branch-aware availability.
- Member administration and account linkage workflows.
- Issue, return, renewal, reservations/waitlists, fine assessment/settlement, and configurable circulation policies.
- Reporting dashboard, overdue views, audit views, and CSV import/export workflows.
- Mock-safe and SMTP notification adapters.
- PostgreSQL schema managed through Flyway migrations.
- Opaque bearer-session authentication and server-side role authorization.
- Backup and restore scripts.
- Light/dark/system appearance support and responsive accessibility-oriented design system.
- Frontend utility/unit tests and backend domain/parser tests.
- Backend and frontend GitHub Actions quality gates.
- Security, privacy, threat-model, support, contribution, operations, architecture, testing, release, and continuity documentation.

### Security

- Password hashing, session expiry/revocation, bounded validation, parameterized data access, server-side authorization, CORS configuration, audit logging, and secret-safe configuration.

## [0.1.0] - 2026-08-19

### Added

- Initial LibraCore repository foundation and modular-monolith backend.
- PostgreSQL development environment, initial schema, and core library domain model.
- Core catalog, member, circulation, security, reporting, exchange, and notification modules.

[Unreleased]: https://github.com/sanskarIN/libracore/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/sanskarIN/libracore/releases/tag/v0.1.0
