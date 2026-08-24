# API Reference

LibraCore exposes JSON REST endpoints under `/api`. Authenticated requests send `Authorization: Bearer <token>`. The server remains the authorization boundary.

## Authentication

- `POST /api/auth/login` — email/password login; returns access token, expiry, and user identity.
- `GET /api/auth/me` — current identity.
- `POST /api/auth/logout` — revoke current session.

## Staff account administration

Administrator-only staff identity operations live under `/api/admin/users`:

- `GET /api/admin/users` — bounded staff-account list, optionally filtered by `ADMIN` or `LIBRARIAN` role.
- `POST /api/admin/users` — create an administrator or librarian account with a validated password.
- `PATCH /api/admin/users/{userId}/enabled` — enable or disable staff access subject to server safeguards.
- `POST /api/admin/users/{userId}/password` — reset a staff password and revoke that account's active sessions.

The web client exposes these operations only to administrators, but the backend role checks and account invariants are authoritative.

## Catalog

The catalog module exposes branch/shelf discovery, searchable/paginated books, book detail, physical-copy creation/update, and barcode/QR/accession lookup. Staff mutations require staff roles; authenticated members can use allowed catalog reads.

Typical search parameters include bounded `limit`/`offset`, free-text `q`, branch selection, and availability filtering.

## Members

- staff member search/list and detail;
- member creation/update/status/account-link workflows;
- `GET /api/members/me` for the member linked to the current account.

Member data access is role/ownership sensitive and must not be inferred from frontend navigation alone.

## Circulation

- `POST /api/circulation/loans` — staff issue using `copyId` and `memberId`.
- `POST /api/circulation/loans/{loanId}/return` — staff return; may assess a fine and promote a reservation.
- `POST /api/circulation/loans/{loanId}/renew` — renew subject to ownership/role and policy.
- `GET /api/circulation/loans/me` — member loan list.
- `GET /api/circulation/loans?memberId=...` — staff member loan list.
- `POST /api/circulation/reservations` — create reservation/waitlist entry.
- `POST /api/circulation/reservations/{reservationId}/cancel` — authorized cancellation.
- `GET /api/circulation/reservations/me` — member reservations.
- `GET /api/circulation/reservations?memberId=...` — staff view.
- `GET /api/circulation/policies` — staff policy view.
- `POST /api/circulation/policies` — administrator creates a policy.

## Fines

- `GET /api/fines/me` — member fine list.
- `GET /api/fines?memberId=...` — staff view.
- `GET /api/fines/{fineId}` — staff detail.
- `POST /api/fines/{fineId}/settle` — staff records paid/waived settlement.

## Reports and audit

- `GET /api/reports/dashboard` — staff metrics, optionally branch-scoped.
- `GET /api/reports/overdue` — bounded overdue page.
- `GET /api/reports/audit` — administrator audit search with bounded filters.

## Data exchange

The exchange module is restricted to `ADMIN` and `LIBRARIAN` roles and provides:

- `POST /api/exchange/books/export` — stream the authorized book export as UTF-8 CSV.
- `POST /api/exchange/members/export` — stream the authorized member export as UTF-8 CSV.
- `POST /api/exchange/books/import` — consume a UTF-8 `text/csv` book import.
- `POST /api/exchange/members/import` — consume a UTF-8 `text/csv` member import.

CSV is an interoperability feature, not a backup substitute. Import parsing is incremental and bounded to 2,000,000 decoded characters, 10,000 rows, 64 columns per row, and 20,000 characters per cell. Malformed UTF-8, NUL characters, invalid quoting, duplicate/missing required headers, and domain-validation failures are rejected with stable API errors.

Book and member exports are capped at 10,000 records. Accepted exports stream database rows directly to the HTTP response instead of retaining the whole dataset in application memory.

To reduce spreadsheet formula-injection risk, exported cell values whose first meaningful character is `=`, `+`, `-`, `@`, tab, carriage return, or line feed are prefixed with an apostrophe. This is an export-only safety representation; import values are not silently rewritten by the same rule.

## Errors

API failures use a stable user-safe error object containing fields such as:

```json
{
  "timestamp": "2026-08-19T12:00:00Z",
  "status": 400,
  "code": "validation_failed",
  "message": "The request contains invalid fields.",
  "path": "/api/example",
  "fieldErrors": {"field": "reason"},
  "correlationId": "..."
}
```

Clients must not depend on stack traces or database-specific text.

## Pagination

Potentially large collections use `limit` and `offset`, with a response containing `items`, `limit`, `offset`, and `hasMore`. Client code must respect server maximum limits.

For exact current request/response fields, treat Java controller/model records as the source of truth until generated OpenAPI documentation is introduced.
