# Setup

## Prerequisites

- Git
- Java 25 JDK
- Maven 3.6.3 or newer
- Node.js matching `frontend/package.json` engines (Node 24 is a recommended current choice)
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

## Frontend

In another terminal:

```bash
cd frontend
npm ci
npm run dev
```

Open `http://localhost:5173`.

The default frontend API base is `http://localhost:8080/api`; override with `VITE_API_BASE_URL` when needed.

## Full verification

```bash
cd backend
mvn clean verify
```

```bash
cd frontend
npm ci
npm run lint
npm run typecheck
npm run test:run
npm run build
```

## Reset disposable development data

**This destroys the Compose PostgreSQL volume. Do not use it for valuable data.**

```bash
docker compose -f compose.yml down -v
docker compose -f compose.yml up -d postgres
```

## Platform notes

Commands are shell-neutral where practical. On Windows PowerShell, environment-variable assignment syntax differs from POSIX shells. Docker Desktop can provide Compose/PostgreSQL, while Java/Maven/Node/npm can be installed with standard vendor/package-manager methods.

For common failures see [`troubleshooting.md`](troubleshooting.md).
