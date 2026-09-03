# Changelog

All notable LibraCore changes are recorded here. The project follows Keep a Changelog conventions and uses Semantic Versioning for release identifiers.

## [Unreleased]

### Release hardening

- Rebased the executable frontend/backend release manifests onto the `1.1.0` release line.
- Retained the committed npm-generated frontend lockfile as the canonical dependency graph; verify/regenerate it with the supported npm toolchain when dependency metadata changes.
- Keep release publication fail-closed on version mismatch, missing lockfile, backend verification, packaged-service health, frontend quality, security, and recovery evidence.
- Continue using the automated PostgreSQL Recovery Drill to validate the real backup/restore path and packaged backend startup against restored data.

### Release gates

- Complete current Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and Recovery Drill evidence on the intended release source.
- Complete role-based browser smoke and manual accessibility evidence.
- Enable `main` branch protection/code-owner enforcement after stable required-check names are confirmed.
- Create and publish `v1.1.0` only after every release-blocking gate is closed.

## [1.1.0] - 2026-09-03 — release candidate

### Changed

- Established `1.1.0` as the intended release line following the existing `v1.0.0` tag.
- Rebased the backend Maven version to `1.1.0`.
- Rebased the frontend npm package version to `1.1.0`.
- Updated the README, roadmap, and release process to use `v1.1.0`.
- Preserved the previous `0.1.1` preparation as historical repository work rather than a release target.

### Verification

- The release version guard remains responsible for validating frontend/backend manifest agreement and optional tag agreement.
- The final `v1.1.0` source must still pass the complete tag-triggered release workflow before publication.
- Current CI and Recovery Drill results must be evaluated from the exact final release source rather than inferred from historical runs.

### Release status

- `v1.1.0` is a release candidate until the complete release gates pass.
- No claim of a published stable `v1.1.0` release should be made until the GitHub release workflow succeeds.

## Historical 2.0.12 engineering line

The repository previously used `2.0.12` as a release-candidate engineering line. Its implementation work is retained in Git history and documentation, but it is no longer the active release target.

## Historical 0.1.1 preparation

A temporary `0.1.1` release-rebaseline pass was superseded after confirming that the intended post-`v1.0.0` release sequence is `v1.1.0`. Those preparation commits remain in Git history for auditability and are not a published release.

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
- Opaque bearer-session authentication and server-side role authorization.
- Backup and restore scripts.
- Light/dark/system appearance support and responsive accessibility-oriented design system.
- Frontend utility/unit tests and backend domain/parser tests.
- Backend/frontend GitHub Actions quality gates, CodeQL, pull-request dependency review, Dependabot, tagged release automation, and version synchronization automation.
- Security, privacy, threat-model, support, contribution, operations, architecture, testing, release, branch-protection, and exhaustive repository-reference documentation.
