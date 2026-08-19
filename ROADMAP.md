# LibraCore Roadmap

The roadmap describes direction, not a promise that an unchecked item already exists. `what_changed.md` is the implementation handoff and `CHANGELOG.md` records delivered behavior.

## 0.1.x — Foundation and end-to-end MVP

- [x] Modular Spring Boot backend and PostgreSQL/Flyway persistence.
- [x] Catalog, copies, branches, shelves, authors, publishers, categories, and search.
- [x] Members, accounts, roles, authentication, authorization, and administrator staff-account management.
- [x] Issue, return, renewal, reservations/waitlists, configurable fine policies, and settlements.
- [x] Dashboard, overdue reporting, audit views, CSV exchange, notifications, and backup scripts.
- [x] Responsive React/TypeScript client with dark/light/system themes and role-complete mobile navigation.
- [x] Core backend/frontend tests and CI foundations.
- [x] Security, privacy, architecture, setup, testing, operations, release, and exhaustive repository-reference documentation.
- [ ] Generate and commit a synchronized `frontend/package-lock.json`, then make `npm ci` the canonical clean-checkout install.
- [ ] Observe successful backend/frontend/security CI on the final release-candidate commit and fix every failure.
- [ ] Enable and verify `main` branch protection/rules using the stable required-check names.
- [ ] Complete release-candidate verification from a clean checkout and close every resulting blocker.

## 0.2.x — Operational hardening

- [ ] Expand database-backed integration tests for role and ownership boundaries.
- [ ] Add browser-level end-to-end tests for sign-in, catalog, circulation, reservation, staff-account, and staff-report journeys.
- [ ] Add automated accessibility scanning plus manual keyboard/screen-reader evidence.
- [ ] Add rate-limit guidance/reference configuration for internet-facing deployments.
- [ ] Add import/export edge-case and spreadsheet-formula safety regression coverage.
- [ ] Add repeatable performance fixtures for large catalog/member/circulation datasets.
- [ ] Add restore-drill automation against disposable databases.

## 0.3.x — Product maturity

- [ ] Internationalization runtime and first additional locale after English strings are fully externalized.
- [ ] PWA/offline-safe shell for read-oriented screens where correctness permits caching.
- [ ] Improved operator onboarding and first-run branch/policy configuration.
- [ ] Saved filters and productivity shortcuts for high-volume staff workflows.
- [ ] Deployment examples for a reverse proxy and containerized production topology.

## 1.0.0 — Stable release criteria

- [ ] No known blocker/critical defects.
- [ ] Clean checkout build, lint, typecheck, tests, migrations, and security analysis all pass.
- [ ] Primary journeys have deterministic end-to-end coverage.
- [ ] Upgrade and rollback procedure is validated.
- [ ] Backup restore is demonstrated on release artifacts.
- [ ] Accessibility and performance budgets have recorded evidence.
- [ ] Public API/configuration compatibility commitments are documented.
- [ ] Release artifacts and checksums are reproducible and published.

## Non-goals until justified

LibraCore will not introduce microservices, payment processing, public anonymous member registration, third-party analytics, or multi-tenant SaaS complexity merely to increase feature count. Those changes require a new threat-model and architecture review first.
