# LibraCore — 2.0.12 Final Engineering Handoff

**Audit/update date:** 2026-08-20  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Latest clean `main` checkpoint immediately before this handoff update:** `9b57de9ebe65684ffbb5ab191dac4c286fe10ea2`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This file is the canonical continuation record for LibraCore. Read it before adding more features. It records completed implementation, verification evidence, temporary diagnostic work, source-side hardening, dependency/update decisions, and the exact gates that still prevent `v2.0.12` from being declared release-ready.

## 2026-08-20 continuation — latest result

This continuation intentionally stayed on the explicit 2.0.12 release-closure path instead of adding unrelated product features.

Latest observable source/repository state before this handoff update:

- backend Maven version: `2.0.12`;
- frontend npm version: `2.0.12`;
- `frontend/package-lock.json`: **still absent** at the latest observable check;
- `main` branch protection: **disabled** (`protected: false`; required checks are not enforced);
- permanent Frontend Lockfile Bootstrap workflow: restored to **manual `workflow_dispatch` only** after diagnostics;
- both temporary lockfile probe PRs are **closed without merge**;
- temporary probe branches `automation/lockfile-bootstrap-2` and `automation/lockfile-bootstrap-3` were force-aligned to the clean `main` checkpoint so they no longer contain divergent workflow definitions;
- CodeQL was successfully observed on a temporary same-repository probe for both Java/Kotlin and JavaScript/TypeScript;
- a corrected lockfile-bootstrap run was successfully created and became observable, but its GitHub-hosted job remained queued during this continuation and therefore produced no lockfile execution evidence;
- the repository's GitHub Actions definitions were upgraded to current supported Node 24 action lines where applicable;
- the GitHub Actions Dependabot PRs that became redundant after those direct workflow upgrades were closed without merge;
- only frontend dependency-update PRs remain open, and they are intentionally not merged until the lockfile/reproducible frontend verification gate exists;
- GitHub's public status page reported Actions operational during the queue observation, so no platform-wide Actions outage was used as an explanation for the repository-specific queued job;
- the local execution environment still could not reach the npm registry reliably, so no dependency lock was generated locally and no lockfile was fabricated.

**Do not tag or publish `v2.0.12` yet.**

## Critical lockfile-bootstrap bug found and fixed

The most important engineering finding in this continuation was a real bug in `.github/workflows/lockfile-bootstrap.yml`.

The original commit step used:

```bash
git diff --quiet -- frontend/package-lock.json
```

That command does not report an untracked file. Because `frontend/package-lock.json` did not exist yet, a successful first-time npm generation could leave the file untracked and the workflow could incorrectly conclude that the lockfile was already synchronized. That explains why earlier bootstrap attempts could fail to publish a newly generated lockfile even if npm generation itself succeeded.

The permanent workflow now checks:

```bash
git status --porcelain -- frontend/package-lock.json
```

This detects both the first untracked lockfile and later modifications.

Permanent lockfile-bootstrap hardening commits:

- `2e609a43` — `ci: detect untracked frontend lockfile`
- `702d0c8a` — `ci: serialize frontend lockfile bootstrap`
- `21b43644` — `ci: preserve verified frontend lockfile artifact`
- `caa26efd` — `ci: cancel stale lockfile bootstrap runs`
- `e582fdc1` — `ci: restore hardened manual lockfile bootstrap`
- `ee0a75e7` — `ci: upgrade lockfile bootstrap actions`

The hardened workflow now:

- checks out `main` explicitly with `actions/checkout@v7`;
- uses Node.js 24 through `actions/setup-node@v7`;
- generates the lockfile with `npm install --package-lock-only --ignore-scripts --no-audit --no-fund`;
- verifies installation with `npm ci --ignore-scripts --no-audit --no-fund`;
- runs the complete frontend quality command `npm run check`;
- uploads the verified lockfile through `actions/upload-artifact@v7` as a short-lived `frontend-package-lock` artifact before attempting the repository commit;
- detects untracked or modified lockfile state correctly;
- commits with `Sanskar <sanskarin@outlook.in>`;
- pushes only the generated `frontend/package-lock.json` to `main` when it changed;
- uses workflow concurrency with stale-run cancellation;
- is permanently restored to `workflow_dispatch` only after the diagnostic probes.

The uploaded lockfile artifact is a recovery/inspection aid only. The canonical release dependency state remains the reviewed `frontend/package-lock.json` committed in the repository.

## Temporary lockfile diagnostics and cleanup

Several temporary source-controlled probes were used because the connected GitHub interface did not expose the normal manual `workflow_dispatch` invocation for this workflow.

