# LibraCore — 2.0.12 Engineering Handoff

**Audit/update date:** 2026-08-20  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Latest `main` checkpoint immediately before this handoff update:** `1691c5b711deca6f6f2d5d456ffcb84360c6a8f5`  
**Commit identity used for project commits:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Read this file before starting another feature pass. The 2.0.12 source is feature-complete enough that release evidence and repository enforcement take priority over unrelated feature expansion.

## Current 2.0.12 status

LibraCore remains a **2.0.12 release candidate**, not a published stable release.

Current verified repository facts:

- backend Maven version: `2.0.12`;
- frontend npm version: `2.0.12`;
- `frontend/package-lock.json`: **present and committed**;
- lockfile commit: `89d1c833491d336626830baa5a69eb038c26e46f` — `build: lock frontend dependencies for 2.0.12`;
- the hosted lockfile-bootstrap closure completed real npm lockfile generation, `npm ci`, lint, strict TypeScript checking, Vitest, production build, artifact upload, and the lockfile commit successfully;
- the strict TypeScript defect exposed by the first hosted bootstrap execution was fixed without weakening compiler settings;
- an automated disposable PostgreSQL Recovery Drill is committed;
- `.github/CODEOWNERS` is committed for repository-wide and high-risk-path ownership;
- core read-only CI/release checkout steps do not persist Git credentials;
- TypeScript 7 and Node 26 type-definition major updates are intentionally deferred from the stabilization line;
- `main` branch protection remains **disabled** in observable GitHub metadata;
- the connected GitHub integration does not expose branch-protection/ruleset mutation, so that host-level enforcement cannot be enabled from this chat;
- `v2.0.12` has **not** been tagged or published.

**Do not create `v2.0.12` until the remaining gates in this document are closed.**

## Major blocker closed: real frontend dependency lock

The earlier release blocker was the absence of `frontend/package-lock.json`.

### Root cause found

The original Frontend Lockfile Bootstrap commit step checked:

```bash
git diff --quiet -- frontend/package-lock.json
```

That does not report an untracked first-time file, so a newly generated lockfile could be silently missed.

The workflow now uses:

```bash
git status --porcelain -- frontend/package-lock.json
```

which detects both an untracked first lockfile and later modifications.

Hardening commits retained:

- `2e609a43` — `ci: detect untracked frontend lockfile`
- `702d0c8a` — `ci: serialize frontend lockfile bootstrap`
- `21b43644` — `ci: preserve verified frontend lockfile artifact`
- `caa26efd` — `ci: cancel stale lockfile bootstrap runs`
- `e582fdc1` — `ci: restore hardened manual lockfile bootstrap`
- `8f7f4aa2` — `ci: preserve generated lockfile on verification failure`

The permanent bootstrap is restored to maintainer-only `workflow_dispatch` operation. It:

1. checks out `main`;
2. uses Node.js 24;
3. generates the npm lockfile with lifecycle scripts/audit/funding network work disabled;
4. installs exactly from that lockfile with `npm ci`;
5. preserves the generated lockfile as a short-lived Actions artifact before later verification;
6. runs the full frontend `npm run check` quality gate;
7. commits only the generated lockfile when it changed;
8. uses `Sanskar <sanskarin@outlook.in>` for that commit;
9. cancels superseded bootstrap runs.

The artifact is only a diagnostic/recovery aid. The canonical dependency state is the reviewed file committed in Git.

## Hosted frontend failure found and fixed

The first executable bootstrap run progressed through real lockfile generation and `npm ci`, then failed during strict TypeScript verification in `frontend/src/api.ts`.

Two `exactOptionalPropertyTypes` problems were exposed:

- the API error class assigned an optional `string | undefined` source value to a property declared with optional-property syntax in a way TypeScript 6 correctly rejected;
- the POST helper explicitly supplied `body: undefined` to `RequestInit` rather than omitting the property.

Fix:

- `correlationId` is represented explicitly as `string | undefined`;
- POST request initialization only adds the `body` property when a body actually exists;
- compiler strictness remains enabled.

Commits:

- `bd1005a5` — `fix: satisfy strict optional API request types`
- `462ab69f` — `test: cover optional API correlation identifiers`

The added regression test verifies both absent and supplied correlation identifiers.

## Frontend verification evidence now obtained

The failed bootstrap job was re-run after the API fix. Although the workflow's final aggregate conclusion was later marked `cancelled` because a newer probe superseded it through concurrency, **every substantive job step had already completed successfully**:

- checkout: success;
- Node.js setup: success;
- generate dependency lockfile: success;
- install locked dependencies with `npm ci`: success;
- frontend quality gate (`npm run check`): success;
- lockfile artifact upload: success;
- lockfile commit: success.

