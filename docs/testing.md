# Testing and Quality Gates

## Release gate

The current source line is prepared for **2.0.12**. A release candidate must pass the smallest relevant checks during development and the full suite before `v2.0.12` is tagged.

### Version synchronization

From the repository root:

```bash
node scripts/check-version.mjs 2.0.12
```

The backend Maven version, frontend npm version, and intended release version must agree. GitHub Actions also runs the Version Sync workflow on relevant manifest/script changes.

### Backend

```bash
cd backend
mvn clean verify
```

This must compile the application, run unit/integration tests configured by Maven, package the application, and leave Flyway migrations compatible with a clean PostgreSQL database. CI additionally discovers the packaged JAR without a hard-coded version filename, starts it, and checks `/actuator/health` against PostgreSQL.

### Frontend

For local development on a commit that does not yet contain `frontend/package-lock.json`:

```bash
cd frontend
npm install
npm run check
```

That development path is **not sufficient for a release**. Before tagging 2.0.12, generate/review/commit the lockfile locally or with the **Frontend Lockfile Bootstrap** workflow, then verify reproducibly:

```bash
cd frontend
npm ci --ignore-scripts
npm run check
```

Normal Frontend CI is read-only and intentionally fails when the lockfile is absent. `npm run check` runs lint, strict type checking, deterministic Vitest execution, and a production build.

Vitest tests cover pure utilities and state/storage edge cases. New regression-prone logic should be moved into testable functions rather than buried in event handlers.

## Test layers

- **Unit:** normalization, ISBN validation, CSV codec, fine policy calculations, formatting/session/theme/API utilities.
- **Service/integration:** circulation transactions, reservations, member/account ownership, fine settlement, staff account administration, imports, database constraints and security boundaries.
- **HTTP/security:** authentication, role checks, validation errors, ownership restrictions, CORS/security behavior.
- **UI/component:** state rendering, form validation, role-aware navigation, accessible names and keyboard interaction.
- **End-to-end:** sign in, catalog lookup, member management, issue/return, reservation/cancellation, staff account administration, member self-service, reports/import/export.
- **Operational:** clean migration, packaged startup/health, backup restore drill.

## Regression policy

Every fixed bug should receive a test at the lowest layer that reproduces it reliably. Avoid tests coupled to implementation details when a public behavior/invariant can be asserted instead.

## Determinism

Use fictional fixtures, controlled clocks where time boundaries matter, explicit locales/currencies, and disposable databases. Tests must not require production accounts, SMTP credentials, or internet services after dependencies/artifacts are resolved.

## Accessibility review

Automated scanning is useful but insufficient. Before stable release, manually verify keyboard-only navigation, visible focus, zoom/reflow, reduced-motion preference, status text not relying only on color, form labels/errors, and screen-reader landmark/name basics. Record evidence in the release checklist.

## Security checks

Run dependency/security automation and review authentication/authorization-sensitive changes manually. Secret scanning should be enabled at the repository/host level where available. Never paste real credentials into test fixtures.

## Current 2.0.12 limitations

- Browser-level E2E automation is not yet committed, so primary web journeys still require a documented manual smoke pass in addition to automated frontend/backend checks.
- `frontend/package-lock.json` is still required before Frontend CI and the tagged release workflow can become green/reproducible.
- Final successful CI/security status and clean runtime/restore/accessibility evidence must be observed rather than inferred from workflow/source presence.

These limitations are tracked in `ROADMAP.md`, `docs/releases/2.0.12.md`, and `what_changed.md` rather than hidden.
