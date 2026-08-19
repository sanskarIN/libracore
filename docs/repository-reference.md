# Repository Reference

This document describes every tracked source/configuration/documentation file in the LibraCore repository at the 2026-08-19 final-audit checkpoint. Directories themselves are organizational and are not listed as files.

## Root files

- `.editorconfig` — cross-editor whitespace/encoding conventions.
- `.env.example` — placeholder-only environment/configuration contract for local/deployment setup.
- `.gitattributes` — Git text/line-ending behavior.
- `.gitignore` — excludes local secrets, build output, IDE state, dependencies, and other generated files.
- `CHANGELOG.md` — notable delivered changes by release/development line.
- `CODE_OF_CONDUCT.md` — community participation and enforcement expectations.
- `CONTRIBUTING.md` — contributor setup, verification, engineering, commit, and security rules.
- `LICENSE` — MIT license text.
- `PRIVACY.md` — data categories, local/browser storage, export/backup privacy, retention, and operator responsibilities.
- `README.md` — project overview, features, current stack, setup, architecture, quality, support, and navigation into deeper docs.
- `ROADMAP.md` — future hardening/product milestones and stable-release criteria.
- `SECURITY.md` — supported-version posture, private vulnerability reporting, security expectations, and deployment responsibility.
- `SUPPORT.md` — support channels, safe bug-report content, and troubleshooting pointers.
- `THREAT_MODEL.md` — assets, trust boundaries, threats, mitigations, abuse cases, residual risks, and review triggers.
- `compose.yml` — development PostgreSQL service definition.
- `what_changed.md` — final/cross-session engineering handoff and verification checkpoint; maintained after this reference.

## GitHub repository automation and governance

- `.github/FUNDING.yml` — optional Buy Me a Coffee funding metadata.
- `.github/PULL_REQUEST_TEMPLATE.md` — verification/security/accessibility/migration review checklist for pull requests.
- `.github/dependabot.yml` — weekly Maven, npm, and GitHub Actions dependency update configuration.
- `.github/ISSUE_TEMPLATE/bug_report.yml` — structured, secret-safe bug report form.
- `.github/ISSUE_TEMPLATE/feature_request.yml` — structured feature proposal form with impact considerations.
- `.github/ISSUE_TEMPLATE/config.yml` — issue-form routing to private security reporting and support guidance.
- `.github/workflows/backend-ci.yml` — Java/PostgreSQL backend CI, Maven verification, packaged startup, and health check.
- `.github/workflows/frontend-ci.yml` — Node frontend gate; currently bootstraps a lockfile, installs dependencies, then runs lint/typecheck/tests/build and can commit the generated lock on `main` when Actions permissions allow.
- `.github/workflows/codeql.yml` — scheduled/push/PR CodeQL analysis for Java and JavaScript/TypeScript.
- `.github/workflows/release.yml` — tagged release verification, backend/frontend packaging, checksums, and GitHub Release publication.

## Backend build

- `backend/pom.xml` — Maven project metadata, Java/Spring Boot dependencies/plugins, and backend build/test configuration.

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
- `frontend/package.json` — exact direct dependency/tool versions, Node engine range, and dev/build/lint/typecheck/test/check scripts.
- `frontend/tsconfig.json` — TypeScript project-reference root.
- `frontend/tsconfig.app.json` — strict browser/React TypeScript compiler settings.
- `frontend/tsconfig.node.json` — strict Node/Vite configuration compiler settings.
- `frontend/vite.config.ts` — React/Vite configuration and Vitest environment settings.
- `frontend/public/logo.svg` — application/logo asset used by sign-in and shell navigation.

`frontend/package-lock.json` is intentionally **not listed as tracked** at this checkpoint because it does not yet exist on `main`; that is a documented release-hardening limitation rather than an omitted file in this reference.

## Frontend application core

- `frontend/src/main.tsx` — React DOM mount and global stylesheet import.
- `frontend/src/App.tsx` — authenticated application composition, API/session/theme integration, role-safe routing, and page dispatch.
- `frontend/src/api.ts` — timeout-aware authenticated JSON/CSV HTTP client and safe API-error handling.
- `frontend/src/api.test.ts` — API base URL/error utility regression coverage.
- `frontend/src/copy.ts` — centralized recurring application/navigation/contact strings.
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
- `frontend/src/pages/SettingsPage.tsx` — theme, account/security, accessibility, privacy, updates, about, contact, and funding information.

## Operations scripts

- `scripts/backup.sh` — environment-driven PostgreSQL backup helper with safe shell behavior.
- `scripts/restore.sh` — explicit PostgreSQL restore helper intended for controlled recovery/drill use.

## Documentation

- `docs/accessibility.md` — accessibility requirements and manual release audit.
- `docs/api.md` — endpoint families, auth/error/pagination conventions, and source-of-truth guidance.
- `docs/architecture.md` — system/module/data/auth/configuration architecture.
- `docs/backup-restore.md` — backup metadata, restore drill, safeguards, migration compatibility, RPO/RTO guidance.
- `docs/branch-protection.md` — recommended `main` ruleset, required checks, bypass, and tag-protection guidance.
- `docs/deployment.md` — production topology, secrets/TLS/CORS/DB/migration/proxy/health/scale boundaries.
- `docs/development.md` — backend/frontend/database/config/error/logging/commit development workflow.
- `docs/performance.md` — budgets, measurement method, database/frontend/load/regression guidance.
- `docs/release.md` — honest pre-release verification, version/tag/artifact/migration/rollback/release-note process.
- `docs/setup.md` — clean checkout prerequisites, database/backend/frontend startup, full verification, reset notes.
- `docs/testing.md` — test layers, quality gates, determinism, regression, accessibility/security checks, known limitations.
- `docs/troubleshooting.md` — database, migration, auth, CORS, npm, TypeScript, circulation, staff-admin, CSV, notification, and recovery diagnosis.
- `docs/adr/0001-modular-monolith.md` — decision to keep strong library transactions in a modular monolith.
- `docs/adr/0002-postgresql-flyway.md` — PostgreSQL + append-only Flyway schema-history decision.
- `docs/adr/0003-opaque-bearer-sessions.md` — revocable server-side opaque bearer session decision.
- `docs/repository-reference.md` — this all-files reference.

## Maintenance rule

When adding, renaming, or deleting a tracked file, update this reference in the same documentation pass when the file changes repository structure, public behavior, operations, security, or continuity. Generated build output, IDE state, `node_modules`, local `.env`, secrets, database volumes, and other intentionally ignored files do not belong here.