`npm run check` is the aggregate frontend gate and includes:

- linting;
- strict TypeScript checking;
- deterministic Vitest execution;
- production Vite build.

The resulting commit is:

- `89d1c833491d336626830baa5a69eb038c26e46f` — `build: lock frontend dependencies for 2.0.12`

The committed lockfile is npm lockfile format version 3 and records the exact resolved graph for the declared 2.0.12 frontend dependencies.

This closes the former “generate and commit `frontend/package-lock.json`” release blocker.

## Current frontend dependency policy

During release stabilization, avoid unnecessary major toolchain churn.

Changes completed:

- `.github/dependabot.yml` now defers TypeScript semver-major updates;
- `.github/dependabot.yml` now defers `@types/node` semver-major updates;
- normal non-major dependency discovery remains enabled.

Commit:

- `662d4e43` — `build: defer frontend toolchain major upgrades`

Dependabot PR disposition:

- PR #5 — TypeScript `6.0.2` → `7.0.2`: closed/deferred until post-release evaluation;
- PR #7 — `@types/node` `24.13.3` → `26.2.0`: closed/deferred while Node 24 remains the primary CI line;
- PR #6 — Vitest `4.1.7` → `4.1.10`: remains a non-blocking patch candidate and should not be merged until current release verification is green.

Do not upgrade merely for version-number freshness. Reproducible verification of the exact resolved graph is more important during release closure.

## CI and release workflow hardening completed

### Stale-run cancellation

Core workflows use workflow/ref-scoped concurrency with `cancel-in-progress: true` so an updated PR or branch does not waste runner time on obsolete verification:

- `4130b8e1` — Backend CI;
- `0d8ad341` — Frontend CI;
- `ea2256e1` — Version Sync;
- `1f1f8d42` — CodeQL;
- `cf7775e0` — Dependency Review;
- `caa26efd` — Frontend Lockfile Bootstrap.

### Current GitHub Action runtime lines

The repository was modernized to supported current action lines used by this project:

- `actions/checkout@v7`;
- `actions/setup-node@v7`;
- `actions/upload-artifact@v7`;
- `actions/dependency-review-action@v5`;
- `softprops/action-gh-release@v3`;
- `actions/setup-java@v5` remains the stable Java setup line;
- `github/codeql-action@v4` remains the CodeQL line.

Superseded GitHub Actions Dependabot PRs #1–#4 were closed after the changes were integrated directly into the hardened workflow source.

### Least-privilege checkout behavior

Read-only verification paths no longer leave checkout credentials persisted in the repository worktree.

Commits:

- `31549a2d` — Backend CI;
- `e78d93b5` — Frontend CI;
- `8b6fa428` — Version Sync;
- `63a1adf5` — CodeQL;
- `0afa4a15` — Dependency Review;
- `58599cf6` — release source checkout.

The lockfile bootstrap is intentionally different because its documented purpose includes pushing the generated lockfile back to `main`.

## Release workflow guarantees now present

The tagged release workflow is fail-closed and currently performs/guards:

- frontend lockfile presence;
- tag/backend/frontend version agreement;
- Java 25 setup;
- Maven verification against PostgreSQL;
- version-independent packaged backend JAR discovery;
- startup of the exact packaged JAR intended for release;
- `/actuator/health` verification against PostgreSQL;
- reproducible frontend `npm ci`;
- full frontend quality gate;
- production frontend packaging;
- backend artifact collection;
- SHA-256 checksum generation;
- GitHub release publication only after the preceding steps;
- packaged backend cleanup/logging on failure.

Important commits include:

- `3eb17e29` — `ci: harden 2.0.12 release verification`
- `db3fe2a4` — `ci: fail clearly when release lockfile is absent`
- `7ee2af63` — `ci: verify packaged backend before release`

Do not use the existence of this workflow as proof that a release passed. The actual tag-triggered run must still be observed after all pre-tag gates close.

## Automated PostgreSQL Recovery Drill added

A new workflow is committed:

- `.github/workflows/recovery-drill.yml`
- commit `374269e5` — `ci: add automated PostgreSQL recovery drill`

The workflow uses disposable PostgreSQL 18 databases and fictional marker content. It:

1. packages the backend;
2. starts the packaged backend against a disposable source database so Flyway applies the actual schema;
3. health-checks that source instance;
4. inserts a fictional recovery marker;
5. records the Flyway migration-history count;
6. invokes the repository's real `scripts/backup.sh` through PostgreSQL 18 client tooling;
7. creates a second empty restore database;
8. invokes the real `scripts/restore.sh` with the destructive restore opt-in only for that disposable target;
9. verifies the checksum through the restore script;
10. compares restored/source migration-history counts;
11. verifies the fictional marker survived;
12. starts the packaged backend against the restored database;
13. requires `/actuator/health` again;
14. destroys the temporary logical dump even on failure.