### Probe PR #8

A temporary branch/PR was used to obtain observable CI/security execution.

Result:

- CodeQL workflow run `32351669280` completed successfully;
- JavaScript/TypeScript CodeQL job: **success**;
- Java/Kotlin CodeQL job: **success**;
- PR #8 was closed without merge after capturing the evidence;
- its branch was later force-aligned to clean `main`.

This proves the CodeQL configuration worked on that probe source. It does **not** replace final check evidence on the exact intended release commit.

### Probe PR #9

A second same-repository probe made the corrected Frontend Lockfile Bootstrap workflow observable.

Observed run:

- workflow: `Frontend Lockfile Bootstrap`;
- run ID: `32352245167`;
- run number: `11`;
- job: `generate-and-verify`;
- observed state throughout the diagnostic window: **queued**;
- no steps started;
- no artifact was produced;
- no `build: lock frontend dependencies for 2.0.12` commit appeared on `main`;
- no `frontend/package-lock.json` appeared on `main`.

After the source-side workflow fixes were completed, PR #9 was closed without merge, the permanent workflow trigger was restored to manual-only operation, and its temporary branch was force-aligned to clean `main`.

No diagnostic trigger file or divergent diagnostic workflow definition is intentionally retained in the product source.

## Release workflow hardened further

The tagged release workflow previously verified the backend build but did not prove that the exact packaged JAR could actually start before publication.

Commits:

- `7ee2af63` — `ci: verify packaged backend before release`
- `34b96960` — `ci: upgrade release actions to Node 24 lines`

The release workflow now:

- checks out tagged source through `actions/checkout@v7`;
- refuses to continue if the committed frontend lockfile is absent;
- sets up Node.js 24 through `actions/setup-node@v7`;
- validates the pushed release tag against backend/frontend executable manifests before expensive build work;
- retains `actions/setup-java@v5` as the stable production Java setup line;
- verifies and packages the backend against PostgreSQL;
- discovers the packaged backend JAR without a historical hard-coded filename;
- starts that exact packaged JAR;
- requires `http://localhost:8080/actuator/health` to become healthy;
- installs frontend dependencies reproducibly through `npm ci --ignore-scripts --no-audit --no-fund`;
- runs the full frontend quality gate;
- packages the production frontend bundle;
- creates SHA-256 checksums;
- publishes GitHub release artifacts through `softprops/action-gh-release@v3` only after the preceding gates;
- always attempts to stop the temporary packaged backend process;
- prints the packaged backend log when the release job fails.

This strengthens the distinction between “Maven built a JAR” and “the exact artifact intended for publication starts successfully against PostgreSQL.”

## CI freshness and runner-efficiency hardening

Superseded branch/PR runs should not consume runner capacity or report obsolete results as the newest verification. Concurrency with `cancel-in-progress: true` was therefore added to the core verification workflows.

Commits:

- `4130b8e1` — `ci: cancel stale backend verification runs`
- `0d8ad341` — `ci: cancel stale frontend verification runs`
- `ea2256e1` — `ci: cancel stale version sync runs`
- `1f1f8d42` — `ci: cancel stale CodeQL runs`
- `cf7775e0` — `ci: cancel stale dependency review runs`
- `caa26efd` — `ci: cancel stale lockfile bootstrap runs`

The groups are workflow/ref scoped so an updated PR replaces its own stale verification without conflating unrelated refs.

## GitHub Actions runtime modernization

The repository's core reusable Actions references were reviewed against the currently supported upstream lines and then upgraded directly in the hardened workflow source instead of blindly merging older Dependabot diffs onto pre-hardening workflow files.

Current workflow action lines after this continuation:

- `actions/checkout@v7` across Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, lockfile bootstrap, and release;
- `actions/setup-node@v7` in Frontend CI, Version Sync, lockfile bootstrap, and release;
- `actions/upload-artifact@v7` in the lockfile bootstrap;
- `actions/dependency-review-action@v5` in Dependency Review;
- `softprops/action-gh-release@v3` in the tagged release workflow;
- `actions/setup-java@v5` intentionally retained as the stable production Java setup line;
- `github/codeql-action@v4` retained for CodeQL initialization/autobuild/analysis.

Upgrade commits:

- `1a771bbb` — `ci: upgrade backend checkout action to v7`
- `03c04e04` — `ci: upgrade frontend core actions to v7`
- `945cbd55` — `ci: upgrade version sync actions to v7`
- `02038a5f` — `ci: upgrade CodeQL checkout action to v7`
- `007e7a6c` — `ci: upgrade dependency review actions`
- `ee0a75e7` — `ci: upgrade lockfile bootstrap actions`
- `34b96960` — `ci: upgrade release actions to Node 24 lines`

