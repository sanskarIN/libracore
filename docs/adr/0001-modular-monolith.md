# ADR 0001: Use a Modular Monolith

- Status: Accepted
- Date: 2026-08-19

## Context

Catalog, members, circulation, reservations, fines, audit, notifications, and reporting share strong transactional relationships. Splitting them into network services early would add deployment and consistency complexity without measured scaling evidence.

## Decision

Use one Spring Boot application organized into cohesive domain modules with one PostgreSQL database. Keep HTTP, business rules, persistence, security, and adapters separated inside the codebase so boundaries remain understandable and can evolve later if justified.

## Consequences

- Multi-step library operations can use ordinary database transactions.
- Local development/deployment remains simple.
- Module coupling must be reviewed because the compiler does not enforce network boundaries.
- Independent service scaling is intentionally deferred.
- Any future service extraction requires measured need, explicit data ownership, failure semantics, and a new threat/architecture review.
