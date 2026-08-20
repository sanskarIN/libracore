# Repository Reference

This document describes every tracked source/configuration/documentation file in the LibraCore repository at the 2026-08-20 **2.0.12 release-candidate closure** checkpoint. Directories themselves are organizational and are not listed as files.

## Root files

- `.editorconfig` — cross-editor whitespace/encoding conventions.
- `.env.example` — placeholder-only environment/configuration contract for local/deployment setup.
- `.gitattributes` — Git text/line-ending behavior.
- `.gitignore` — excludes local secrets, build output, IDE state, dependencies, and generated files.
- `CHANGELOG.md` — notable delivered changes, including the 2.0.12 release-candidate line, completed frontend dependency closure, recovery automation, and remaining release gates.
- `CODE_OF_CONDUCT.md` — community participation and enforcement expectations.
- `CONTRIBUTING.md` — contributor setup, verification, engineering, commit, and security rules.
- `LICENSE` — MIT license text.
- `PRIVACY.md` — data categories, local/browser storage, export/backup privacy, retention, and operator responsibilities.
- `README.md` — project overview, current 2.0.12 source status, features, stack, reproducible setup, architecture, quality, support, and documentation navigation.
- `ROADMAP.md` — 2.0.12 release closure plus later operational/product hardening milestones.
- `SECURITY.md` — supported-version posture, private vulnerability reporting, security expectations, and deployment responsibility.
- `SUPPORT.md` — support channels, safe bug-report content, and troubleshooting pointers.
- `THREAT_MODEL.md` — assets, trust boundaries, threats, mitigations, abuse cases, residual risks, and review triggers.
- `compose.yml` — development PostgreSQL service definition.
- `what_changed.md` — canonical cross-session engineering handoff, verification evidence, and exact continuation checkpoint.

## GitHub repository automation and governance

- `.github/CODEOWNERS` — default `@sanskarIN` repository ownership plus explicit ownership of CI/release, security, Flyway migration, and recovery-sensitive paths; enforcement requires branch-protection/ruleset settings.
- `.github/FUNDING.yml` — optional Buy Me a Coffee funding metadata.
- `.github/PULL_REQUEST_TEMPLATE.md` — verification/security/accessibility/migration review checklist for pull requests.
- `.github/dependabot.yml` — weekly Maven, npm, and GitHub Actions dependency updates; defers TypeScript and `@types/node` semver-major updates during the 2.0.12 stabilization line.
- `.github/ISSUE_TEMPLATE/bug_report.yml` — structured, secret-safe bug report form.
- `.github/ISSUE_TEMPLATE/feature_request.yml` — structured feature proposal form with impact considerations.
- `.github/ISSUE_TEMPLATE/config.yml` — issue-form routing to private security reporting and support guidance.
- `.github/workflows/backend-ci.yml` — Java/PostgreSQL backend CI, Maven verification, version-independent packaged-JAR startup, Flyway startup, health check, stale-run cancellation, and non-persisted checkout credentials.
- `.github/workflows/codeql.yml` — scheduled/push/PR CodeQL analysis for Java/Kotlin and JavaScript/TypeScript with stale-run cancellation and non-persisted checkout credentials.
- `.github/workflows/dependency-review.yml` — pull-request dependency-change review for newly introduced vulnerable dependencies with stale-run cancellation and non-persisted checkout credentials.
- `.github/workflows/frontend-ci.yml` — read-only reproducible frontend quality gate that requires the committed npm lockfile, uses `npm ci`, verifies manifest versions, runs lint/typecheck/tests/build through the aggregate frontend check, cancels stale runs, and does not persist checkout credentials.
- `.github/workflows/lockfile-bootstrap.yml` — explicit maintainer-triggered workflow that generates the npm lockfile, verifies installation, preserves the generated lock as a short-lived artifact, runs the full frontend gate, detects first-time/untracked locks correctly, and commits only the generated `frontend/package-lock.json` when necessary.
- `.github/workflows/recovery-drill.yml` — automated disposable PostgreSQL recovery test that packages/migrates the backend, invokes the real backup/restore scripts, verifies checksum/migration history/fictional restored data, and health-checks the packaged backend against the restored database; also supports scheduled/manual execution.
- `.github/workflows/release.yml` — tagged release verification; requires the committed lockfile, checks tag/manifest version agreement, verifies/packages backend and frontend, starts and health-checks the exact packaged backend against PostgreSQL, writes checksums, and publishes the GitHub release with non-persisted checkout credentials.
- `.github/workflows/version-sync.yml` — lightweight CI guard that checks backend/frontend executable version synchronization with stale-run cancellation and non-persisted checkout credentials.