The workflow is available manually, runs on relevant migration/recovery changes, and has a recurring scheduled drill. It does **not** upload database dump files as artifacts.

Passing this workflow proves the repository logical backup/restore path plus packaged application startup against a restored schema. It does not replace organization-specific RPO/RTO, encryption/key, production-scale, or representative-data recovery exercises.

Documentation was updated in `docs/backup-restore.md` accordingly.

## Code ownership and branch-protection preparation

Added:

- `.github/CODEOWNERS`
- commit `7939c225` — `governance: add repository code ownership`

Default ownership is `@sanskarIN`, with explicit ownership entries for:

- `.github/` CI/release/governance controls;
- security policy files;
- backend security package;
- application security configuration;
- Flyway migrations;
- backup/restore scripts;
- recovery/release documentation.

`docs/branch-protection.md` now recommends requiring code-owner review and Recovery Drill for migration/recovery-sensitive changes.

Important limitation: CODEOWNERS is not enforcement by itself. `main` branch metadata still reports:

- `protected: false`;
- required-status-check enforcement: off.

The connected GitHub integration does not expose a ruleset/branch-protection mutation action, so this remains a manual repository-host setting after stable check names are confirmed.

## Current consolidated release-verification PR

Draft PR #11 is the disposable verification harness:

- title: `ci: verify 2.0.12 release candidate`;
- branch: `automation/release-verification-2`;
- base: `main`;
- must **not** be merged;
- changes are only temporary marker files/comment-only trigger material.

Current PR verification head after its refresh:

- `555450dd086b35613bc6e481c8c9562d7f7177f3`

The PR currently exposes these six workflow runs:

- Recovery Drill — run `32362295920`;
- CodeQL — run `32362295949`;
- Frontend CI — run `32362296005`;
- Dependency Review — run `32362295911`;
- Version Sync — run `32362295969`;
- Backend CI — run `32362295973`.

At the time this handoff section was written, those current-source runs were **queued**, not failed. Do not reinterpret queued as pass or failure. Inspect the final job conclusions/logs before updating release status.

Earlier probe evidence retained:

- a previous same-repository CodeQL probe completed successfully for Java/Kotlin and JavaScript/TypeScript;
- temporary lockfile probe PRs were closed without merge after their diagnostic purpose;
- old diagnostic branches were force-aligned back to clean `main` rather than left divergent.

## Product implementation present

The release candidate contains the intended end-to-end LibraCore implementation across:

- Spring Boot modular-monolith backend;
- PostgreSQL persistence and Flyway V1–V6;
- catalog metadata, branches, shelves, physical copies, accession/barcode/QR lookup, and search;
- members and account linkage;
- administrator/librarian/member authentication and authorization;
- administrator staff-account lifecycle management;
- issue, return, renewal, reservations/waitlists;
- circulation policy and fine assessment/settlement;
- dashboard, overdue reporting, audit search;
- bounded CSV import/export;
- notification scheduling and mock/SMTP gateways;
- responsive React/TypeScript UI with role-aware navigation;
- light/dark/system themes;
- mobile navigation coverage for role-authorized routes;
- backup/restore helpers and automated recovery verification;
- security/privacy/threat-model/governance documentation;
- backend/frontend/version/security/dependency/recovery/release automation.

## Important previous source fixes retained

Earlier closure work already fixed/completed:

- missing React application entry point;
- incomplete circulation, reservations, reports, and settings routes;
- administrator staff-account frontend workflow;
- login strict optional-property typing;
- asynchronous staff form reset safety;
- mobile route truncation;
- exact direct frontend dependency/tool pinning;
- aggregate `npm run check`;
- version-independent backend JAR discovery in CI/release;
- build-derived Settings version display;
- cross-manifest version guard;
- security/privacy/threat-model documentation;
- contribution/conduct/support policies;
- issue forms, PR template, Dependabot, CodeQL, Dependency Review;
- architecture/API/setup/development/testing/deployment/backup/accessibility/performance/release/troubleshooting/branch-protection/ADR documentation;
- exhaustive tracked-file repository reference.

## Verification evidence that may be claimed now

The following are supported by observed tool/workflow evidence:

- backend/frontend manifests identify `2.0.12`;
- a real npm-generated lockfile is committed;
- lockfile generation completed successfully on a GitHub-hosted runner;
- `npm ci` completed successfully with the generated graph;
- frontend lint completed successfully during the successful bootstrap rerun;
- strict TypeScript checking completed successfully after the API fix;
- Vitest completed successfully during the successful rerun;
- production Vite build completed successfully during the successful rerun;
- the lockfile commit step completed successfully;
- earlier CodeQL probe passed both configured language families;
- core workflow source contains stale-run cancellation and least-privilege checkout changes;
- the release workflow source contains packaged backend startup/health verification;
- the Recovery Drill workflow source is syntactically accepted by GitHub Actions and its first run is registered/queued;
- `main` is currently unprotected.

