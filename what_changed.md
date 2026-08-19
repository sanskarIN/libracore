# LibraCore — 2.0.12 Final Engineering Handoff

**Audit/update date:** 2026-08-19  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**2.0.12 documentation/source checkpoint before this handoff commit:** `7b66a79b28579496a6580ae304da7b62a86bd42f`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This file is the canonical continuation record for LibraCore. Read it before adding more features. It distinguishes completed source work from verification that still requires a runnable dependency/CI/host environment.

## Version 2.0.12 status

LibraCore source is now explicitly prepared as **version 2.0.12**:

- `backend/pom.xml` project version: `2.0.12`;
- `frontend/package.json` version: `2.0.12`;
- Settings displays the frontend build version injected from `frontend/package.json` rather than a duplicated hard-coded string;
- `scripts/check-version.mjs` fails if backend/frontend versions disagree and can also validate an expected tag such as `v2.0.12`;
- `.github/workflows/version-sync.yml` runs that synchronization check on relevant repository changes;
- `.github/workflows/release.yml` validates the release tag against both executable manifests.

**Do not tag/publish `v2.0.12` yet.** The source version is prepared, but release evidence is not complete because the frontend lockfile is still absent, final CI success is not observable through the available status interface, and `main` branch protection remains disabled.

## Product implementation currently present

The repository contains the end-to-end LibraCore library-management implementation across:

- Spring Boot modular-monolith backend;
- PostgreSQL persistence and Flyway migrations V1–V6;
- books, authors, publishers, categories, branches, shelves, physical copies, accession/barcode/QR lookup, and search;
- members, account linking, administrator/librarian/member roles, authentication, and authorization;
- issue, return, renewal, reservations/waitlists, circulation policy, fine assessment and settlement;
- dashboard, overdue reporting, audit search, CSV import/export, notification scheduling/gateways;
- administrator staff-account creation, role filtering, enable/disable, password reset, session revocation, and active-session visibility;
- responsive React/TypeScript frontend with light/dark/system themes and role-aware navigation;
- mobile navigation that keeps every authorized route reachable;
- backup/restore helpers;
- security/privacy/threat-model/contribution/support documentation;
- CodeQL, dependency review, Dependabot, backend/frontend CI, release automation, version sync, issue/PR templates, and funding metadata.

## 2.0.12 work completed in this continuation

### Executable version alignment

Changed both build manifests from the old 0.1.x identifiers to `2.0.12`.

Important commits:

- `2b1ce0fd` — `release: set backend version 2.0.12`
- `774bc10f` — `release: set frontend version 2.0.12`

### Build-derived frontend version

Removed the hard-coded `0.1.0` display from Settings.

Implemented:

- Vite reads `frontend/package.json` during configuration;
- Vite injects `__APP_VERSION__` at build time;
- `frontend/src/env.d.ts` declares the injected constant for strict TypeScript;
- Settings renders `{__APP_VERSION__}` and identifies the source as the 2.0.x release-candidate line.

Commits:

- `2464145b` — `build: inject frontend package version`
- `22b11afc` — `build: type injected application version`
- `c0d3cfe4` — `release: show build version in settings`

### Cross-manifest version guard

Added `scripts/check-version.mjs`.

It:

- reads `frontend/package.json`;
- extracts the LibraCore backend project version from `backend/pom.xml`;
- fails if they disagree;
- accepts an optional expected value/tag and strips a leading `v`;
- prints the synchronized version when valid.

A local dependency-free execution test was performed with 2.0.12 fixture manifests and returned:

```text
LibraCore version 2.0.12 is synchronized across frontend and backend manifests.
```

This tests the guard itself; it is not a substitute for the repository's Maven/npm build.

Commit:

- `2da8b109` — `build: add cross-manifest version guard`

### Version synchronization CI

Added `.github/workflows/version-sync.yml` so relevant manifest/script changes get a lightweight, dependency-minimal version consistency check.

Commit:

- `0f93167e` — `ci: enforce synchronized release versions`

### Backend CI version bug fixed

Changing the Maven version exposed a concrete CI defect: backend CI still attempted to run:

```text
target/libracore-backend-0.1.0-SNAPSHOT.jar
```

That filename no longer matches the packaged artifact. Backend CI now discovers the packaged `libracore-backend-*.jar` dynamically, excludes Spring Boot's `.original` artifact, and fails clearly if no packaged JAR exists.

Commit:

- `a998e189` — `ci: make backend startup version independent`

### Release workflow hardened

The tagged release workflow no longer contains the historical `0.1.0-SNAPSHOT` artifact path.

It now:

