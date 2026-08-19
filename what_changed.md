# LibraCore — 2.0.12 Final Engineering Handoff

**Audit/update date:** 2026-08-19  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Final source checkpoint immediately before this handoff refresh:** `220e51d68c1c06d56312f0c2986043e6d28595de`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This file is the canonical continuation record for LibraCore. Read it before adding more features. It distinguishes completed source work from verification that still requires a runnable dependency/CI/host environment.

## Version 2.0.12 status

LibraCore source is explicitly prepared as **version 2.0.12**:

- `backend/pom.xml` project version: `2.0.12`;
- `frontend/package.json` version: `2.0.12`;
- Settings displays a build-time version derived from `frontend/package.json` rather than a duplicated hard-coded string;
- `scripts/check-version.mjs` validates backend/frontend version equality and can validate an expected tag/version such as `v2.0.12`;
- `.github/workflows/version-sync.yml` enforces manifest synchronization on relevant changes;
- `.github/workflows/release.yml` validates the release tag against both executable manifests.

**Do not tag/publish `v2.0.12` yet.** The source version is prepared, but release evidence remains incomplete because the frontend lockfile is absent, final CI success is not observable through the available status interface, and `main` branch protection remains disabled.

## Product implementation present

The repository contains the end-to-end LibraCore implementation across:

- Spring Boot modular-monolith backend;
- PostgreSQL persistence and Flyway migrations V1–V6;
- books, authors, publishers, categories, branches, shelves, physical copies, accession/barcode/QR lookup, and search;
- members, account linking, administrator/librarian/member roles, authentication, and authorization;
- issue, return, renewal, reservations/waitlists, circulation policy, fine assessment and settlement;
- dashboard, overdue reporting, audit search, CSV import/export, notification scheduling/gateways;
- administrator staff-account creation, role filtering, enable/disable, password reset, session revocation, and active-session visibility;
- responsive React/TypeScript frontend with light/dark/system themes and role-aware navigation;
- role-complete horizontally scrollable mobile navigation;
- backup/restore helpers;
- security/privacy/threat-model/contribution/support documentation;
- CodeQL, dependency review, Dependabot, backend/frontend CI, release automation, version sync, issue/PR templates, and funding metadata.

## 2.0.12 work completed in this continuation

### Executable version alignment

Changed both executable manifests from the old 0.1.x identifiers to `2.0.12`.

- `2b1ce0fd` — `release: set backend version 2.0.12`
- `774bc10f` — `release: set frontend version 2.0.12`

### Build-derived frontend version

Removed the hard-coded `0.1.0` Settings display.

- Vite reads `frontend/package.json` during configuration.
- Vite injects `__APP_VERSION__` at build time.
- `frontend/src/env.d.ts` declares the injected constant for strict TypeScript.
- Settings renders the build-derived version and identifies the line as a 2.0.x release candidate.

Commits:

- `2464145b` — `build: inject frontend package version`
- `22b11afc` — `build: type injected application version`
- `c0d3cfe4` — `release: show build version in settings`

### Cross-manifest version guard

Added `scripts/check-version.mjs`.

It reads the frontend npm version and backend Maven project version, fails if they disagree, and optionally verifies an expected value/tag after stripping a leading `v`.

A dependency-free local fixture execution for 2.0.12 returned:

```text
LibraCore version 2.0.12 is synchronized across frontend and backend manifests.
```

This validates the guard itself, not the complete project build.

- `2da8b109` — `build: add cross-manifest version guard`
- `0f93167e` — `ci: enforce synchronized release versions`

### Backend CI version bug fixed

Changing Maven version exposed a concrete CI failure: backend CI still attempted to start `target/libracore-backend-0.1.0-SNAPSHOT.jar`.

Backend CI now discovers the packaged `libracore-backend-*.jar` dynamically, excludes `.original`, and fails clearly if no packaged JAR exists.

- `a998e189` — `ci: make backend startup version independent`

### Release workflow hardened

The tagged release workflow now:

- runs backend Maven verification against PostgreSQL;
- requires `frontend/package-lock.json` before release dependency setup;
- validates pushed tag versus backend/frontend versions;
- installs frontend dependencies with `npm ci --ignore-scripts`;
- runs `npm run check`;
- discovers the packaged backend JAR without a historical version filename;
- packages the frontend production bundle;
- creates SHA-256 checksums;
- publishes artifacts through the GitHub release action.

Commits:

- `3eb17e29` — `ci: harden 2.0.12 release verification`
- `db3fe2a4` — `ci: fail clearly when release lockfile is absent`

### Frontend CI made read-only and reproducible

Normal frontend CI no longer generates/commits dependency state. It now:

- uses read-only repository permission;
- requires a committed lockfile;
- uses Node 24 and npm cache keyed by the lockfile;
- checks version synchronization;
- installs through `npm ci`;
- runs the aggregate frontend quality gate.

- `a9466a5e` — `ci: make frontend verification read-only and reproducible`

### Explicit lockfile bootstrap workflow

Added `.github/workflows/lockfile-bootstrap.yml` for maintainers.

It checks out `main`, uses Node 24, generates the lockfile, installs from it, runs the frontend quality gate, and commits/pushes the lockfile only when changed.

- `5a99a727` — `ci: add explicit frontend lockfile bootstrap`

The connected GitHub write tools available in this chat do not expose workflow dispatch, so this workflow could be committed but not triggered from this execution.