## Backend build

- `backend/pom.xml` — Maven project metadata, 2.0.12 backend version, Java/Spring Boot dependencies/plugins, and backend build/test configuration.

## Backend application entry point

- `backend/src/main/java/com/sanskar/libracore/LibraCoreApplication.java` — Spring Boot application bootstrap.

## Backend audit module

- `backend/src/main/java/com/sanskar/libracore/audit/AuditService.java` — append-oriented success/failure operational audit recording used by domain/security services.

## Backend catalog module

- `backend/src/main/java/com/sanskar/libracore/catalog/CatalogController.java` — catalog/branch/shelf/book/copy HTTP endpoints and authorization boundaries.
- `backend/src/main/java/com/sanskar/libracore/catalog/CatalogModels.java` — catalog request/response records and validation contracts.
- `backend/src/main/java/com/sanskar/libracore/catalog/CatalogService.java` — catalog search, metadata management, physical-copy/branch/shelf persistence, lookup, and availability rules.
- `backend/src/main/java/com/sanskar/libracore/catalog/Isbn13.java` — ISBN-13 normalization/validation helper.

## Backend circulation and fine module

- `backend/src/main/java/com/sanskar/libracore/circulation/CirculationController.java` — issue/return/renew/reservation/policy HTTP endpoints.
- `backend/src/main/java/com/sanskar/libracore/circulation/CirculationModels.java` — loan, return, reservation, page, and circulation-policy API records.
- `backend/src/main/java/com/sanskar/libracore/circulation/CirculationService.java` — transactional issue/return/renew, reservation/waitlist, copy state, promotion, ownership, and policy behavior.
- `backend/src/main/java/com/sanskar/libracore/circulation/FineController.java` — member/staff fine reads and settlement endpoint.
- `backend/src/main/java/com/sanskar/libracore/circulation/FineModels.java` — fine/settlement response and request records.
- `backend/src/main/java/com/sanskar/libracore/circulation/FinePolicyService.java` — fine calculation and circulation policy evaluation.
- `backend/src/main/java/com/sanskar/libracore/circulation/FineService.java` — fine query/settlement persistence and authorization/audit integration.

## Backend common module

- `backend/src/main/java/com/sanskar/libracore/common/ApiError.java` — stable public API error shape.
- `backend/src/main/java/com/sanskar/libracore/common/ApiException.java` — explicit domain/HTTP-safe application exception helper.
- `backend/src/main/java/com/sanskar/libracore/common/GlobalExceptionHandler.java` — validation/domain/unexpected exception mapping to safe API errors.
- `backend/src/main/java/com/sanskar/libracore/common/TextNormalizer.java` — shared input normalization utility.

## Backend data-exchange module

- `backend/src/main/java/com/sanskar/libracore/exchange/CsvCodec.java` — deterministic CSV parsing/encoding utility.
- `backend/src/main/java/com/sanskar/libracore/exchange/DataExchangeController.java` — authorized book/member CSV import/export HTTP endpoints.
- `backend/src/main/java/com/sanskar/libracore/exchange/DataExchangeModels.java` — CSV import result model.
- `backend/src/main/java/com/sanskar/libracore/exchange/DataExchangeService.java` — bounded/validated catalog and member CSV exchange logic.

