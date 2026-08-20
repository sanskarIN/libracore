# Main Branch Protection Guidance

The repository can contain CI workflows without enforcing them. Production/release discipline requires repository settings that prevent accidental bypass.

## Recommended rules for `main`

Configure a GitHub branch ruleset or branch-protection rule with:

- Require a pull request before merging for normal collaborative changes.
- Require at least one approving review when the project has multiple active maintainers.
- Require code-owner review for paths covered by `.github/CODEOWNERS` when the repository plan/settings support that control.
- Dismiss stale approvals when review-relevant code changes.
- Require conversation resolution before merge.
- Require stable successful checks after their exact names have been observed. For the 2.0.12 line, evaluate at minimum Backend CI, Frontend CI, Version Sync, CodeQL/security analysis, Dependency Review where applicable, and Recovery Drill for migration/recovery-sensitive changes.
- Require branches to be up to date when appropriate for the chosen merge strategy.
- Block force pushes and branch deletion.
- Restrict bypass permissions to the smallest maintainer group that actually needs emergency access.
- Require signed commits/tags if the maintainer workflow can support them consistently.

Do not enable a required check before confirming its exact GitHub check name and that it can run for pull requests; an incorrect required-check configuration can deadlock merges.

## Code ownership

`.github/CODEOWNERS` establishes `@sanskarIN` as the default owner and calls out repository automation, security-sensitive backend/configuration paths, Flyway migrations, recovery scripts, and release/recovery documentation explicitly.

CODEOWNERS by itself does not enforce review. Enforcement comes from the branch ruleset/protection setting that requires code-owner approval.

## Frontend lockfile state

The npm-generated `frontend/package-lock.json` is committed for the 2.0.12 release candidate and clean frontend verification uses `npm ci`. Frontend CI can therefore be made a mandatory merge check once its stable successful check name has been confirmed through the release-verification PR.

The maintainer-only **Frontend Lockfile Bootstrap** workflow remains available for intentional dependency-lock regeneration after future dependency declaration changes; it is not a substitute for ordinary read-only Frontend CI.

## Recovery-sensitive changes

Migration and backup/restore changes should receive code-owner review and successful Recovery Drill evidence. The automated drill uses disposable PostgreSQL databases only and does not remove the need for environment-specific operational recovery testing.

## Release tags

Protect release tag patterns such as `v*` against deletion/rewriting. Published version tags should be immutable; corrections receive a new version. For the current prepared source line, do not create `v2.0.12` until the release gates in `what_changed.md` are complete.

## Why this is not stored entirely in Git

Branch rules are repository-host settings, not ordinary tracked files. This document records the expected policy so settings can be audited/recreated, but maintainers must enable the actual GitHub ruleset separately.

## Current checkpoint rule

`what_changed.md` must record whether `main` protection was observed as enabled during the latest audit. Do not claim branch protection is active merely because this guidance or `.github/CODEOWNERS` exists.
