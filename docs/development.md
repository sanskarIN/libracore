# Development Workflow

## Working principles

LibraCore favors small modules, explicit domain rules, migrations, deterministic tests, and reviewable commits. Keep the modular-monolith boundary unless a measured requirement justifies a network boundary.

## Backend changes

1. Place HTTP validation/serialization in controllers/models.
2. Put business invariants and transactions in services.
3. Use parameterized Spring JDBC queries.
4. Add a new Flyway migration for schema changes; never rewrite released migration history.
5. Add tests near the domain/parser/security behavior being changed.
6. Run `mvn clean verify` before merge.

## Frontend changes

1. Keep API transport in `api.ts` and shared response contracts in `types.ts`.
2. Keep feature UI in page/components rather than adding unrelated global state.
3. Do not treat role-based navigation visibility as authorization.
4. Preserve semantic labels, keyboard operation, focus styles, reduced-motion behavior, and responsive layouts.
5. Externalize recurring copy when practical.
6. Run lint, typecheck, tests, and production build.

## Database work

Migration names use Flyway ordering such as `V7__describe_change.sql`. A migration must be safe to apply exactly once to the prior supported schema. Review locking/downtime implications for production-sized tables.

## Configuration

Add new non-secret configuration keys to `.env.example` and relevant docs. Secrets must be injected by the deployment environment.

## Error handling

API errors should use stable machine-readable codes and user-safe messages. Do not expose SQL, stack traces, tokens, passwords, or internal secrets to clients.

## Logging and audit

Use application logs for diagnostics and domain audit events for accountable operational changes. They are not interchangeable. Avoid PII unless it is necessary for the documented operational purpose, and never log credentials/tokens.

## Commit discipline

Use focused Conventional Commit messages. Examples:

```text
feat: add reservation expiry policy
fix: reject issue for suspended member
test: cover duplicate accession import
docs: document restore verification
```

Before pushing, review `git diff` for accidental secrets, generated output, or unrelated edits.
