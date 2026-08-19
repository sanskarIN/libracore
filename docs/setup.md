# Setup

## Prerequisites

- Git
- Java 25 JDK
- Maven 3.6.3 or newer
- Node.js matching `frontend/package.json` engines (Node 24 is the CI/release choice)
- npm
- Docker with Compose, or PostgreSQL 18-compatible service credentials

Verify tools:

```bash
java -version
mvn -version
node --version
npm --version
docker --version
docker compose version
```

## Clean checkout

```bash
git clone https://github.com/sanskarIN/libracore.git
cd libracore
```

The current executable source version is **2.0.12**. Confirm backend/frontend manifest synchronization:

```bash
node scripts/check-version.mjs 2.0.12
```

Copy `.env.example` to `.env` for your local values. Never commit `.env`.

## Database

Start the development database:

```bash
docker compose -f compose.yml up -d postgres
docker compose -f compose.yml ps
```

The backend uses Flyway on startup. Against a new database it applies every migration in `backend/src/main/resources/db/migration/` in version order.

## Backend

```bash
cd backend
mvn clean verify
mvn spring-boot:run
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

If bootstrap administration is required for a disposable/local environment, configure `APP_BOOTSTRAP_ADMIN_EMAIL` and `APP_BOOTSTRAP_ADMIN_PASSWORD` explicitly. Do not use a source-controlled/default production password.

## Frontend development before the lockfile is committed

In another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

The default frontend API base is `http://localhost:8080/api`; override with `VITE_API_BASE_URL` when needed.

`frontend/package.json` pins direct dependency/tool versions exactly, but `frontend/package-lock.json` is still a 2.0.12 release gate. `npm install` is acceptable for local development on commits without the lockfile; it is not the final reproducible release path.

## Generate the release lockfile

Maintainers can run the **Frontend Lockfile Bootstrap** GitHub Actions workflow, or generate it locally with the supported Node/npm toolchain:

```bash
cd frontend
npm install --package-lock-only --ignore-scripts --no-audit --no-fund
npm ci --ignore-scripts --no-audit --no-fund
npm run check
```

Review `package-lock.json` before committing it. After it is committed, clean frontend setup and CI should use `npm ci` instead of regenerating dependency resolution.

## Full verification after lockfile closure

```bash
node scripts/check-version.mjs 2.0.12
```

```bash
cd backend
mvn clean verify
```

```bash
cd frontend
npm ci --ignore-scripts
npm run check
```

`npm run check` runs lint, strict type checking, deterministic tests, and the production build.

## Reset disposable development data

**This destroys the Compose PostgreSQL volume. Do not use it for valuable data.**

```bash
docker compose -f compose.yml down -v
docker compose -f compose.yml up -d postgres
```

## Platform notes

Commands are shell-neutral where practical. On Windows PowerShell, environment-variable assignment syntax differs from POSIX shells. Docker Desktop can provide Compose/PostgreSQL, while Java/Maven/Node/npm can be installed with standard vendor/package-manager methods.

For common failures see [`troubleshooting.md`](troubleshooting.md). For the exact 2.0.12 pre-tag gate see [`releases/2.0.12.md`](releases/2.0.12.md).
