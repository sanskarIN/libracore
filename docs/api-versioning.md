# LibraCore API Versioning Policy

## Purpose

This document defines how the `/api` surface evolves during the 1.2.x release line. The goal is to make additive change the default while making compatibility and migration decisions explicit.

## Compatibility categories

### Additive

An additive change does not invalidate existing clients. Examples include a new optional response field, a new endpoint, or a new optional request property with a safe default.

### Behavior-preserving maintenance

A maintenance change fixes an implementation defect while retaining the documented request and response contract. Release notes should explain behavior changes that could affect operators or users.

### Contract change

A contract change modifies validation, authorization, required fields, response semantics, pagination behavior, or another externally observable rule. These changes require tests and a release-note entry describing the migration impact.

### Breaking

A breaking change removes or renames a required field/endpoint, changes an existing field's meaning or type, or makes previously valid requests invalid without a compatibility path. Breaking changes require an explicit migration plan and should not be introduced silently in a patch release.

## Rules for 1.2.x

1. Prefer additive endpoints and optional response fields.
2. Never expose sensitive internal fields merely to make a client feature convenient.
3. Preserve server-side authorization; frontend route visibility is not a security boundary.
4. Keep pagination bounded for collection endpoints.
5. Use stable problem-style errors with machine-readable codes where validation or authorization fails.
6. Document externally observable behavior changes in `CHANGELOG.md` and the applicable release note.
7. Add contract tests for every new endpoint or materially changed response.
8. Treat database migrations as forward-only history; never edit an already released migration.
9. Preserve correlation identifiers where the existing error/audit contract provides them.
10. Validate authentication and authorization behavior for both permitted and denied roles.

## Database migration policy

LibraCore uses Flyway-managed database migrations. Released migrations are immutable: a migration that has already shipped must never be edited or repurposed. Schema changes require a new forward migration, and release validation should exercise migration startup against a disposable or isolated database before production deployment.

## Deprecation

A deprecated API should remain functional for a documented transition period whenever practical. Deprecation must be visible in release documentation and accompanied by a replacement path. Removal requires an explicit release decision rather than an incidental refactor.

## Testing expectations

API changes should cover success, validation failure, authorization failure, not-found behavior where applicable, pagination/bounds, and persistence invariants. Security-sensitive endpoints should also have tests proving that an unauthorized role cannot gain access by bypassing frontend controls.

## Client guidance

Clients should ignore unknown response fields, use documented machine-readable error codes instead of matching prose, respect pagination metadata, and avoid depending on undocumented fields or ordering.

## Review checklist

- [ ] Compatibility category identified.
- [ ] API documentation updated.
- [ ] Contract tests added or updated.
- [ ] Authorization tests cover allowed and denied roles.
- [ ] Persistence/migration impact reviewed.
- [ ] Flyway migration policy reviewed.
- [ ] Release notes and changelog updated.
- [ ] Security and privacy impact reviewed.