A repository search after the sweep returned no remaining references to:

- `actions/checkout@v5`;
- `actions/setup-node@v5`;
- `actions/upload-artifact@v4`;
- `actions/dependency-review-action@v4`;
- `softprops/action-gh-release@v2`.

Dependabot cleanup after absorbing these upgrades directly:

- PR #1 (`actions/dependency-review-action` v4 → v5): closed without merge;
- PR #2 (`actions/setup-node` v5 → v7): closed without merge;
- PR #3 (`softprops/action-gh-release` v2 → v3): closed without merge;
- PR #4 (`actions/checkout` v5 → v7): closed without merge.

Those PRs became redundant because `main` now contains the supported upgrades together with the additional 2.0.12 workflow hardening that was not present on the old Dependabot branch bases.

Frontend dependency PRs intentionally remain open:

- PR #5: TypeScript `6.0.2` → `7.0.2`;
- PR #6: Vitest `4.1.7` → `4.1.10`;
- PR #7: `@types/node` `24.13.3` → `26.2.0`.

They are **not** being merged as part of this release-closure pass because `frontend/package-lock.json` is still absent. Changing direct frontend dependencies before the real lockfile exists would enlarge the unresolved dependency surface while reproducible `npm ci` verification is still unavailable. Review those PRs only after the lockfile gate is closed and the exact resolved graph can be tested.

## Documentation refreshed in this continuation

Documentation was updated to match actual source behavior rather than aspirational release claims.

Commits:

- `a5543b8b` — `docs: document hardened 2.0.12 release verification`
- `d140b639` — `docs: refresh 2.0.12 hardening evidence`
- `0500d7b3` — `docs: record 2.0.12 CI hardening`
- `06547fad` — `docs: record current GitHub Actions runtime lines`
- `9b57de9e` — `docs: align 2.0.12 notes with action upgrades`
- this handoff update — canonical continuation truth

Updated documentation now explains:

- correct first-lockfile detection;
- short-lived verified lockfile artifact behavior;
- reproducible `npm ci` flags;
- packaged-backend runtime verification before release publication;
- stale-run cancellation behavior;
- current supported GitHub Actions runtime lines;
- the difference between probe evidence and final intended-release-commit evidence;
- why frontend dependency upgrades remain gated until the dependency lock exists;
- the continuing lockfile and branch-protection blockers.

## Version 2.0.12 identity

LibraCore source remains explicitly prepared as **2.0.12**:

- `backend/pom.xml`: `2.0.12`;
- `frontend/package.json`: `2.0.12`;
- Vite reads frontend package metadata and injects `__APP_VERSION__`;
- the Settings page displays the build-derived version instead of a duplicated hard-coded value;
- `scripts/check-version.mjs` validates backend/frontend equality and optionally an expected version/tag;
- `.github/workflows/version-sync.yml` enforces manifest synchronization;
- `.github/workflows/release.yml` verifies tag/manifest agreement before release build work.

Relevant preceding commits retained:

- `2b1ce0fd` — `release: set backend version 2.0.12`
- `774bc10f` — `release: set frontend version 2.0.12`
- `2464145b` — `build: inject frontend package version`
- `22b11afc` — `build: type injected application version`
- `c0d3cfe4` — `release: show build version in settings`
- `2da8b109` — `build: add cross-manifest version guard`
- `0f93167e` — `ci: enforce synchronized release versions`

A dependency-free fixture execution previously confirmed the guard behavior for `2.0.12`, but this does not substitute for final full-project verification.

## Product implementation retained

The repository continues to contain the end-to-end LibraCore implementation across:

- Spring Boot modular-monolith backend;
- PostgreSQL persistence and Flyway migrations V1–V6;
- books, authors, publishers, categories, branches, shelves, physical copies, accession/barcode/QR lookup, and search;
- members, account linking, administrator/librarian/member roles, authentication, and authorization;
- issue, return, renewal, reservations/waitlists, circulation policy, fine assessment and settlement;
- dashboard, overdue reporting, audit search, CSV import/export, and notification scheduling/gateways;
- administrator staff-account creation, role filtering, enable/disable, password reset, session revocation, and active-session visibility;
- responsive React/TypeScript frontend with light/dark/system themes and role-aware navigation;
- role-complete horizontally scrollable mobile navigation;
- backup/restore helpers;
- security/privacy/threat-model/contribution/support documentation;
- Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, Dependabot, release automation, issue/PR templates, and funding metadata.