## 2.0.12 documentation completed

Updated or added:

- `README.md` — current 2.0.12 source status, version synchronization, lockfile/release rules;
- `CHANGELOG.md` — 2.0.12 release-candidate record and release gates;
- `ROADMAP.md` — 2.0.12 closure line plus 2.1.x/2.2.x follow-on hardening;
- `CONTRIBUTING.md` — version guard and reproducible frontend contribution flow;
- `docs/setup.md` — 2.0.12 setup, lockfile generation, and post-lock clean verification;
- `docs/testing.md` — 2.0.12 version/lockfile/CI quality gates;
- `docs/release.md` — strict tag/version/lockfile/reproducibility gates;
- `docs/branch-protection.md` — Backend CI, Frontend CI, Version Sync, CodeQL/security, dependency-review expectations plus lockfile prerequisite;
- `docs/releases/2.0.12.md` — dedicated 2.0.12 release-candidate scope and pre-tag checklist;
- `docs/repository-reference.md` — exhaustive tracked-file purpose map refreshed for new workflows/scripts/declarations/release notes;
- this `what_changed.md` — canonical final continuation record.

Key documentation commits:

- `1dec12af` — README 2.0.12 alignment
- `628efd02` — 2.0.12 release process
- `f18fab24` — 2.0.12 roadmap
- `03b78f4d` — 2.0.12 changelog
- `84e5a747` — 2.0.12 release candidate notes
- `7b66a79b` — exhaustive repository reference refresh
- `2254688f` — branch-protection/check alignment
- `84604498` — testing gate alignment
- `dfea1e3e` — setup/lockfile workflow alignment
- `220e51d6` — contribution workflow alignment

## Existing final-audit fixes retained

The preceding audit already fixed/completed:

- missing frontend application entry point;
- circulation, reservation, reports, and settings pages;
- administrator staff-account frontend workflow;
- strict `exactOptionalPropertyTypes` login-prop defect;
- asynchronous staff form reset safety;
- mobile route truncation;
- exact direct frontend dependency/tool version pinning;
- aggregate `npm run check` command;
- backend/frontend/security/release workflow foundations;
- security/privacy/threat model;
- contribution/conduct/support policies;
- issue forms, PR template, Dependabot, CodeQL, dependency review;
- architecture/API/setup/development/testing/deployment/backup/accessibility/performance/release/troubleshooting/branch-protection/ADR documentation;
- exhaustive tracked-file repository reference.

## Final verification evidence

Actually checked in this continuation:

- current `main` through the connected GitHub API;
- backend Maven version/configuration;
- frontend npm version/tool configuration;
- Settings/Vite/strict TypeScript version wiring;
- backend CI workflow;
- frontend CI workflow;
- release workflow;
- setup/testing/release/branch/contribution documentation;
- `frontend/package-lock.json` presence;
- current branch-protection state;
- combined commit statuses for the final pre-handoff source checkpoint;
- version-guard behavior in a local dependency-free fixture.

Final pre-handoff observations at `220e51d68c1c06d56312f0c2986043e6d28595de`:

- `frontend/package-lock.json`: **not present** (`404 Not Found`);
- `main` protection: **disabled** (`protected: false`, required checks unenforced);
- combined commit statuses: **empty** through the available connector.

Local npm registry access was attempted for `npm install --package-lock-only`; it did not complete within the execution timeout. No lockfile was fabricated, guessed, or manually assembled.

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

Those claims require observable execution evidence.

## Remaining 2.0.12 release blockers

### 1. Generate and commit `frontend/package-lock.json`

Preferred hosted path: GitHub Actions → **Frontend Lockfile Bootstrap** → Run workflow, then inspect the generated lockfile/commit and verification result.

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
- applicable dependency/security checks.

### 3. Clean database/runtime evidence

Prove with a clean disposable PostgreSQL database:

- migrations V1–V6 apply in order;
- packaged backend starts;
- `/actuator/health` is healthy;
- representative operations work;
- isolated backup/restore drill succeeds.

### 4. Role-based smoke evidence

Verify administrator, librarian, and member journeys, including staff-account administration, circulation, reservations, reports/audit, CSV exchange, responsive navigation, and loading/empty/error states.

### 5. Accessibility evidence

Record keyboard, visible-focus, zoom/reflow, reduced-motion, and screen-reader checks from `docs/accessibility.md`.

### 6. Enable `main` branch protection/rules

After stable check names are green, enable the rules described in `docs/branch-protection.md`. The current GitHub connector does not expose branch-protection/ruleset mutation, so this host-level setting cannot be completed by a source commit from this chat.

## Exact final closure sequence for 2.0.12

1. Generate/review/commit `frontend/package-lock.json`.
2. Confirm `node scripts/check-version.mjs 2.0.12`.
3. Observe Backend CI.
4. Observe Frontend CI using the committed lockfile.
5. Observe Version Sync and CodeQL/security checks.
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

Do not add unrelated feature expansion before closing the explicit 2.0.12 release blockers. On the next continuation, first inspect current `main`, `frontend/package-lock.json`, CI statuses, and branch protection. If the lockfile has appeared, switch clean frontend verification fully to `npm ci`, reconcile documentation if needed, run/observe the remaining gates, and only then consider `v2.0.12` ready for tagging.

**Made by the Sanskar**
