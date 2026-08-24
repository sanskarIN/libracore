# Changelog

All notable LibraCore changes are recorded here. The project follows Keep a Changelog conventions and uses Semantic Versioning for release identifiers.

## [Unreleased]

### Release hardening

- Fixed the Frontend Lockfile Bootstrap workflow so the first generated, untracked `frontend/package-lock.json` is detected and committed instead of being missed by `git diff --quiet`.
- Preserve the generated frontend lockfile as a short-lived Actions artifact before later verification so a failing quality gate does not discard useful dependency-state evidence.
- Cancel superseded Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and lockfile-bootstrap runs to reduce stale verification and runner waste.
- The tagged release workflow now starts the packaged backend JAR against PostgreSQL and requires a healthy `/actuator/health` response before publication.
- Added an automated Recovery Drill that migrates a disposable PostgreSQL source database, runs the repository backup/restore scripts, validates checksum/schema/data restoration, and health-checks the packaged backend against the restored database.
- Upgraded supported GitHub Actions runtime lines across the repository: `actions/checkout@v7`, `actions/setup-node@v7`, `actions/upload-artifact@v7`, `actions/dependency-review-action@v5`, and `softprops/action-gh-release@v3`; `actions/setup-java@v5` remains on its stable production line.
- Disabled persisted checkout credentials in read-only Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, Recovery Drill, and release source checkout; the lockfile bootstrap intentionally retains credentials because it must push the generated lockfile.
- Added `.github/CODEOWNERS` with explicit ownership of CI/release, security, Flyway migration, and recovery-sensitive paths.
- Closed the superseded GitHub Actions Dependabot PRs after absorbing their supported upgrades directly into the hardened workflows.
- Deferred TypeScript 7 and Node 26 type-definition major upgrades from the 2.0.12 stabilization line while continuing to allow normal non-major dependency updates.
- Release documentation and the roadmap now distinguish workflow/configuration evidence from final release-commit evidence.

### Operational hardening

- CSV imports now stream strict UTF-8 request data through a bounded row parser instead of materializing both the complete request body and complete parsed document.
- CSV parser budgets explicitly cap decoded input at 2,000,000 characters, 10,000 rows, 64 columns per row, and 20,000 characters per cell while preserving quoted multiline and CRLF behavior.
- Book/member CSV exports now reject datasets above 10,000 records, use forward-only read-only JDBC streaming with a 250-row fetch window, and write directly to the HTTP response.
- Exported user-controlled CSV cells neutralize spreadsheet formula/command prefixes without rewriting imported values.
- Added parser, service, and controller regression coverage for CSV boundaries, Reader failures, streamed imports/exports, oversized exports, and spreadsheet formula neutralization.

### Fixed

- Fixed strict `exactOptionalPropertyTypes` failures in the frontend API client by representing the optional correlation ID explicitly as `string | undefined` and omitting POST request bodies instead of assigning `body: undefined`.
- Added API regression coverage for optional correlation identifiers.

### Verification evidence

- A temporary same-repository probe completed CodeQL successfully for both Java/Kotlin and JavaScript/TypeScript.
- The hosted lockfile bootstrap successfully generated the npm lockfile and completed `npm ci`; the first executable frontend failure was isolated to two strict TypeScript errors in `frontend/src/api.ts`.
- After those strict TypeScript errors were fixed, the hosted bootstrap rerun completed lockfile generation, reproducible installation, lint, strict typecheck, Vitest, production build, artifact upload, and lockfile commit successfully.
- The real npm-generated `frontend/package-lock.json` is committed in `89d1c833` and is now the canonical resolved frontend dependency graph for 2.0.12.
- Draft PR #11 is the disposable current-source verification harness for Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and Recovery Drill; it must not be merged.

### Release gates

- Observe successful Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and Recovery Drill on the current release-verification source.
- Complete role-based browser smoke and manual accessibility evidence.
- Enable `main` branch protection/code-owner enforcement after stable required-check names are confirmed.
- Tag and publish `v2.0.12` only after every pre-tag gate is closed.

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

- Backend Maven and frontend npm package versions are now `2.0.12`.
- The Settings page derives its displayed web version from build-time package metadata rather than a hard-coded value.
- Frontend CI is read-only and requires a committed lockfile for reproducible `npm ci` verification.
- Release automation validates tag/manifest consistency before packaging.
- Release and backend CI discover the packaged backend JAR without embedding a historical version in workflow source.
- README, release process, and roadmap now describe the 2.0.12 release-candidate line and its remaining gates.

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

> `2.0.12` is prepared in source but must not be treated as a published stable release until the explicit gates in `what_changed.md` and `ROADMAP.md` are closed and `v2.0.12` is tagged.

## [0.1.0] - 2026-08-19

### Added

- Initial LibraCore repository foundation and modular-monolith backend.
- PostgreSQL development environment, initial schema, and core library domain model.
- Core catalog, member, circulation, security, reporting, exchange, and notification modules.

[Unreleased]: https://github.com/sanskarIN/libracore/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/sanskarIN/libracore/releases/tag/v0.1.0