## Backend member module

- `backend/src/main/java/com/sanskar/libracore/member/MemberController.java` — staff member CRUD/search/account-link endpoints plus member self-view.
- `backend/src/main/java/com/sanskar/libracore/member/MemberModels.java` — member API records and validation contracts.
- `backend/src/main/java/com/sanskar/libracore/member/MemberService.java` — member lifecycle, search, card/account linking, ownership checks, persistence, and audit behavior.

## Backend notification module

- `backend/src/main/java/com/sanskar/libracore/notification/MockNotificationGateway.java` — safe development notification sink.
- `backend/src/main/java/com/sanskar/libracore/notification/NotificationGateway.java` — outbound notification adapter contract.
- `backend/src/main/java/com/sanskar/libracore/notification/NotificationSchedulingConfig.java` — notification scheduling enablement/configuration.
- `backend/src/main/java/com/sanskar/libracore/notification/NotificationService.java` — due/overdue notification orchestration and durable delivery-state management.
- `backend/src/main/java/com/sanskar/libracore/notification/SmtpNotificationGateway.java` — configured SMTP delivery adapter.

## Backend reporting module

- `backend/src/main/java/com/sanskar/libracore/reporting/ReportingController.java` — dashboard, overdue, and audit-report HTTP endpoints.
- `backend/src/main/java/com/sanskar/libracore/reporting/ReportingModels.java` — dashboard/overdue/audit response models.
- `backend/src/main/java/com/sanskar/libracore/reporting/ReportingService.java` — bounded operational metrics, overdue queries, and administrator audit search.

## Backend security module

- `backend/src/main/java/com/sanskar/libracore/security/AdminBootstrap.java` — optional explicitly configured first administrator provisioning.
- `backend/src/main/java/com/sanskar/libracore/security/AdminUserController.java` — administrator-only staff-account list/create/enable/password-reset HTTP endpoints.
- `backend/src/main/java/com/sanskar/libracore/security/AdminUserModels.java` — validated staff-account admin request/response records.
- `backend/src/main/java/com/sanskar/libracore/security/AdminUserService.java` — staff account lifecycle, password hashing, session revocation, self/last-admin safeguards, and audit integration.
- `backend/src/main/java/com/sanskar/libracore/security/AppPrincipal.java` — authenticated application identity/role representation.
- `backend/src/main/java/com/sanskar/libracore/security/AuthController.java` — login/current-user/logout HTTP endpoints.
- `backend/src/main/java/com/sanskar/libracore/security/BearerSessionFilter.java` — bearer-token extraction and server-side session authentication filter.
- `backend/src/main/java/com/sanskar/libracore/security/SecurityConfig.java` — Spring Security, password hashing, authorization, session policy, and CORS configuration.
- `backend/src/main/java/com/sanskar/libracore/security/SessionTokenService.java` — opaque token generation, hashing, persistence, expiry/revocation, and authenticated session lookup.

## Backend runtime resources

- `backend/src/main/resources/application.yml` — application defaults/environment binding for DB, sessions, CORS, bootstrap admin, notification/SMTP, scheduling, and actuator settings.

## Flyway schema history

- `backend/src/main/resources/db/migration/V1__initial_schema.sql` — initial normalized library/security/circulation/audit schema, constraints, and indexes.
- `backend/src/main/resources/db/migration/V2__reservation_copy_assignment.sql` — reservation-to-copy assignment support.
- `backend/src/main/resources/db/migration/V3__author_identity_index.sql` — author identity/index hardening.
- `backend/src/main/resources/db/migration/V4__circulation_policy_snapshot.sql` — loan/circulation policy snapshot fields.
- `backend/src/main/resources/db/migration/V5__notification_message_fields.sql` — notification message persistence fields.
- `backend/src/main/resources/db/migration/V6__fine_settlement_audit.sql` — fine settlement audit-support schema change.