## Claims that must NOT be made yet

Do not claim any of the following until direct evidence exists:

- current PR #11 Backend CI passed;
- current PR #11 Frontend CI passed;
- current PR #11 Version Sync passed;
- current PR #11 CodeQL passed;
- current PR #11 Dependency Review passed;
- current PR #11 Recovery Drill passed;
- final browser role smoke testing is complete;
- administrator/librarian/member journeys are all release-verified;
- manual accessibility review is complete;
- `main` branch protection/code-owner enforcement is active;
- the tagged release workflow passed for `v2.0.12`;
- `v2.0.12` is published.

## Remaining pre-tag release gates

### 1. Finish the current six-check verification matrix

Inspect and require successful conclusions for:

- Backend CI;
- Frontend CI;
- Version Sync;
- CodeQL;
- Dependency Review;
- Recovery Drill.

If any fails, inspect the exact job log and fix the source/workflow on `main`; do not paper over failures in documentation.

### 2. Record role-based browser smoke evidence

Verify administrator, librarian, and member journeys, including at minimum:

- sign in/current session/logout;
- administrator staff-account create/filter/enable-disable/password reset;
- catalog/search/detail/copy workflows;
- member administration;
- issue/return/renew;
- reservations/waitlists;
- fines/policy behavior;
- reports/audit;
- CSV import/export with fictional data;
- responsive navigation;
- loading/empty/validation/error states.

Browser-level E2E automation is a 2.1.x roadmap item, so 2.0.12 still requires recorded manual smoke evidence unless that automation is intentionally brought forward and verified.

### 3. Record manual accessibility evidence

Use `docs/accessibility.md` and record:

- keyboard-only navigation;
- visible focus;
- logical focus order;
- zoom/reflow;
- reduced-motion behavior;
- accessible labels/names;
- non-color-only status meaning;
- screen-reader landmark/name basics.

Automated checks do not replace this manual evidence.

### 4. Enable `main` branch protection/ruleset

After the exact stable check names are known, enable the policy documented in `docs/branch-protection.md`, including appropriate code-owner review.

The connected tool cannot perform this host mutation. Do not mark this complete until GitHub branch metadata confirms protection is enabled.

### 5. Final documentation/checkpoint reconciliation

After the above:

- close PR #11 without merge;
- align its temporary branch back to final `main`;
- update `CHANGELOG.md`;
- update `ROADMAP.md`;
- update `docs/releases/2.0.12.md`;
- update this file;
- refresh `docs/repository-reference.md` for the committed lockfile, Recovery Drill, and CODEOWNERS;
- confirm no temporary probe markers exist on `main`;
- confirm no known blocker/critical defects remain.

### 6. Tag only after all pre-tag gates close

```bash
git tag -a v2.0.12 -m "LibraCore v2.0.12"
git push origin v2.0.12
```

Then observe the tag-triggered Release workflow itself. Publication is complete only if that release run succeeds and expected artifacts/checksums are present.

## Current documentation map

- overview and setup entry point: `README.md`
- exhaustive tracked-file purpose map: `docs/repository-reference.md`
- release-candidate checklist/evidence: `docs/releases/2.0.12.md`
- release process: `docs/release.md`
- testing/quality gates: `docs/testing.md`
- backup/recovery: `docs/backup-restore.md`
- branch protection: `docs/branch-protection.md`
- roadmap: `ROADMAP.md`
- delivered/recent changes: `CHANGELOG.md`
- architecture: `docs/architecture.md`
- API: `docs/api.md`
- setup: `docs/setup.md`
- development: `docs/development.md`
- deployment: `docs/deployment.md`
- accessibility: `docs/accessibility.md`
- performance: `docs/performance.md`
- troubleshooting: `docs/troubleshooting.md`
- security/privacy: `SECURITY.md`, `PRIVACY.md`, `THREAT_MODEL.md`
- architecture decisions: `docs/adr/`
- continuation truth: this file.

## Continuation rule

Do **not** start unrelated feature expansion on the next continuation. First inspect:

1. current `main` head;
2. PR #11 six-check workflow conclusions and logs;
3. `main` branch-protection metadata;
4. current manual smoke/accessibility evidence.

Fix any failing automated gate, finish the manual evidence, enable host enforcement, reconcile docs, and only then tag 2.0.12.

**Made by the Sanskar**
