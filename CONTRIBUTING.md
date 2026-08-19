# Contributing to LibraCore

Thanks for helping improve LibraCore.

## Before changing code

1. Read `README.md`, `docs/architecture.md`, and relevant ADRs.
2. Search existing issues and pull requests.
3. Keep a change focused. Large unrelated refactors should be separate work.
4. Never commit real member data, passwords, tokens, private endpoints, or generated secrets.

## Local setup

See [`docs/setup.md`](docs/setup.md). The normal verification commands are:

```bash
cd backend
mvn clean verify
```

and, until a synchronized `frontend/package-lock.json` is committed:

```bash
cd frontend
npm install
npm run check
```

Once the lockfile is present on the branch, use `npm ci` for clean/reproducible dependency installation.

## Engineering expectations

- Preserve server-side authorization; hiding UI is not an access-control mechanism.
- Add or update tests for behavior changes and every regression fix.
- Add a Flyway migration for schema changes; do not rewrite released migration history.
- Keep untrusted inputs bounded and validated.
- Use parameterized database access.
- Keep user-facing strings and errors understandable and safe.
- Preserve keyboard access, focus visibility, semantic labels, and responsive behavior.
- Update documentation when behavior/configuration changes.

## Commit and PR style

Conventional Commits are preferred, for example `feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `perf:`, `build:`, `ci:`, and `chore:`. Favor small, reviewable commits that each leave the repository coherent.

A pull request should explain what changed, why, verification performed, migration/security/accessibility implications, and screenshots for meaningful UI changes.

## Security reports

Do not disclose exploitable vulnerabilities in a public issue. Follow [`SECURITY.md`](SECURITY.md).

## Conduct

Participation is governed by [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).

**Made by the Sanskar**
