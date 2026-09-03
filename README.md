# LibraCore

**Production-minded, open-source library management for cataloging, circulation, members, branches, policy, reporting, and auditability.**

> Current release candidate: **1.1.0**

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-sanskarIN-FFDD00?logo=buy-me-a-coffee&logoColor=000000)](https://buymeacoffee.com/sanskarIN)

**Made by the Sanskar**

## Why LibraCore

LibraCore is a modular-monolith Library Management System intended to grow beyond a classroom CRUD demo. The repository is designed around real library workflows: catalog metadata, physical copies, members, circulation, configurable fine rules, reservations/waitlists, branch-aware inventory, audit trails, imports/exports, notifications, reporting, security, accessibility, and recoverable operations.

The project intentionally favors explicit business rules, database migrations, documented architecture decisions, strict validation, deterministic tests, and small reviewable modules over premature microservices.

## Status

The executable backend and frontend manifests are aligned to **1.1.0**. The committed `frontend/package-lock.json` is the canonical generated dependency graph and must remain synchronized with the frontend manifest. `what_changed.md` is the canonical continuation/handoff document and records completed work, verification evidence, limitations, recent commits, and the next exact release tasks. A version value in source does not by itself mean that the corresponding GitHub release has passed every release gate.

The current 1.1.0 gates are lockfile closure, current CI/security/recovery evidence, role-based smoke testing, accessibility evidence, and repository branch-protection enforcement. Do not infer a published stable release until those gates are closed and `v1.1.0` is tagged and successfully processed by release automation.

## Feature map

### Catalog
- Books, authors, publishers, categories, editions/copy metadata, shelves, and accession codes
- ISBN-aware metadata with Unicode-safe text fields
- Branch-aware physical copies and availability states
- Search-ready schema for title, ISBN, author, category, publisher, language, and availability

### Members and access
- Member registration/profile lifecycle
- Library-card identifiers
- Application accounts with role-based authorization
- Administrator, librarian, and member role model
- Administrator UI/API for staff account creation, enable/disable control, password reset, and session revocation on reset
- Opaque bearer-session architecture; passwords are never stored in plaintext

### Circulation
- Issue/check-out
- Return/check-in
- Renewal with policy validation
- Reservation and waitlist workflow
- Fine assessment and settlement workflows
- Overdue and circulation reporting
- Transactional state changes and audit records

### Policy and operations
- Configurable fine rules rather than hard-coded fees
- Multi-branch-ready schema
- Mock-safe notification adapter for local development and SMTP adapter for configured deployments
- Bounded CSV import/export and database backup/restore tooling
- Structured, privacy-conscious audit data

### Product quality
- Responsive React/TypeScript web application
- Light/dark/system appearance architecture
- Keyboard and screen-reader-oriented UI conventions
- Loading, empty, success, warning, and error states
- Security, privacy, accessibility, performance, testing, release, and troubleshooting documentation
- Backend/frontend CI, version synchronization, CodeQL, dependency review, Dependabot, release automation, issue/PR templates, and funding metadata

## Screenshots

Real screenshots will be added after the UI reaches a stable release-candidate state. The repository deliberately does not use fake screenshots that could misrepresent the current build.

## Technology

| Layer | Technology |
|---|---|
| Backend | Java 25, Spring Boot 4.1, Spring Security, Spring JDBC |
| Database | PostgreSQL, Flyway migrations |
| Frontend | React 19.2, TypeScript 6, Vite 8.2 |
| API | JSON REST under `/api` |
| Local services | Docker Compose |
| CI | GitHub Actions |
| Security automation | CodeQL, Dependabot, dependency review, secret-safe configuration |

Spring Boot 4.1 supports Java 25. The frontend package declares the supported Node.js range; Node.js 24 is used by frontend/release CI.

## Repository layout

```text
.
├── .github/                 # CI, release/security automation, templates, funding
├── backend/                 # Spring Boot application and database migrations
├── frontend/                # React + TypeScript application
├── docs/                    # Architecture, setup, operations, ADRs, file reference
├── scripts/                 # Backup/restore helpers and version guard
├── .env.example             # Placeholder-only local configuration contract
├── compose.yml              # Local PostgreSQL
├── CHANGELOG.md
├── ROADMAP.md
├── SECURITY.md
├── PRIVACY.md
└── what_changed.md          # Cross-session implementation handoff
```

For a tracked-file purpose map, see [`docs/repository-reference.md`](docs/repository-reference.md).

## Prerequisites

- Git
- Java 25 JDK
- Maven 3.6.3+
- Node.js compatible with `frontend/package.json`
- npm
- Docker with Compose support, or a separately managed PostgreSQL instance

See [`docs/setup.md`](docs/setup.md) for setup notes and verification commands.

## Quick start

### 1. Clone

```bash
git clone https://github.com/sanskarIN/libracore.git
cd libracore
```

### 2. Configure

Copy `.env.example` to a local `.env` file and replace placeholder development values. Never commit `.env`.

### 3. Start PostgreSQL

```bash
docker compose -f compose.yml up -d postgres
```

### 4. Start the backend

```bash
cd backend
mvn spring-boot:run
```

Default API origin: `http://localhost:8080`.

### 5. Start the frontend

In a second terminal:

```bash
cd frontend
npm ci
npm run dev
```

Default development UI: `http://localhost:5173`.

> `frontend/package-lock.json` is committed and is the canonical resolved frontend dependency graph. Before final `v1.1.0` tagging, verify it with the supported Node/npm toolchain so its root package metadata matches `1.1.0`. Use `npm ci` for clean/reproducible installs. Use `npm install` only when intentionally changing dependency declarations and review the resulting lockfile diff together with `package.json`.

## Configuration

Important environment variables are documented in `.env.example` and `docs/setup.md`. Core variables include:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`
- `APP_CORS_ALLOWED_ORIGINS`
- `APP_SESSION_TTL_HOURS`
- `APP_BOOTSTRAP_ADMIN_EMAIL`
- `APP_BOOTSTRAP_ADMIN_PASSWORD`
- `VITE_API_BASE_URL`

The bootstrap administrator is optional and intended only for explicitly configured local/controlled environments. LibraCore does not commit a default production password.

## Development commands

### Version synchronization

From the repository root:

```bash
node scripts/check-version.mjs 1.1.0
```

This fails if `frontend/package.json`, `backend/pom.xml`, and an optional expected release version disagree.

### Backend

```bash
cd backend
mvn clean verify
mvn spring-boot:run
```

### Frontend

Clean verification uses the committed lockfile:

```bash
cd frontend
npm ci --ignore-scripts --no-audit --no-fund
npm run check
```

`npm run check` performs linting, strict type checking, deterministic Vitest execution, and a production Vite build.

### Full local database reset

For disposable development data only:

```bash
docker compose -f compose.yml down -v
docker compose -f compose.yml up -d postgres
```

Flyway recreates the schema from versioned migrations when the backend starts against an empty database.

## Architecture

LibraCore is a **modular monolith**. The backend keeps HTTP concerns, application services, domain rules, persistence, security, notifications, and operational concerns separated without introducing network boundaries that are not yet justified.

The frontend is organized around application routes/features, reusable UI primitives, API adapters, explicit state transitions, and externalized presentation strings where practical.

Read:

- [`docs/architecture.md`](docs/architecture.md)
- [`docs/adr/0001-modular-monolith.md`](docs/adr/0001-modular-monolith.md)
- [`docs/adr/0002-postgresql-flyway.md`](docs/adr/0002-postgresql-flyway.md)
- [`docs/adr/0003-opaque-bearer-sessions.md`](docs/adr/0003-opaque-bearer-sessions.md)

## Database and migrations

Schema changes belong in `backend/src/main/resources/db/migration/`. Do not edit a released migration to change history; add a new migration. Multi-step circulation writes execute transactionally. Schema constraints protect important invariants even when application validation is bypassed.

## API conventions

- Base path: `/api`
- JSON request/response bodies plus explicit CSV exchange endpoints
- Validation errors use a stable problem-style response shape
- Authentication uses opaque bearer sessions where protected endpoints require identity
- Authorization is enforced server-side; frontend visibility is not treated as security
- Pagination and bounded query parameters are preferred for potentially large collections
- Sensitive material must not be logged

API-specific documentation lives in [`docs/api.md`](docs/api.md).

## Security

Security is a release requirement, not a later add-on. The project uses maintained password hashing, server-side authorization, bounded validation, least-privilege configuration, placeholder-only environment examples, audit trails, and automated security checks.

Read [`SECURITY.md`](SECURITY.md) and [`THREAT_MODEL.md`](THREAT_MODEL.md). Do not report a vulnerability through a public issue when private disclosure is appropriate.

## Privacy

LibraCore stores library/member data in the configured PostgreSQL database. Operators remain responsible for lawful collection, retention, access, backup, export, correction, and deletion policies for their deployment. Development fixtures must be fictional.

Read [`PRIVACY.md`](PRIVACY.md).

## Accessibility

The web application targets practical WCAG-oriented behavior: semantic landmarks, keyboard operation, visible focus, accessible names, non-color-only status cues, reduced-motion support, responsive zoom/text behavior, and meaningful loading/error announcements.

Read [`docs/accessibility.md`](docs/accessibility.md).

## Testing and quality gates

The intended release gate includes:

- backend compile + unit/integration tests
- Flyway migration verification from a clean database
- frontend lockfile presence + `npm ci`
- frontend lint + strict type checks + tests + production build
- manifest/tag version synchronization
- accessibility checks
- dependency/security analysis
- documentation/link review
- clean-checkout smoke verification

See [`docs/testing.md`](docs/testing.md).

## Performance

Large lists must use bounded queries and pagination. Database indexes should be justified by real query paths. The project records performance budgets and measurement notes in [`docs/performance.md`](docs/performance.md).

## Import, export, and recovery

Import validates bounded CSV input before changing durable state. Exports are scoped and privacy-aware. Database backup/restore guidance is documented separately from application-level CSV exchange.

See [`docs/backup-restore.md`](docs/backup-restore.md).

## Deployment and operations

Production deployment requires explicit TLS, secret storage, CORS, reverse-proxy limits, database isolation, backup retention, monitoring, and migration decisions. Development defaults are not a production security boundary.

Read [`docs/deployment.md`](docs/deployment.md) and [`docs/troubleshooting.md`](docs/troubleshooting.md).

## Releases

LibraCore source manifests are currently prepared for **1.1.0**. A release tag must match the backend/frontend manifest version; release automation validates this before packaging. The release workflow requires the committed frontend lockfile, performs reproducible frontend verification, starts the packaged backend against PostgreSQL, requires a healthy actuator response, and packages the backend JAR without a hard-coded historical filename.

Release procedure, migration checks, rollback expectations, artifact verification, and release-note requirements are in [`docs/release.md`](docs/release.md).

## Contributing

Contributions are welcome. Start with [`CONTRIBUTING.md`](CONTRIBUTING.md), follow [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md), keep changes focused, add tests for behavior changes, and update documentation with behavior.

## Support and contact

- GitHub: https://github.com/sanskarIN
- Business: `sanskarin@outlook.in`
- Business: `sanskarin.business@gmail.com`
- Support: `supportramsandesh@gmail.com`
- Buy Me a Coffee: https://buymeacoffee.com/sanskarIN

Funding is optional and never required to use the project.

## License

LibraCore is licensed under the [MIT License](LICENSE).

## Project continuity

For the exact repository checkpoint, recent meaningful commits, verification evidence, limitations, and next tasks, read [`what_changed.md`](what_changed.md). For planned capabilities and milestones, read [`ROADMAP.md`](ROADMAP.md).

---

**Made by the Sanskar**