## Backend tests

- `backend/src/test/java/com/sanskar/libracore/catalog/Isbn13Test.java` — valid/invalid ISBN-13 normalization/check-digit behavior.
- `backend/src/test/java/com/sanskar/libracore/circulation/FinePolicyServiceTest.java` — overdue/grace/cap policy calculation coverage.
- `backend/src/test/java/com/sanskar/libracore/common/TextNormalizerTest.java` — shared normalization behavior.
- `backend/src/test/java/com/sanskar/libracore/exchange/CsvCodecTest.java` — CSV escaping/parsing regression coverage.

## Frontend root/build files

- `frontend/index.html` — Vite HTML entry document and application mount node.
- `frontend/package.json` — 2.0.12 frontend version, exact direct dependency/tool versions, Node engine range, and dev/build/lint/typecheck/test/check scripts.
- `frontend/package-lock.json` — npm lockfile version 3 generated by the hosted Node/npm bootstrap and committed as the canonical exact resolved dependency graph for the 2.0.12 frontend.
- `frontend/tsconfig.json` — TypeScript project-reference root.
- `frontend/tsconfig.app.json` — strict browser/React TypeScript compiler settings including `exactOptionalPropertyTypes`.
- `frontend/tsconfig.node.json` — strict Node/Vite configuration compiler settings.
- `frontend/vite.config.ts` — React/Vite/Vitest configuration and build-time injection of the version read from `frontend/package.json`.
- `frontend/public/logo.svg` — application/logo asset used by sign-in and shell navigation.

## Frontend application core

- `frontend/src/main.tsx` — React DOM mount and global stylesheet import.
- `frontend/src/App.tsx` — authenticated application composition, API/session/theme integration, role-safe routing, and page dispatch.
- `frontend/src/api.ts` — timeout-aware authenticated JSON/CSV HTTP client and safe API-error handling; optional request/error data is represented compatibly with strict optional-property typing.
- `frontend/src/api.test.ts` — API base URL/error utility regression coverage including absent/present API correlation identifiers.
- `frontend/src/copy.ts` — centralized recurring application/navigation/contact strings.
- `frontend/src/env.d.ts` — browser compile-time declaration for the Vite-injected application version constant.
- `frontend/src/format.ts` — date/time/money formatting helpers.
- `frontend/src/format.test.ts` — formatting helper tests.
- `frontend/src/routes.ts` — typed hash-route parsing and navigation URLs.
- `frontend/src/session.ts` — browser session persistence/expiry validation/cleanup.
- `frontend/src/session.test.ts` — session persistence/expiry/corruption tests.
- `frontend/src/styles.css` — responsive design system, themes, forms, tables, states, navigation, accessibility/focus, and mobile layout.
- `frontend/src/theme.ts` — light/dark/system preference persistence/application.
- `frontend/src/theme.test.ts` — theme preference/apply behavior tests.
- `frontend/src/types.ts` — shared typed HTTP/domain contracts for auth, catalog, members, loans, reservations, fines, reports, audit, and staff administration.

## Frontend shared components

- `frontend/src/components/AppShell.tsx` — responsive desktop/mobile role-aware navigation, account context, sign-out, and project footer/watermark.
- `frontend/src/components/MetricCard.tsx` — reusable dashboard/report metric card.
- `frontend/src/components/PageHeader.tsx` — consistent feature-page heading/description/action region.
- `frontend/src/components/StatePanel.tsx` — reusable loading/empty/error/offline-style state presentation.
- `frontend/src/components/StatusBadge.tsx` — textual status indicator with semantic style classes.

## Frontend feature pages

