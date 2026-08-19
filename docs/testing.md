# Testing and Quality Gates

## Release gate

A release candidate must pass the smallest relevant checks during development and the full suite before release.

### Backend

```bash
cd backend
mvn clean verify
```

This must compile the application, run unit/integration tests configured by Maven, package the application, and leave Flyway migrations compatible with a clean PostgreSQL database. CI additionally starts the packaged JAR and checks `/actuator/health` against PostgreSQL.

### Frontend

```bash
cd frontend
npm ci
npm run lint
npm run typecheck
npm run test:run
npm run build
```

Vitest tests cover pure utilities and state/storage edge cases. New regression-prone logic should be moved into testable functions rather than buried in event handlers.

## Test layers

- **Unit:** normalization, ISBN validation, CSV codec, fine policy calculations, formatting/session/theme utilities.
- **Service/integration:** circulation transactions, reservations, member/account ownership, fine settlement, imports, database constraints and security boundaries.
- **HTTP/security:** authentication, role checks, validation errors, ownership restrictions, CORS/security behavior.
- **UI/component:** state rendering, form validation, role-aware navigation, accessible names and keyboard interaction.
- **End-to-end:** sign in, catalog lookup, issue/return, reservation/cancellation, member self-service, reports/import/export.
- **Operational:** clean migration, packaged startup/health, backup restore drill.

## Regression policy

Every fixed bug should receive a test at the lowest layer that reproduces it reliably. Avoid tests coupled to implementation details when a public behavior/invariant can be asserted instead.

## Determinism

Use fictional fixtures, controlled clocks where time boundaries matter, explicit locales/currencies, and disposable databases. Tests must not require production accounts, SMTP credentials, or internet services.

## Accessibility review

Automated scanning is useful but insufficient. Before stable release, manually verify keyboard-only navigation, visible focus, zoom/reflow, reduced-motion preference, status text not relying only on color, form labels/errors, and screen-reader landmark/name basics. Record evidence in the release checklist.

## Security checks

Run dependency/security automation and review authentication/authorization-sensitive changes manually. Secret scanning should be enabled at the repository/host level where available. Never paste real credentials into test fixtures.

## Current limitation

Until browser-level E2E automation is committed, primary web journeys require a documented manual smoke pass in addition to automated frontend/backend checks. This limitation is tracked in `ROADMAP.md` rather than hidden.
