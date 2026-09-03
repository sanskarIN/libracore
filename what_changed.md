# LibraCore — 1.1.4 Engineering Handoff

**Audit/update date:** 2026-09-03  
**Repository:** `sanskarIN/libracore`  
**Branch:** `main`  
**Published stable release:** `v1.1.2`  
**Active release target:** `v1.1.4`  
**Commit identity:** `Sanskar <sanskarin@outlook.in>`

This is the canonical continuation record for LibraCore. Published `v1.0.0`, `v1.1.0`, `v1.1.1`, and `v1.1.2` history must not be rewritten or force-moved. The attempted `v1.1.3` tag and its failed workflow are retained as audit history.

## v1.1.4 work completed in this pass

Focused, reviewable commits have opened the next maintenance line:

- `ci(release): harden packaged health gate and release notes path`
- `chore(release): advance backend to 1.1.4`
- `chore(release): advance frontend to 1.1.4`
- `docs(release): add v1.1.4 release notes`
- `docs(package): add frontend repository metadata`
- `ci(lockfile): automate frontend lockfile synchronization`
- `docs(changelog): open v1.1.4 maintenance line`
- `docs(roadmap): move maintenance closure to 1.1.4`
- `docs(release): target v1.1.4 publication procedure`
- `docs(release): record v1.1.4 engineering handoff`
- `docs(release): correct v1.1.4 handoff record`
- `docs(release): remove unverified commit identifiers`

All commits use `Sanskar <sanskarin@outlook.in>`.

## v1.1.3 diagnostic record

The existing `v1.1.3` tag triggered release workflow run `33748921280` from source commit `275782d1d735aa8fedd8bb98c06dc019fbb95e4d`. The job successfully checked out the tag, validated the release version, packaged the backend, and started the packaged service, but failed at `Verify packaged backend health`; later release steps were skipped. The failure is retained as diagnostic evidence rather than rewritten.

## Manifest and lockfile state

The executable manifests are aligned to `1.1.4`:

- `backend/pom.xml` → `1.1.4`;
- `frontend/package.json` → `1.1.4`;
- `frontend/package-lock.json` → must be regenerated/synchronized to `1.1.4` by the supported npm workflow before tagging.

The lockfile must not be hand-synthesized. The updated lockfile bootstrap workflow now runs on relevant `main` pushes and generates the lockfile with Node.js 24/npm, verifies the locked installation and frontend checks, then commits the generated lockfile using the configured maintainer identity when it changes.

`scripts/check-version.mjs` remains the executable guard for all three version-bearing manifests.

## Current release sequence

```text
v1.0.0 → v1.1.0 → v1.1.1 → v1.1.2 → v1.1.3 → v1.1.4 → ...
```

The old `2.0.12` and temporary `0.1.1` preparation lines remain historical engineering work and are not active release targets.

## Release workflow changes

`.github/workflows/release.yml` remains tag-scoped and fail-closed. For v1.1.4 it now:

- keeps the committed lockfile requirement;
- validates the tag against backend/frontend/lockfile versions;
- verifies the backend with PostgreSQL;
- gives the packaged backend a bounded extended health-startup retry window;
- installs frontend dependencies with `npm ci`;
- runs frontend quality checks;
- packages release artifacts;
- creates SHA-256 checksums;
- selects `docs/release-notes/${GITHUB_REF_NAME}.md`, preventing stale hard-coded release-note paths;
- stops the temporary backend and prints its log when the job fails.

## Current verification status

The v1.1.4 source line is **not release-ready yet**. The lockfile synchronization workflow must first produce and commit the `1.1.4` lockfile, then all release-blocking CI and manual evidence must settle on the exact final source.

At this handoff:

- `v1.1.2` is the latest published stable release.
- `v1.1.3` has an existing failed release workflow and is retained as history.
- backend and frontend manifests are updated to `1.1.4`.
- the frontend lockfile is awaiting supported-toolchain synchronization to `1.1.4`.
- no `v1.1.4` tag should be created until the exact final source has successful release-blocking evidence.

## Remaining v1.1.4 release gates

1. confirm the `1.1.4` frontend lockfile synchronization commit is present;
2. run `node scripts/check-version.mjs 1.1.4` against the final checkout;
3. pass current Backend CI;
4. pass current Frontend CI;
5. pass Version Sync;
6. pass CodeQL;
7. pass Dependency Review/security checks;
8. pass the current Recovery Drill;
9. complete role-based browser smoke journeys;
10. complete the documented manual accessibility review;
11. review repository links/configuration and tracked secrets;
12. confirm branch-protection/rules status and document any host-level limitation;
13. identify the exact final verified commit;
14. create `v1.1.4` only from that exact commit;
15. confirm the tag-scoped release workflow succeeds;
16. review generated release artifacts and SHA-256 checksums;
17. confirm the GitHub release is published as stable/latest without rewriting previous releases.

## Local verification commands

From the repository root:

```bash
node scripts/check-version.mjs 1.1.4
```

Backend:

```bash
cd backend
mvn clean verify
```

Frontend:

```bash
cd frontend
npm ci --ignore-scripts --no-audit --no-fund
npm run check
```

## Tagging rule

Do not create `v1.1.4` until the exact final source has passed all release-blocking gates.

```bash
git checkout main
git pull --ff-only
node scripts/check-version.mjs 1.1.4
git tag -a v1.1.4 -m "LibraCore v1.1.4"
git push origin v1.1.4
```

Never force-move `v1.0.0`, `v1.1.0`, `v1.1.1`, or `v1.1.2`.

## Next engineering priorities after v1.1.4

After release closure, continue with meaningful product/operational work rather than version-only commits:

- operational observability and diagnostics;
- representative performance/load measurement;
- deeper branch-level workflows and reporting;
- deployment/environment validation;
- accessibility and internationalization improvements;
- further interoperability and library integrations.

## Project links

- GitHub: https://github.com/sanskarIN/libracore
- Maintainer: https://github.com/sanskarIN
- BuyMeACoffee: https://buymeacoffee.com/sanskarIN
