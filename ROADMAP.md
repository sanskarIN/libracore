# LibraCore Roadmap

The roadmap describes direction, not a promise that an unchecked item already exists. `what_changed.md` is the implementation handoff and `CHANGELOG.md` records delivered behavior.

## 1.1.0 — Release-candidate closure

- [x] Modular Spring Boot backend and PostgreSQL/Flyway persistence.
- [x] Catalog, copies, branches, shelves, authors, publishers, categories, and search.
- [x] Members, accounts, roles, authentication, authorization, and administrator staff-account management.
- [x] Issue, return, renewal, reservations/waitlists, configurable fine policies, and settlements.
- [x] Dashboard, overdue reporting, audit views, CSV exchange, notifications, and backup scripts.
- [x] Responsive React/TypeScript client with dark/light/system themes and role-complete mobile navigation.
- [x] Core backend/frontend tests and CI foundations.
- [x] Security, privacy, architecture, setup, testing, operations, release, and exhaustive repository-reference documentation.
- [x] Rebaseline backend/frontend executable manifests to `1.1.0`.
- [x] Keep build-derived frontend version display and the cross-manifest version guard.
- [x] Keep release automation fail-closed on tag/manifest mismatch and missing lockfile.
- [x] Keep packaged-backend PostgreSQL startup and `/actuator/health` verification in release automation.
- [x] Keep the automated disposable PostgreSQL Recovery Drill and backup/restore verification.
- [ ] Verify/regenerate the committed `frontend/package-lock.json` with `1.1.0` root metadata using the supported Node/npm toolchain.
- [ ] Observe successful Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and Recovery Drill on the exact release source; fix every failure.
- [ ] Complete role-based browser smoke testing and accessibility evidence.
- [ ] Enable and verify `main` branch protection/rules using stable required-check names, or document the host-level limitation if unavailable.
- [ ] Tag and publish `v1.1.0` only after every release gate above is closed.

## Historical 2.0.12 engineering line

The repository previously used `2.0.12` as a release-candidate engineering line. Its implementation work is retained in Git history and changelog documentation, but it is no longer the active release target. Do not create or publish `v2.0.12` as part of the current release pass.

## Historical 0.1.1 rebaseline attempt

A temporary `0.1.1` release-rebaseline pass was superseded after confirming that the intended post-`v1.0.0` release sequence is `v1.1.0`. The temporary release-preparation commits remain in Git history as auditable work; they are not the release target and must not be tagged as `v0.1.1`.

## 2.1.x — Operational hardening

- Expand observability and operational diagnostics.
- Strengthen performance measurement and representative load testing.
- Extend branch-level workflows and reporting.
- Improve deployment automation and environment-specific configuration validation.

## Future

- Additional library integrations and interoperability.
- Expanded analytics and reporting.
- Further accessibility and internationalization improvements.
- Optional service decomposition only when operational boundaries justify it.
