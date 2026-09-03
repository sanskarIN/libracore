# LibraCore Roadmap

The roadmap describes direction, not a promise that an unchecked item already exists. `what_changed.md` is the implementation handoff and `CHANGELOG.md` records delivered behavior.

## 0.1.1 — Release-candidate closure

- [x] Modular Spring Boot backend and PostgreSQL/Flyway persistence.
- [x] Catalog, copies, branches, shelves, authors, publishers, categories, and search.
- [x] Members, accounts, roles, authentication, authorization, and administrator staff-account management.
- [x] Issue, return, renewal, reservations/waitlists, configurable fine policies, and settlements.
- [x] Dashboard, overdue reporting, audit views, CSV exchange, notifications, and backup scripts.
- [x] Responsive React/TypeScript client with dark/light/system themes and role-complete mobile navigation.
- [x] Core backend/frontend tests and CI foundations.
- [x] Security, privacy, architecture, setup, testing, operations, release, and exhaustive repository-reference documentation.
- [x] Rebaseline backend/frontend executable manifests to `0.1.1`.
- [x] Keep build-derived frontend version display and the cross-manifest version guard.
- [x] Keep release automation fail-closed on tag/manifest mismatch and missing lockfile.
- [x] Keep packaged-backend PostgreSQL startup and `/actuator/health` verification in release automation.
- [x] Keep the automated disposable PostgreSQL Recovery Drill and backup/restore verification.
- [ ] Regenerate and commit `frontend/package-lock.json` with `0.1.1` root metadata using the supported Node/npm toolchain.
- [ ] Observe successful Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and Recovery Drill on the exact release source; fix every failure.
- [ ] Complete role-based browser smoke testing and accessibility evidence.
- [ ] Enable and verify `main` branch protection/rules using stable required-check names, or document the host-level limitation if unavailable.
- [ ] Tag and publish `v0.1.1` only after every release gate above is closed.

## Historical 2.0.12 engineering line

The repository previously used `2.0.12` as a release-candidate engineering line. Its implementation work is retained in Git history and changelog documentation, but it is no longer the active release target. Do not create or publish `v2.0.12` as part of the current release pass.

## 2.1.x — Operational hardening

- [ ] Expand database-backed integration tests for role and ownership boundaries.
- [ ] Add browser-level end-to-end tests for sign-in, catalog, circulation, reservation, staff-account, and staff-report journeys.
- [ ] Add automated accessibility scanning plus manual keyboard/screen-reader evidence.
- [ ] Add rate-limit guidance/reference configuration for internet-facing deployments.
- [ ] Add import/export edge-case and spreadsheet-formula safety regression coverage.
- [ ] Add repeatable performance fixtures for large catalog/member/circulation datasets.
- [x] Add restore-drill automation against disposable databases.

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
