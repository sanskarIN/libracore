# LibraCore — 1.1.0 Engineering Handoff

**Audit/update date:** 2026-09-03  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Active release line:** `1.1.0`  
**Commit identity used for project commits:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. The intended next release follows the existing `v1.0.0` tag and is **`v1.1.0`**. Release work remains evidence-driven: source changes, CI results, recovery evidence, and release publication status are separate facts.

## Current release state

The active release target is **`v1.1.0`**.

Completed in this correction/rebaseline pass:

- backend Maven version is `1.1.0`;
- frontend npm version is `1.1.0`;
- README release references are `1.1.0`;
- roadmap release references are `1.1.0`;
- release procedure targets `v1.1.0`;
- changelog now has a dedicated `1.1.0` release-candidate section;
- the previous temporary `0.1.1` preparation is explicitly recorded as superseded historical work;
- `what_changed.md` is now the canonical `1.1.0` handoff.

The existing `v1.0.0` tag is intentionally not being rewritten. The release sequence is now treated as `v1.0.0` followed by `v1.1.0`.

## Release commits from the correction pass

The version/documentation correction was intentionally split into reviewable commits:

- `41bec20c` — `docs(release): rebaseline README for 1.1.0`
- `fb7731ef` — `docs(release): rebaseline roadmap for 1.1.0`
- `0f15c290` — `docs(release): target 1.1.0 release process`
- `207ec72c` — `docs(changelog): rebaseline release notes for 1.1.0`
- this handoff update records the final release-line decision and remaining gates.

All project commits use the requested commit identity `Sanskar <sanskarin@outlook.in>`.

## Manifest state

The executable manifests are aligned to `1.1.0`:

- `backend/pom.xml` → `1.1.0`;
- `frontend/package.json` → `1.1.0`.

`scripts/check-version.mjs` remains the executable guard for frontend/backend/tag consistency.

## Frontend lockfile

`frontend/package-lock.json` is committed and its root metadata currently follows the frontend package version. It must be verified with the supported Node/npm toolchain before the final release tag. Do not introduce dependency changes merely to perform this release rebaseline, and do not synthesize a lockfile by hand.

The release source must pass a clean `npm ci` installation and the frontend quality gate.

## Release workflow evidence

The release workflow is fail-closed on manifest/tag mismatch. The earlier `v1.0.0` attempt demonstrated this by stopping at version validation when source manifests still declared `2.0.12`.

That historical failure proves the guard works; it is not evidence for `v1.1.0` readiness.

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

An automated disposable PostgreSQL Recovery Drill exists at `.github/workflows/recovery-drill.yml`. It exercises the repository's actual backup and restore scripts, verifies migration history and marker data, then starts the packaged backend against the restored database.

A later CI hardening change added stronger PostgreSQL readiness and backend startup diagnostics after an earlier drill stopped during source-database migration. That hardening must be included in the exact release source.

Do not claim Recovery Drill success until a current run on the exact intended `v1.1.0` source has a successful conclusion.

## Frontend verification history

The hosted frontend lockfile/bootstrap work previously demonstrated real lockfile generation, reproducible `npm ci`, linting, strict TypeScript verification, deterministic Vitest execution, production Vite build, artifact preservation, and lockfile commit.

The earlier `exactOptionalPropertyTypes` issue in `frontend/src/api.ts` was fixed without weakening compiler strictness, with regression coverage for optional API correlation identifiers.

Those historical results remain useful engineering evidence but must not be represented as a fresh `v1.1.0` release-gate pass unless the corresponding current-source workflows succeed.

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

Before tagging `v1.1.0`, complete and directly verify:

1. verify/regenerate the committed frontend lockfile with the supported Node/npm toolchain;
2. run `node scripts/check-version.mjs 1.1.0`;
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
14. create `v1.1.0` only after the above evidence is green.

## Tagging rule

The intended release tag is:

```text
v1.1.0
```

Create it from the exact commit containing the final verified release source:

```bash
git tag -a v1.1.0 -m "LibraCore v1.1.0"
git push origin v1.1.0
```

Do not create or publish the tag early. Do not force-move a published release tag. If a pre-publication error is found, fix the source and create the release tag from the corrected commit.

## Planned v1.1.0 release contents

The release should describe the mature library-management functionality already present, including catalog and physical-copy management, members and account management, circulation and fines, reservations/waitlists, branch-aware inventory, reporting and audit capabilities, CSV import/export, notification adapters, secure sessions and role authorization, responsive role-aware web UI, PostgreSQL/Flyway persistence, backup/restore tooling, Recovery Drill automation, CI/security automation, and engineering documentation.

## Release publication status

**`v1.1.0` is not yet published.**

Release notes can be prepared now, but publication should occur only after the final release-source verification and tag-triggered release workflow succeed.

## Continuation rule

The next work session should continue from this file using `v1.1.0` as the sole active release target. Do not reopen the superseded `0.1.1` plan or the old `2.0.12` release plan. Prioritize lockfile verification, current CI/recovery evidence, release-source consistency, and final release publication over unrelated feature expansion.
