# LibraCore Roadmap

The roadmap describes direction, not a promise that an unchecked item already exists. `what_changed.md` is the implementation handoff and `CHANGELOG.md` records delivered behavior.

## 2.0.12 — Release-candidate closure

- [x] Modular Spring Boot backend and PostgreSQL/Flyway persistence.
- [x] Catalog, copies, branches, shelves, authors, publishers, categories, and search.
- [x] Members, accounts, roles, authentication, authorization, and administrator staff-account management.
- [x] Issue, return, renewal, reservations/waitlists, configurable fine policies, and settlements.
- [x] Dashboard, overdue reporting, audit views, CSV exchange, notifications, and backup scripts.
- [x] Responsive React/TypeScript client with dark/light/system themes and role-complete mobile navigation.
- [x] Core backend/frontend tests and CI foundations.
- [x] Security, privacy, architecture, setup, testing, operations, release, and exhaustive repository-reference documentation.
- [x] Align backend/frontend executable manifests to `2.0.12`.
- [x] Display the frontend package version from build metadata instead of a hard-coded Settings value.
- [x] Add a cross-manifest version guard and GitHub Actions version-sync check.
- [x] Remove historical hard-coded backend JAR filenames from backend CI and release packaging.
- [x] Make release automation reject a tag that disagrees with executable manifest versions.
- [x] Make release automation require a committed frontend lockfile and `npm ci`.
- [x] Separate lockfile generation into an explicit maintainer bootstrap workflow; keep normal frontend CI read-only.
- [ ] Generate, review, and commit the synchronized `frontend/package-lock.json` using the supported Node/npm toolchain.
- [ ] Observe successful backend/frontend/version/security CI on the final 2.0.12 commit and fix every failure.
- [ ] Run clean PostgreSQL migration/startup/health verification and a backup/restore drill.
- [ ] Complete role-based browser smoke testing and accessibility evidence.
- [ ] Enable and verify `main` branch protection/rules using the stable required-check names.
- [ ] Tag `v2.0.12` only after every release gate above is closed.

## 2.1.x — Operational hardening

- [ ] Expand database-backed integration tests for role and ownership boundaries.
- [ ] Add browser-level end-to-end tests for sign-in, catalog, circulation, reservation, staff-account, and staff-report journeys.
- [ ] Add automated accessibility scanning plus manual keyboard/screen-reader evidence.
- [ ] Add rate-limit guidance/reference configuration for internet-facing deployments.
- [ ] Add import/export edge-case and spreadsheet-formula safety regression coverage.
- [ ] Add repeatable performance fixtures for large catalog/member/circulation datasets.
- [ ] Add restore-drill automation against disposable databases.

## 2.2.x — Product maturity

- [ ] Internationalization runtime and first additional locale after English strings are fully externalized.
- [ ] PWA/offline-safe shell for read-oriented screens where correctness permits caching.
- [ ] Improved operator onboarding and first-run branch/policy configuration.
- [ ] Saved filters and productivity shortcuts for high-volume staff workflows.
- [ ] Deployment examples for a reverse proxy and containerized production topology.

## Stable release quality criteria

- [ ] No known blocker/critical defects.
- [ ] Clean checkout build, lint, typecheck, tests, migrations, and security analysis all pass.
- [ ] Primary journeys have deterministic end-to-end coverage or recorded release smoke evidence.
- [ ] Upgrade and rollback procedure is validated.
- [ ] Backup restore is demonstrated on release artifacts or the exact release schema.
- [ ] Accessibility and performance budgets have recorded evidence.
- [ ] Public API/configuration compatibility commitments are documented.
- [ ] Release artifacts and checksums are reproducible and published.

## Non-goals until justified

LibraCore will not introduce microservices, payment processing, public anonymous member registration, third-party analytics, or multi-tenant SaaS complexity merely to increase feature count. Those changes require a new threat-model and architecture review first.
