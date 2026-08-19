# Main Branch Protection Guidance

The repository can contain CI workflows without enforcing them. Production/release discipline requires repository settings that prevent accidental bypass.

## Recommended rules for `main`

Configure a GitHub branch ruleset or branch-protection rule with:

- Require a pull request before merging for normal collaborative changes.
- Require at least one approving review when the project has multiple active maintainers.
- Dismiss stale approvals when review-relevant code changes.
- Require conversation resolution before merge.
- Require stable successful checks after their exact names have been observed. For the 2.0.12 line, evaluate at minimum Backend CI, Frontend CI, Version Sync, CodeQL/security analysis, and applicable dependency review checks.
- Require branches to be up to date when appropriate for the chosen merge strategy.
- Block force pushes and branch deletion.
- Restrict bypass permissions to the smallest maintainer group that actually needs emergency access.
- Require signed commits/tags if the maintainer workflow can support them consistently.

Do not enable a required check before confirming its exact GitHub check name and that it can run for pull requests; an incorrect required-check configuration can deadlock merges.

## Frontend lockfile prerequisite

Frontend CI intentionally requires the committed `frontend/package-lock.json`. Generate/review/commit that file before making Frontend CI a mandatory merge check. The maintainer-only **Frontend Lockfile Bootstrap** workflow exists to create the lockfile with the supported Node/npm toolchain when needed.

## Release tags

Protect release tag patterns such as `v*` against deletion/rewriting. Published version tags should be immutable; corrections receive a new version. For the current prepared source line, do not create `v2.0.12` until the release gates in `what_changed.md` are complete.

## Why this is not stored entirely in Git

Branch rules are repository-host settings, not ordinary tracked files. This document records the expected policy so settings can be audited/recreated, but maintainers must enable the actual GitHub ruleset separately.

## Current checkpoint rule

`what_changed.md` must record whether `main` protection was observed as enabled during the latest audit. Do not claim branch protection is active merely because this guidance exists.
