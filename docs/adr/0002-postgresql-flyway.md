# ADR 0002: PostgreSQL with Flyway Migrations

- Status: Accepted
- Date: 2026-08-19

## Context

LibraCore requires relational integrity, transactions, constraints, indexing, date/time handling, numeric precision for fines, and a reproducible upgrade path.

## Decision

Use PostgreSQL as the primary relational database and Flyway versioned SQL migrations as the authoritative schema history. Application persistence uses parameterized Spring JDBC.

## Consequences

- Schema changes are reviewable source artifacts.
- Clean environments can be built deterministically from migration history.
- Released migrations must not be rewritten; corrections use later migrations.
- PostgreSQL-specific features may be used deliberately when they provide clear correctness/performance value.
- Operators must review production migration lock/runtime impact and maintain tested backups.
- Application rollback must consider schema compatibility; restoring an older binary alone is not a database rollback strategy.