## Important previous final-audit work retained

The preceding 2.0.12 closure work already completed or fixed:

- missing frontend application entry point;
- circulation, reservation, reports, and settings pages;
- administrator staff-account frontend workflow;
- strict `exactOptionalPropertyTypes` login-prop defect;
- asynchronous staff form reset safety;
- mobile route truncation;
- exact direct frontend dependency/tool version pinning;
- aggregate `npm run check` command;
- version-independent packaged-backend discovery in Backend CI;
- release tag/version synchronization enforcement;
- read-only lockfile-based Frontend CI;
- security/privacy/threat model;
- contribution/conduct/support policies;
- issue forms, PR template, Dependabot, CodeQL, dependency review;
- architecture/API/setup/development/testing/deployment/backup/accessibility/performance/release/troubleshooting/branch-protection/ADR documentation;
- exhaustive tracked-file repository reference.

Key preceding commits include:

- `a998e189` — `ci: make backend startup version independent`
- `3eb17e29` — `ci: harden 2.0.12 release verification`
- `db3fe2a4` — `ci: fail clearly when release lockfile is absent`
- `a9466a5e` — `ci: make frontend verification read-only and reproducible`
- `5a99a727` — `ci: add explicit frontend lockfile bootstrap`
- `1dec12af` — README 2.0.12 alignment
- `628efd02` — 2.0.12 release process
- `f18fab24` — 2.0.12 roadmap
- `03b78f4d` — 2.0.12 changelog
- `84e5a747` — 2.0.12 release-candidate notes
- `7b66a79b` — exhaustive repository reference refresh
- `2254688f` — branch-protection/check alignment
- `84604498` — testing gate alignment
- `dfea1e3e` — setup/lockfile workflow alignment
- `220e51d6` — contribution workflow alignment
- `a434f9df` — sealed earlier 2.0.12 checkpoint
- `7fc63652` — recorded the preceding lockfile-bootstrap continuation

## Verification evidence actually observed

The following statements are based on concrete repository/tool evidence, not assumptions:

- backend and frontend manifests both identify `2.0.12`;
- permanent Frontend Lockfile Bootstrap source contains correct untracked-file detection;
- permanent bootstrap is restored to `workflow_dispatch` only;
- permanent bootstrap preserves its verified generated lockfile as an Actions artifact before the commit step;
- Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and bootstrap source now include stale-run cancellation where applicable;
- release workflow source now starts and health-checks the packaged backend before release publication;
- current supported action lines have been applied consistently to the affected workflow files;
- repository searches found no remaining old action references listed in the runtime-modernization section;
- temporary CodeQL probe completed successfully for both configured language families;
- the corrected lockfile bootstrap probe created an observable run, but its job never started during the observation window;
- `frontend/package-lock.json` remained absent at the latest check;
- branch metadata still reported `main` unprotected;
- temporary PRs #8 and #9 are closed without merge;
- temporary branches used for those probes were aligned back to clean `main`;
- CI Dependabot PRs #1–#4 are closed after their supported upgrades were absorbed directly into `main`;
- only frontend dependency PRs #5–#7 remain open at the latest PR-state check;
- local npm access timed out, so it was not used to manufacture dependency metadata.

## Claims intentionally not made

This continuation does **not** claim:

- the corrected lockfile-bootstrap job actually executed to completion;
- `frontend/package-lock.json` has been generated or reviewed;
- final Frontend CI passed;
- final Backend CI passed on the exact intended release commit;
- final Version Sync passed on the exact intended release commit;
- final CodeQL passed on the exact intended release commit;
- Dependency Review passed on the exact intended release commit;
- any of frontend Dependabot PRs #5–#7 are compatible with the complete application until tested against a real lockfile;
- a clean disposable PostgreSQL/Flyway V1–V6 release drill is complete;
- the packaged-backend release workflow itself has been exercised through a real `v2.0.12` tag;
- representative administrator/librarian/member smoke journeys are complete;
- browser/accessibility release evidence is complete;
- an isolated backup/restore drill is complete;
- `main` branch protection is enabled;
- `v2.0.12` is release-green, tagged, or published.

## Remaining 2.0.12 release blockers

### 1. Generate, review, and commit `frontend/package-lock.json`

Preferred hosted path:

GitHub Actions → **Frontend Lockfile Bootstrap** → **Run workflow**.

