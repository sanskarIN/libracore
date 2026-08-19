# Performance

LibraCore optimizes measured bottlenecks while keeping correctness and maintainability first.

## Budgets

Initial release targets for a normal staff workstation and representative local/regional deployment:

- Keep API list endpoints bounded to documented server page limits.
- Avoid unbounded catalog/member/report queries in UI workflows.
- Keep initial production frontend assets small enough for interactive use on ordinary broadband/mobile networks; review Vite build output for unexpected bundle growth.
- Keep common database queries supported by indexes tied to actual filters/joins.
- Avoid request-time N+1 query patterns and repeated refetch loops.

These are engineering guardrails, not fabricated latency guarantees. Concrete millisecond/throughput SLOs require measurements on declared hardware/data/network profiles.

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

## Frontend

Use production builds for bundle analysis. Prefer pagination, incremental rendering, and explicit refresh over loading entire operational datasets. Avoid fake delays and expensive computation in render paths.

## Load and scale

Before internet/public or high-volume deployment, benchmark concurrent authentication, catalog search, issue/return transactions, reservation promotion, reporting, and CSV exchange with representative data. Confirm database connection-pool and reverse-proxy limits under load.

## Regression policy

A performance optimization should include evidence and must not weaken authorization, transaction correctness, accessibility, or data integrity. If a hot path becomes regression-prone, add a stable benchmark or query-plan check where practical.
