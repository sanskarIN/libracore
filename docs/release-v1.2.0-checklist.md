# v1.2.0 Release Contract Checklist

This checklist is the release contract for LibraCore v1.2.0 and can be reused for later 1.2.x releases.

## Source integrity

- [ ] `backend/pom.xml` version is `1.2.0`.
- [ ] `frontend/package.json` version is `1.2.0`.
- [ ] `frontend/package-lock.json` root version is `1.2.0`.
- [ ] `frontend/package-lock.json` workspace package version is `1.2.0`.
- [ ] No stale `1.1.10` release-line metadata remains in active release automation.
- [ ] No credentials, tokens, private keys, or real member records are committed.

## Automated verification

- [ ] Repository version guard passes.
- [ ] Backend compile and tests pass.
- [ ] Frontend lint passes.
- [ ] Frontend strict type checking passes.
- [ ] Frontend deterministic tests pass.
- [ ] Frontend production build passes.
- [ ] Version Sync passes.
- [ ] API contract policy validation passes.
- [ ] CodeQL passes.
- [ ] Dependency review/security checks pass.
- [ ] Recovery Drill passes.
- [ ] Performance Fixture CI passes.
- [ ] Performance Thresholds CI passes.

## Runtime verification

- [ ] Packaged backend starts against a disposable PostgreSQL instance.
- [ ] `/actuator/health` returns the expected restricted health contract.
- [ ] Liveness reports application liveness.
- [ ] Readiness includes database health.
- [ ] `/actuator/info` exposes only reviewed non-sensitive build metadata.
- [ ] Authentication smoke journey succeeds.
- [ ] Catalog smoke journey succeeds.
- [ ] Member smoke journey succeeds.
- [ ] Circulation smoke journey succeeds.
- [ ] Reservation smoke journey succeeds.
- [ ] Reporting smoke journey succeeds.
- [ ] Administrator authorization boundaries are verified.
- [ ] Member authorization boundaries are verified.

## Accessibility and UX

- [ ] Keyboard navigation works across primary journeys.
- [ ] Focus remains visible.
- [ ] Interactive controls have accessible names.
- [ ] Status is not conveyed by color alone.
- [ ] Loading, empty, success, and error states are understandable.
- [ ] Reduced-motion behavior remains respected.
- [ ] Responsive layout is usable at common viewport sizes.

## Release integrity

- [ ] Final verified commit SHA is recorded in the handoff.
- [ ] Artifact filenames are derived from the current version.
- [ ] SHA-256 checksums are reviewed.
- [ ] Release notes describe actual delivered behavior.
- [ ] Changelog entry is present.
- [ ] Upgrade and rollback guidance is present.
- [ ] Tag is created only from the exact verified commit.
- [ ] Tag-scoped validation passes.
- [ ] Stable release publication is completed only after all gates pass.

## Rollback readiness

- [ ] Previous validated artifact is identified.
- [ ] Database backup/restore procedure is available.
- [ ] Operators understand that released Flyway migrations must not be edited.
- [ ] Published tags will not be force-moved.
