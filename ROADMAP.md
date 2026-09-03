# LibraCore Roadmap

The roadmap describes direction, not a promise that an unchecked item already exists. `what_changed.md` is the implementation handoff and `CHANGELOG.md` records delivered behavior.

## 1.1.3 — Maintenance release closure

- [x] Preserve the published `v1.0.0`, `v1.1.0`, `v1.1.1`, and `v1.1.2` history.
- [x] Advance the backend executable manifest to `1.1.3`.
- [x] Advance the frontend executable manifest to `1.1.3`.
- [x] Add repository-managed `v1.1.3` release notes.
- [ ] Synchronize and commit `frontend/package-lock.json` using the supported npm toolchain.
- [ ] Observe successful Backend CI, Frontend CI, Version Sync, CodeQL, Dependency Review, and Recovery Drill on the exact final source; fix every failure.
- [ ] Complete role-based browser smoke testing and accessibility evidence.
- [ ] Review repository links, configuration, tracked secrets, and release artifacts.
- [ ] Run `node scripts/check-version.mjs 1.1.3` against the exact final checkout.
- [ ] Tag and publish `v1.1.3` only after every release gate above is closed.

## Published 1.1.2 release line

The repository's `v1.1.2` release is published and remains immutable. The next release target is `v1.1.3`; do not rewrite or force-move any existing stable tag.

## Historical 1.1.1 release line

The repository's `v1.1.1` release is published and remains immutable. Its source and verification history are retained for auditability.

## Historical 1.1.0 release line

The repository's `v1.1.0` release preparation and publication history are retained for auditability. Its tag is historical and must not be rewritten.

## Historical 2.0.12 engineering line

The repository previously used `2.0.12` as a release-candidate engineering line. Its implementation work is retained in Git history and changelog documentation, but it is no longer an active release target.

## Historical 0.1.1 rebaseline attempt

A temporary `0.1.1` release-rebaseline pass was superseded after confirming the intended post-`v1.0.0` release sequence. Those preparation commits remain in Git history as auditable work and are not a published `v0.1.1` release.

## 2.1.x — Operational hardening

- Expand observability and operational diagnostics.
- Strengthen performance measurement and representative load testing.
- Extend branch-level workflows and reporting.
- Improve deployment automation and environment-specific configuration validation.

## Future

- Additional library integrations and interoperability.
- Expanded analytics and reporting.
- Further accessibility and internationalization improvements.
- Optional service decomposition only when operational boundaries justify it.