- runs backend Maven verification against PostgreSQL;
- requires `frontend/package-lock.json` before setting up cached npm installation;
- validates the pushed tag against backend/frontend manifest versions;
- installs frontend dependencies with `npm ci --ignore-scripts`;
- runs `npm run check`;
- discovers the packaged backend JAR dynamically;
- packages the frontend production bundle;
- creates SHA-256 checksums;
- publishes artifacts through the GitHub release action.

Relevant commits:

- `3eb17e29` — `ci: harden 2.0.12 release verification`
- `db3fe2a4` — `ci: fail clearly when release lockfile is absent`

### Frontend CI made read-only and reproducible

The previous frontend CI tried to generate and push a lockfile from a normal CI run. That mixed verification with repository mutation and required broad `contents: write` permission.

Frontend CI now:

- has read-only repository permission;
- requires an already committed lockfile;
- uses Node 24 + npm cache keyed by the lockfile;
- checks version synchronization;
- installs using `npm ci`;
- runs the aggregate frontend quality gate.

Commit:

- `a9466a5e` — `ci: make frontend verification read-only and reproducible`

### Explicit lockfile bootstrap workflow

Because the lockfile is not yet committed and this execution environment cannot reach the npm registry, lockfile generation was separated into `.github/workflows/lockfile-bootstrap.yml`.

The maintainer-triggered workflow:

1. checks out `main`;
2. uses Node 24;
3. generates `frontend/package-lock.json` with `npm install --package-lock-only --ignore-scripts`;
4. installs from the generated lock with `npm ci`;
5. runs `npm run check`;
6. commits/pushes the lockfile only when it changed.

Commit:

- `5a99a727` — `ci: add explicit frontend lockfile bootstrap`

The available GitHub connector does not expose workflow dispatch, so this workflow could be committed but not triggered from this chat.

## 2.0.12 documentation completed

Updated or added:

- `README.md` — identifies 2.0.12 as the current source release candidate and documents version synchronization plus lockfile rules;
- `CHANGELOG.md` — adds the 2.0.12 release-candidate record and outstanding release gates;
- `ROADMAP.md` — moves current closure work to the 2.0.12 line and separates later 2.1.x/2.2.x hardening;
- `docs/release.md` — defines strict 2.0.12 tag/version/lockfile/reproducibility gates;
- `docs/releases/2.0.12.md` — dedicated 2.0.12 release-candidate scope and pre-tag checklist;
- `docs/repository-reference.md` — exhaustive file-purpose reference refreshed for the new workflows, version guard, build-version declaration, release notes, and current lockfile limitation;
- this `what_changed.md` — canonical current continuation record.

Documentation commits:

- `1dec12af` — `docs: align README with version 2.0.12`
- `628efd02` — `docs: harden 2.0.12 release process`
- `f18fab24` — `docs: move release roadmap to 2.0.12`
- `03b78f4d` — `docs: prepare 2.0.12 changelog`
- `84e5a747` — `docs: add 2.0.12 release candidate notes`
- `7b66a79b` — `docs: refresh exhaustive reference for 2.0.12`

## Existing important fixes retained from the preceding final audit

The previous audit already fixed/completed:

- missing frontend application entry point;
- circulation, reservation, reports, and settings pages;
- administrator staff-account frontend workflow;
- strict `exactOptionalPropertyTypes` login-prop defect;
- asynchronous staff form reset safety;
- mobile route truncation;
- direct frontend dependency version pinning;
- aggregate `npm run check` command;
- backend/frontend/security/release workflow foundations;
- security/privacy/threat model;
- contribution/conduct/support policies;
- issue forms, PR template, Dependabot, CodeQL, dependency review;
- architecture/API/setup/development/testing/deployment/backup/accessibility/performance/release/troubleshooting/branch-protection/ADR documentation;
- exhaustive repository file reference.

## Verification evidence from this 2.0.12 pass

Actually checked:

- current `main` branch through the connected GitHub API;
- backend Maven manifest version and dependencies;
- frontend npm manifest version and tool versions;
- frontend Settings source;
- Vite configuration and strict frontend TypeScript configuration;
- backend CI workflow;
- frontend CI workflow;
- release workflow;
- release/roadmap/changelog/repository documentation;
- `frontend/package-lock.json` presence;
- current branch-protection state;
- combined commit statuses for the final pre-handoff 2.0.12 checkpoint;
- version-guard behavior in a local dependency-free fixture.

Observed at the final pre-handoff checkpoint:

- `frontend/package-lock.json`: **not present** (`404 Not Found`);
- `main` protection: **disabled** (`protected: false`);
- combined commit status list: **empty** through the available connector.

Local npm registry access was attempted for `npm install --package-lock-only`; it did not complete within the execution timeout, consistent with the environment's lack of usable outbound dependency access. Therefore no lockfile was fabricated or manually guessed.

