# Performance fixtures

LibraCore includes a deterministic PostgreSQL dataset for repeatable catalog, member, circulation, reservation, fine, and export/load investigations. The fixture is intentionally **not** a production seeder and must only target a disposable benchmark database.

## Safety contract

`scripts/load-performance-fixture.sh` refuses to write unless both conditions are true:

1. `PERF_FIXTURE_ALLOW_WRITE=YES` is set exactly; and
2. the connected PostgreSQL database name ends in `_perf` or `_benchmark`.

The loader also requires a Flyway-managed LibraCore schema with no failed migration history. These controls are defense in depth, not permission to point benchmark tooling at valuable data. Create a dedicated database, run the current migrations, benchmark it, and drop that database when the measurement is finished.

Never copy production member records into this fixture. All generated members, email addresses, publishers, titles, and identities are fictional and deterministic.

## Default dataset

Without overrides the loader creates:

- 5,000 books;
- 2 copies per book (10,000 copies);
- 5,000 members;
- 4,000 loans, mixing open and returned state;
- 1,000 active reservations;
- deterministic overdue fines for a subset of overdue open loans;
- generated publishers, authors, categories, one benchmark branch/shelf, and one benchmark circulation policy;
- one disabled, intentionally non-login-capable staff identity used only to satisfy historical loan foreign keys.

The fixture uses reserved deterministic UUID prefixes so rerunning the loader replaces only its prior fixture rows and recreates the same cardinality/relationships.

## Prerequisites

- PostgreSQL compatible with the current LibraCore release line;
- `psql` available on the workstation;
- a disposable database whose name ends in `_perf` or `_benchmark`;
- all current Flyway migrations already applied to that database.

Example database name: `libracore_perf`.

## Load the default fixture

Use a benchmark-only database credential and avoid exposing its URL in shared shell transcripts or logs.

```bash
export PERF_DATABASE_URL='postgresql://libracore:benchmark-password@localhost:5432/libracore_perf'
export PERF_FIXTURE_ALLOW_WRITE=YES
bash scripts/load-performance-fixture.sh
bash scripts/check-performance-fixture.sh
```

The checker verifies exact core counts plus invariants such as open-loan copy state, dedicated policy linkage, active-reservation uniqueness, fictional email domains, and the disabled fixture user.

## Change the scale

The loader accepts bounded integer environment variables:

| Variable | Default | Allowed range |
|---|---:|---:|
| `PERF_BOOKS` | 5,000 | 1–100,000 |
| `PERF_MEMBERS` | 5,000 | 1–100,000 |
| `PERF_COPIES_PER_BOOK` | 2 | 1–10 |
| `PERF_LOANS` | 4,000 | 0–200,000 |
| `PERF_RESERVATIONS` | 1,000 | 0–100,000 |

Additional relational guards apply: total copies cannot exceed 500,000; loans cannot exceed generated copies or members; reservations cannot exceed books or members.

Example larger profile:

```bash
export PERF_BOOKS=25000
export PERF_MEMBERS=20000
export PERF_COPIES_PER_BOOK=3
export PERF_LOANS=15000
export PERF_RESERVATIONS=5000
bash scripts/load-performance-fixture.sh
bash scripts/check-performance-fixture.sh
```

Do not increase limits merely to produce an impressive number. Choose a profile that represents the deployment being measured and record it with the result.

## Repeatability

Running the loader again with the same variables deletes the previous fixture UUID namespaces and recreates them inside a transaction. This makes query-plan and before/after comparisons much less sensitive to random fixture shape. PostgreSQL `ANALYZE` runs after each load so the planner sees current fixture statistics.

The dedicated CI workflow loads a small fixture twice on PostgreSQL 18 and checks the resulting shape after each load. CI also proves that the explicit-write and database-name safety gates reject unsafe invocations.

## Suggested measurements

Use these fixtures to investigate concrete operations rather than generating unsupported global throughput claims. Useful targets include:

- catalog title/author/category search and bounded pagination;
- member name/card/email search;
- branch/copy availability lookups;
- open/overdue loan queries;
- reservation queue reads;
- dashboard/reporting queries;
- bounded CSV export behavior;
- connection-pool behavior under representative concurrent API traffic.

For database work, capture `EXPLAIN (ANALYZE, BUFFERS)` only in the disposable benchmark database. A plan from a tiny or differently distributed dataset is not evidence for production.

## Measurement record

For every result, record at least:

- Git commit/version;
- PostgreSQL, Java, Node/browser versions when relevant;
- machine CPU/memory/storage and whether virtualization/container limits apply;
- all `PERF_*` cardinality variables;
- cold/warm state and cache-reset method, if any;
- exact operation/query and parameters;
- concurrency and sample count;
- median/tail metrics or query-plan evidence appropriate to the operation;
- before/after commits for optimization claims.

Do not compare results collected with materially different fixture profiles as though they were equivalent.

## Cleanup

The preferred cleanup is to drop the entire disposable benchmark database using normal PostgreSQL administrative tooling after the benchmark record is saved. This avoids creating a false impression that a row-level cleanup script can make a mixed database safe again.

If you need another benchmark run, recreate/migrate the dedicated `_perf`/`_benchmark` database and reload the fixture. Never use fixture UUID-prefix deletion as a production cleanup mechanism.