- `frontend/src/pages/LoginPage.tsx` — accessible credential sign-in view; explicit-undefined error prop is compatible with strict optional-property typing.
- `frontend/src/pages/DashboardPage.tsx` — role-specific operational/member dashboard summaries and current-state views.
- `frontend/src/pages/CatalogPage.tsx` — search/filter/detail/copy/catalog administration workflows.
- `frontend/src/pages/MembersPage.tsx` — member search, create/update/status/account-link workflows.
- `frontend/src/pages/CirculationPage.tsx` — member/copy lookup, issue/return/renew, fine settlement, and policy administration workflows.
- `frontend/src/pages/ReservationsPage.tsx` — member self-service and staff reservation/waitlist creation/cancellation/inspection.
- `frontend/src/pages/ReportsPage.tsx` — dashboard metrics, overdue/audit views, and CSV import/export controls.
- `frontend/src/pages/AdminUsersPage.tsx` — administrator staff-account provisioning, role filtering, enable/disable, password reset, and session-count visibility.
- `frontend/src/pages/SettingsPage.tsx` — theme, account/security, accessibility, privacy, build-derived version/release channel, about, contact, and funding information.

## Operations and release-support scripts

- `scripts/backup.sh` — environment-driven PostgreSQL custom-format logical backup helper with no-owner/no-ACL output, overwrite refusal, non-empty validation, SHA-256 checksum generation, and restrictive file permissions.
- `scripts/check-version.mjs` — dependency-free cross-manifest version guard; optionally validates an expected tag/version such as `v2.0.12`.
- `scripts/restore.sh` — explicit destructive-opt-in PostgreSQL restore helper with optional adjacent checksum validation, single-transaction/exit-on-error restore, and no-owner/no-ACL behavior.

## Documentation

- `docs/accessibility.md` — accessibility requirements and manual release audit.
- `docs/api.md` — endpoint families, auth/error/pagination conventions, and source-of-truth guidance.
- `docs/architecture.md` — system/module/data/auth/configuration architecture.
- `docs/backup-restore.md` — backup metadata, automated disposable Recovery Drill, environment-specific manual restore drill, safeguards, migration compatibility, and RPO/RTO guidance.
- `docs/branch-protection.md` — recommended `main` ruleset, code-owner review, required checks including Recovery Drill where relevant, bypass, lockfile state, and tag-protection guidance.
- `docs/deployment.md` — production topology, secrets/TLS/CORS/DB/migration/proxy/health/scale boundaries.
- `docs/development.md` — backend/frontend/database/config/error/logging/commit development workflow.
- `docs/performance.md` — budgets, measurement method, database/frontend/load/regression guidance.
- `docs/release.md` — 2.0.12-aware reproducible pre-release verification, committed-lockfile state, least-privilege checkout behavior, version/tag/artifact/migration/rollback/release-note process.
- `docs/releases/2.0.12.md` — dedicated 2.0.12 release-candidate scope, completed frontend dependency closure, hardening changes, live verification harness, and exact pre-tag checklist.
- `docs/setup.md` — clean checkout prerequisites, database/backend/frontend startup using the committed lockfile, dependency-lock maintenance, full verification, and reset notes.
- `docs/testing.md` — test layers, committed-lockfile quality gates, hosted frontend evidence, determinism, regression, accessibility/security checks, and current limitations.
- `docs/troubleshooting.md` — database, migration, auth, CORS, npm, TypeScript, circulation, staff-admin, CSV, notification, and recovery diagnosis.
- `docs/adr/0001-modular-monolith.md` — decision to keep strong library transactions in a modular monolith.
- `docs/adr/0002-postgresql-flyway.md` — PostgreSQL + append-only Flyway schema-history decision.
- `docs/adr/0003-opaque-bearer-sessions.md` — revocable server-side opaque bearer session decision.
- `docs/repository-reference.md` — this exhaustive tracked-file reference.

## Maintenance rule

When adding, renaming, or deleting a tracked file, update this reference in the same documentation pass when the file changes repository structure, public behavior, operations, security, release behavior, or continuity. Generated build output, IDE state, `node_modules`, local `.env`, secrets, database volumes, temporary CI probe markers/branches, and other intentionally ignored or unmerged files do not belong here.
