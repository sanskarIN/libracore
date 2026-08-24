# Performance

LibraCore optimizes measured bottlenecks while keeping correctness and maintainability first.

## Budgets

Initial release targets for a normal staff workstation and representative local/regional deployment:

- Keep API list endpoints bounded to documented server page limits.
- Avoid unbounded catalog/member/report queries in UI workflows.
- Keep initial production frontend assets small enough for interactive use on ordinary broadband/mobile networks; review Vite build output for unexpected bundle growth.
- Keep common database queries supported by indexes tied to actual filters/joins.
- Avoid request-time N+1 query patterns and repeated refetch loops.
- Keep CSV import parsing bounded to 2,000,000 decoded characters, 10,000 rows, 64 columns per row, and 20,000 characters per cell.
- Keep book/member CSV exports bounded to 10,000 records and stream database rows with a 250-row JDBC fetch window instead of materializing the entire query result.

These are engineering guardrails, not fabricated latency guarantees. Concrete millisecond/throughput SLOs require measurements on declared hardware/data/network profiles.

## CSV exchange memory safety

CSV exchange is intentionally bounded in both directions:

- Import request bodies are decoded as strict UTF-8 and passed to the CSV parser as a `Reader`; the controller does not first create a complete request `String`.
- The parser emits one completed row at a time while enforcing total-character, row, column, and cell limits before data can grow without bound.
- Book/member import services consume those emitted rows transactionally instead of retaining the complete parsed document.
- Export services reject datasets above 10,000 records before writing a response.
- Allowed exports use forward-only, read-only JDBC statements with a fetch size of 250 and stream rows to the HTTP response rather than building a complete result list and response string.
- Exported user-controlled cells are spreadsheet-safe encoded: values that could be interpreted as formulas or commands when opened in common spreadsheet applications are prefixed with an apostrophe. Import values are not rewritten by this safety rule.

The compatibility tests cover quoted multiline cells, CRLF row boundaries, malformed input, all configured parser limits, Reader failures, formula-prefix neutralization, Reader-based imports, export row bounds, and controller streaming behavior.

## Measurement method

For a performance claim, record:

1. commit/version;
2. machine/runtime/database versions;
3. dataset shape and size;
4. warm/cold state;
5. exact operation/query;
6. sample count and percentile/summary metrics;
7. before/after result for optimizations.

## Database

Use `EXPLAIN (ANALYZE, BUFFERS)` in a safe non-production environment when diagnosing slow SQL. Add indexes because a real query needs them, not speculatively. Re-test write cost and migration impact after adding indexes.

For PostgreSQL cursor-style fetch behavior, keep streamed export execution inside its read-only transaction. If datasource/driver settings change, re-run export regression and representative large-dataset checks rather than assuming fetch-size behavior remains identical.

## Frontend

Use production builds for bundle analysis. Prefer pagination, incremental rendering, and explicit refresh over loading entire operational datasets. Avoid fake delays and expensive computation in render paths.

## Load and scale

Before internet/public or high-volume deployment, benchmark concurrent authentication, catalog search, issue/return transactions, reservation promotion, reporting, and CSV exchange with representative data. Confirm database connection-pool and reverse-proxy limits under load.

The CSV safeguards above bound application memory growth; they are not a throughput benchmark. The roadmap separately tracks repeatable large catalog/member/circulation performance fixtures.

## Regression policy

A performance optimization should include evidence and must not weaken authorization, transaction correctness, accessibility, or data integrity. If a hot path becomes regression-prone, add a stable benchmark or query-plan check where practical.
