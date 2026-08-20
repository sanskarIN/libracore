# Changelog

All notable LibraCore changes are recorded here. The project follows Keep a Changelog conventions and uses Semantic Versioning for release identifiers.

## [Unreleased]

### Release hardening

- Fixed the Frontend Lockfile Bootstrap workflow so the first generated, untracked `frontend/package-lock.json` is detected and committed instead of being missed by `git diff --quiet`.
- Preserve the verified generated frontend lockfile as a short-lived Actions artifact before the bootstrap commit step.
- Cancel superseded Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and lockfile-bootstrap runs to reduce stale verification and runner waste.
- The tagged release workflow now starts the packaged backend JAR against PostgreSQL and requires a healthy `/actuator/health` response before publication.
- Upgraded supported GitHub Actions runtime lines across the repository: `actions/checkout@v7`, `actions/setup-node@v7`, `actions/upload-artifact@v7`, `actions/dependency-review-action@v5`, and `softprops/action-gh-release@v3`; `actions/setup-java@v5` remains on its stable production line.
- Closed the superseded GitHub Actions Dependabot PRs after absorbing their supported upgrades directly into the hardened workflows.
- Release documentation now distinguishes workflow/configuration evidence from final release-commit evidence.

### Verification evidence

- A temporary same-repository probe completed CodeQL successfully for both Java/Kotlin and JavaScript/TypeScript.
- A corrected lockfile-bootstrap probe created an observable workflow run, but its GitHub-hosted job remained queued during the diagnostic window; no release claim is made until a real npm-generated lockfile is committed to `main`.
- Frontend dependency PRs remain intentionally unmerged until a committed lockfile enables reproducible frontend CI and dependency review against the exact resolved graph.

### Release gates

- Commit and review `frontend/package-lock.json` before tagging a release.
- Observe successful backend/frontend/version/security CI on the final release-candidate commit.
- Complete clean PostgreSQL migration/startup, backup/restore, role smoke, and accessibility evidence.
- Enable `main` branch protection after stable required-check names are confirmed.

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
- Release verification now refuses to publish without the committed frontend dependency lockfile.

> `2.0.12` is prepared in source but must not be treated as a published stable release until the explicit gates in `what_changed.md` and `ROADMAP.md` are closed and `v2.0.12` is tagged.

## [0.1.0] - 2026-08-19

### Added

- Initial LibraCore repository foundation and modular-monolith backend.
- PostgreSQL development environment, initial schema, and core library domain model.
- Core catalog, member, circulation, security, reporting, exchange, and notification modules.

[Unreleased]: https://github.com/sanskarIN/libracore/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/sanskarIN/libracore/releases/tag/v0.1.0