## Claims intentionally not made

This pass does **not** claim:

- all Maven tests passed;
- frontend lint/typecheck/tests/build passed against installed dependencies;
- CodeQL passed;
- clean PostgreSQL/Flyway startup passed;
- backup/restore drill passed;
- browser smoke tests passed;
- accessibility release evidence is complete;
- 2.0.12 is release-green or already published.

These require observable execution evidence.

## Remaining 2.0.12 release blockers

### 1. Generate and commit `frontend/package-lock.json`

Preferred hosted path:

- GitHub Actions → **Frontend Lockfile Bootstrap** → Run workflow;
- inspect the generated commit/lockfile;
- confirm the workflow's frontend verification succeeds.

Equivalent local path:

```bash
cd frontend
npm install --package-lock-only --ignore-scripts --no-audit --no-fund
npm ci --ignore-scripts --no-audit --no-fund
npm run check
git add package-lock.json
git commit -m "build: lock frontend dependencies for 2.0.12"
git push
```

### 2. Observe successful CI/security checks

Required evidence on the final intended release commit:

- Backend CI;
- Frontend CI;
- Version Sync;
- CodeQL;
- dependency/security checks relevant to the change set.

Fix every failure before tagging.

### 3. Clean database/runtime evidence

Use a clean disposable PostgreSQL database and prove:

- migrations V1–V6 apply in order;
- packaged backend starts;
- `/actuator/health` is healthy;
- representative domain operations work;
- isolated backup/restore drill succeeds.

### 4. Role-based application smoke evidence

Verify administrator, librarian, and member journeys, especially:

- login/logout/session expiry;
- catalog search/detail/copy administration;
- member administration/account linking;
- issue/return/renew/fine settlement;
- reservations/waitlists;
- administrator staff-account creation/filter/enable-disable/password reset;
- reports/audit;
- CSV import/export;
- responsive/mobile route access;
- loading/empty/error states.

### 5. Accessibility release evidence

Record keyboard, visible-focus, zoom/reflow, reduced-motion, and screen-reader checks described in `docs/accessibility.md`.

### 6. Enable `main` branch protection/rules

The repository currently reports `protected: false`.

After check names are stable and green, configure the recommended rules in `docs/branch-protection.md`, including required checks, force-push/deletion restrictions, and minimal bypass access.

The currently available GitHub write connector does not expose branch-protection/ruleset mutation, so this host-level setting cannot be completed by a source-file commit.

## Exact final closure sequence for 2.0.12

1. Generate/review/commit `frontend/package-lock.json`.
2. Confirm `node scripts/check-version.mjs 2.0.12`.
3. Run/observe Backend CI.
4. Run/observe Frontend CI using the committed lockfile.
5. Run/observe Version Sync and CodeQL/security checks.
6. Verify clean PostgreSQL/Flyway packaged startup and `/actuator/health`.
7. Execute role-based smoke tests.
8. Record accessibility evidence.
9. Execute isolated backup/restore drill.
10. Enable `main` branch protection using stable green check names.
11. Re-read `CHANGELOG.md`, `ROADMAP.md`, `docs/releases/2.0.12.md`, `docs/release.md`, and this handoff.
12. Only then create and push `v2.0.12`.

Release command after every gate is closed:

```bash
git tag -a v2.0.12 -m "LibraCore v2.0.12"
git push origin v2.0.12
```

## Documentation map

- overview/current source version: `README.md`
- complete tracked-file map: `docs/repository-reference.md`
- 2.0.12 release-candidate notes: `docs/releases/2.0.12.md`
- release process: `docs/release.md`
- delivered changes: `CHANGELOG.md`
- remaining release/product work: `ROADMAP.md`
- architecture: `docs/architecture.md`
- API: `docs/api.md`
- setup: `docs/setup.md`
- development: `docs/development.md`
- testing: `docs/testing.md`
- deployment: `docs/deployment.md`
- backup/restore: `docs/backup-restore.md`
- accessibility: `docs/accessibility.md`
- performance: `docs/performance.md`
- troubleshooting: `docs/troubleshooting.md`
- branch protection: `docs/branch-protection.md`
- security/privacy: `SECURITY.md`, `PRIVACY.md`, `THREAT_MODEL.md`
- architecture decisions: `docs/adr/`
- continuation truth: this file.

## Continuation rule

Do not add unrelated feature expansion before closing the explicit 2.0.12 release blockers. On the next continuation, first inspect current `main`, `frontend/package-lock.json`, CI statuses, and branch protection. If the lockfile has appeared, switch clean frontend documentation/verification fully to `npm ci`, reconcile the repository reference, run/observe the remaining gates, and only then consider `v2.0.12` ready for tagging.

**Made by the Sanskar**