The permanent workflow is now fixed for first-time untracked lockfiles and uses the current supported Node 24 action lines. After the job runs, review the generated lockfile and resulting commit rather than assuming success from the workflow trigger alone.

Equivalent supported local path:

```bash
cd frontend
npm install --package-lock-only --ignore-scripts --no-audit --no-fund
npm ci --ignore-scripts --no-audit --no-fund
npm run check
git add package-lock.json
git commit -m "build: lock frontend dependencies for 2.0.12"
git push
```

Do not hand-write, guess, or synthesize the lockfile.

### 2. Observe final CI/security checks

On the exact intended release commit, observe successful:

- Backend CI;
- Frontend CI using the committed lockfile;
- Version Sync;
- CodeQL;
- applicable dependency/security checks.

Probe success is useful workflow evidence but is not the final release gate.

### 3. Review pending frontend dependency updates after the lockfile gate

Once the dependency lock exists and reproducible CI can execute, review the still-open frontend dependency PRs independently rather than batching major-version changes blindly:

- PR #5 — TypeScript 7;
- PR #6 — Vitest 4.1.10;
- PR #7 — `@types/node` 26.

For each update, regenerate/review the lockfile as appropriate and require lint, typecheck, tests, and production build success before merge. These updates are not prerequisites for publishing 2.0.12 unless a security finding makes one release-blocking.

### 4. Clean database/runtime evidence

Using a clean disposable PostgreSQL database, prove:

- Flyway migrations V1–V6 apply in order;
- the packaged backend starts;
- `/actuator/health` is healthy;
- representative operations work;
- no migration/startup errors are hidden in logs.

### 5. Role-based smoke evidence

Verify administrator, librarian, and member journeys, including:

- authentication/session behavior;
- staff-account administration;
- catalog/copy/member operations;
- circulation and renewals;
- reservations/waitlists;
- fines/policy behavior;
- reports/audit;
- CSV exchange;
- responsive role-aware navigation;
- loading, empty, validation, and error states.

### 6. Accessibility evidence

Record the manual checks required by `docs/accessibility.md`, including keyboard operation, visible focus, zoom/reflow, reduced motion, and screen-reader behavior.

### 7. Backup/restore evidence

Complete an isolated current backup/restore drill and record the result without destructive assumptions about production data.

### 8. Enable `main` branch protection/rules

After stable check names are green, enable the rules described in `docs/branch-protection.md`.

The connected GitHub interface used in this continuation does not expose branch-protection/ruleset mutation, so this host-level setting was not changed from source commits. Latest branch metadata still reports protection disabled.

## Exact final closure sequence for 2.0.12

1. Run the corrected Frontend Lockfile Bootstrap workflow or the documented supported local npm commands.
2. Review and commit the real generated `frontend/package-lock.json`.
3. Confirm `node scripts/check-version.mjs 2.0.12`.
4. Observe Backend CI on the intended release commit.
5. Observe Frontend CI using the committed lockfile.
6. Observe Version Sync, CodeQL, and applicable security/dependency checks.
7. Review the open frontend dependency PRs separately; do not let non-required major upgrades destabilize the release candidate.
8. Verify clean PostgreSQL/Flyway packaged startup and `/actuator/health`.
9. Execute role-based smoke tests.
10. Record accessibility evidence.
11. Execute the isolated backup/restore drill.
12. Enable `main` branch protection using stable green check names.
13. Re-read `CHANGELOG.md`, `ROADMAP.md`, `docs/releases/2.0.12.md`, `docs/release.md`, and this handoff.
14. Only then create and push `v2.0.12`.
15. Observe the tag-triggered release workflow itself before declaring publication complete.

Release command **only after every pre-tag gate is closed**:

```bash
git tag -a v2.0.12 -m "LibraCore v2.0.12"
git push origin v2.0.12
```

## Documentation map

- overview/current source version: `README.md`
- complete tracked-file map: `docs/repository-reference.md`
- 2.0.12 release-candidate notes: `docs/releases/2.0.12.md`
- release process: `docs/release.md`
- delivered/recent changes: `CHANGELOG.md`
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
- continuation truth: this file

## Continuation rule

Do not add unrelated feature expansion before closing the explicit 2.0.12 release blockers. On the next continuation, first inspect current `main`, `frontend/package-lock.json`, the latest CI/check evidence, open dependency PRs, and branch protection. The first expected source-changing event is the real npm-generated lockfile commit. After that, run/observe the final verification matrix, review pending dependency updates separately, complete runtime/smoke/accessibility/backup gates, enable branch protection, and only then consider `v2.0.12` ready for tagging.

**Made by the Sanskar**
