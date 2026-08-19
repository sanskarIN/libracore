# LibraCore Architecture

## System shape

LibraCore is a modular monolith with two deployable application layers:

```text
Browser (React/TypeScript)
        |
        | JSON/CSV HTTP + Bearer session
        v
Spring Boot API
  |-- security
  |-- catalog
  |-- member
  |-- circulation/fines
  |-- reporting/audit
  |-- exchange
  |-- notification
        |
        v
PostgreSQL (Flyway-managed schema)
```

This structure keeps transactional library rules in one process/database while preserving module boundaries. It avoids distributed consistency costs before there is a demonstrated scaling need.

## Backend modules

- `security`: login/logout, application principals, bearer sessions, password verification, bootstrap admin configuration, HTTP security.
- `catalog`: books, authors, publishers, categories, physical copies, branches, shelves, availability, code lookup.
- `member`: member lifecycle, search, account linkage, member-facing profile.
- `circulation`: issue/return/renew, reservations, queue promotion, policy snapshots, fines and settlements.
- `reporting`: operational dashboard, overdue views, audit queries.
- `audit`: durable operational event recording used by domain services.
- `exchange`: bounded CSV import/export and parsing.
- `notification`: notification orchestration plus mock and SMTP adapters.
- `common`: stable errors, exception mapping, normalization helpers.

Controllers validate HTTP contracts and delegate. Services own business transactions and authorization-sensitive decisions that are not purely endpoint-role checks. Spring JDBC uses parameterized queries; Flyway owns schema history.

## Frontend

`frontend/src/App.tsx` composes authentication, session storage, API client, hash routing, role-aware shell, and feature pages. Feature pages receive explicit dependencies rather than reaching into global mutable state. Shared UI primitives live under `components/`; transport models live in `types.ts`; theme/session/formatting are isolated utilities with tests.

The client is not a security boundary. Server authorization remains authoritative.

## Data invariants

Important state transitions use database transactions and constraints. Examples include copy availability during issue/return, reservation assignment/promotion, membership eligibility, policy application, and fine settlement. Released migrations are append-only: change schema by adding a new migration.

## Authentication

LibraCore uses opaque bearer sessions rather than stateless self-contained tokens. The server controls session expiry and revocation. Browser session data is intentionally stored in `sessionStorage`, limiting persistence across browser sessions but still requiring XSS-safe UI and TLS in production.

## Configuration

Runtime configuration belongs in environment variables/application configuration, not source code. `.env.example` documents names with non-secret examples/placeholders. Production secret storage is a deployment responsibility.

## Operational boundaries

Backups are database-level recovery tools; CSV exchange is an application-level interoperability feature. They solve different problems and have different security/consistency properties.

See ADRs in [`docs/adr/`](adr/) and [`THREAT_MODEL.md`](../THREAT_MODEL.md).
