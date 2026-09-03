# LibraCore — 0.1.1 Engineering Handoff

**Audit/update date:** 2026-09-03  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Active release line:** `0.1.1`  
**Commit identity used for project commits:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. The project is being prepared for `v0.1.1`. Release work must remain evidence-driven: source changes, CI results, recovery evidence, and release publication status are separate facts.

## Current release state

The active release target is **`v0.1.1`**.

Completed in this release-rebaseline pass:

- backend Maven version changed from `2.0.12` to `0.1.1`;
- frontend npm version changed from `2.0.12` to `0.1.1`;
- release documentation changed to use `v0.1.1`;
- changelog now contains a dedicated `0.1.1` release-candidate section;
- this handoff document has been rebaselined from the former `2.0.12` release-candidate line.

The existing `v1.0.0` tag is intentionally not being rewritten. Its earlier release attempt was rejected because source manifests still declared `2.0.12`. The next release target is therefore a new `v0.1.1` tag created only after the release source has passed its gates.

## Release commits from this pass

The rebaseline was intentionally split into reviewable commits:

- `7defea93` — `chore(release): rebaseline backend version to 0.1.1`
- `013a93f5` — `chore(release): rebaseline frontend version to 0.1.1`
- `3becf61e` — `docs(release): switch release process to 0.1.1`
- `40af5b6d` — `docs(changelog): establish 0.1.1 release notes baseline`
- this handoff update records the release-line transition and remaining gates.

All project commits use the requested commit identity `Sanskar <sanskarin@outlook.in>`.

## Lockfile closure required

`frontend/package-lock.json` is already committed and remains the canonical generated dependency graph. The package declaration version is now `0.1.1`, while the existing lockfile was generated for the former `2.0.12` manifest metadata.

Before the final `v0.1.1` tag, the lockfile must be regenerated with the repository's supported Node/npm toolchain and reviewed as a generated file. Do **not** hand-edit or synthesize the lockfile. The final release source should contain a lockfile whose root package metadata agrees with `0.1.1`.

## Release workflow evidence

The release workflow is fail-closed on manifest/tag mismatch. A previous `v1.0.0` attempt reached version validation and stopped with:

```text
Version mismatch: manifests=2.0.12, expected=1.0.0
```

That proves the version guard is active. It is not evidence that `v0.1.1` has passed release verification.

The release workflow also guards:

- committed frontend lockfile;
- tag/backend/frontend version agreement;
- Java 25 backend verification;
- PostgreSQL-backed packaged backend startup;
- `/actuator/health` verification;
- reproducible frontend `npm ci`;
- frontend lint, strict type checking, tests, and production build;
- backend artifact discovery;
- SHA-256 checksums;
- publication only after preceding gates succeed;
- cleanup and backend-log diagnostics on failure.

## Recovery Drill

An automated disposable PostgreSQL Recovery Drill exists at `.github/workflows/recovery-drill.yml`. It exercises the repository's actual backup and restore scripts, verifies migration history and fictional marker data, then starts the packaged backend against the restored database.

A later CI hardening change added stronger PostgreSQL readiness and backend startup diagnostics after an earlier drill stopped during source-database migration. That hardening must be included in the release source before final release verification.

Do not claim Recovery Drill success until a current run on the exact intended release source has a successful conclusion.

## Frontend verification history

The hosted frontend lockfile/bootstrap work previously demonstrated real lockfile generation, reproducible `npm ci`, linting, strict TypeScript verification, deterministic Vitest execution, production Vite build, artifact preservation, and lockfile commit.

The earlier `exactOptionalPropertyTypes` issue in `frontend/src/api.ts` was fixed without weakening compiler strictness, with regression coverage for optional API correlation identifiers.

Those historical results remain useful engineering evidence but must not be represented as a fresh `v0.1.1` release-gate pass unless the corresponding current-source workflows succeed.

## Product implementation already present

LibraCore contains the intended end-to-end library-management implementation across:

- Spring Boot modular-monolith backend;
- PostgreSQL persistence and Flyway migrations;
- catalog metadata, branches, shelves, physical copies, accession/barcode/QR lookup, and search;
- members and account linkage;
- administrator/librarian/member authentication and authorization;
- staff-account lifecycle management;
- issue, return, renewal, reservations/waitlists;
- circulation policy and fine assessment/settlement;
- dashboard, overdue reporting, and audit search;
- bounded CSV import/export;
- notification scheduling and mock/SMTP gateways;
- responsive React/TypeScript UI with role-aware navigation;
- light/dark/system themes;
- backup/restore helpers and automated recovery verification;
- security/privacy/threat-model/governance documentation;
- backend/frontend/version/security/dependency/recovery/release automation.

## Remaining release gates

Before tagging `v0.1.1`, complete and directly verify:

1. regenerate and commit the `0.1.1`-consistent frontend lockfile;
2. run `node scripts/check-version.mjs 0.1.1`;
3. pass current Backend CI;
4. pass current Frontend CI;
5. pass Version Sync;
6. pass CodeQL;
7. pass Dependency Review;
8. pass the current Recovery Drill on the intended release source;
9. run role-based browser smoke journeys;
10. perform the documented manual accessibility review;
11. review repository links/configuration and release artifacts;
12. confirm no secrets or private data are tracked;
13. confirm `main` branch-protection/code-owner enforcement status and document the host-level limitation if it remains disabled;
14. create `v0.1.1` only after the above evidence is green.

## Tagging rule

The intended release tag is:

```text
v0.1.1
```

Create it from the exact commit containing the final verified release source:

```bash
git tag -a v0.1.1 -m "LibraCore v0.1.1"
git push origin v0.1.1
```

Do not create or publish the tag early. Do not force-move a published release tag. If a pre-publication error is found, fix the source and create the release tag from the corrected commit.

## Planned v0.1.1 release contents

The release should describe the mature library-management functionality already present, including catalog and physical-copy management, members and account management, circulation and fines, reservations/waitlists, branch-aware inventory, reporting and audit capabilities, CSV import/export, notification adapters, secure sessions and role authorization, responsive role-aware web UI, PostgreSQL/Flyway persistence, backup/restore tooling, Recovery Drill automation, CI/security automation, and engineering documentation.

## Release publication status

**`v0.1.1` is not yet published.**

The release notes can be prepared now, but the final notes must distinguish implemented functionality from checks actually observed passing. Publication should occur only after the tag-triggered release workflow succeeds.

## Continuation rule

The next work session should continue from this file rather than reopening the old `2.0.12` release plan. Prioritize generated-lockfile closure, current CI/recovery evidence, release-source consistency, and final release publication over unrelated feature expansion.
