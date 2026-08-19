# API Reference

LibraCore exposes JSON REST endpoints under `/api`. Authenticated requests send `Authorization: Bearer <token>`. The server remains the authorization boundary.

## Authentication

- `POST /api/auth/login` — email/password login; returns access token, expiry, and user identity.
- `GET /api/auth/me` — current identity.
- `POST /api/auth/logout` — revoke current session.

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

The exchange module provides authorized CSV import/export operations. CSV is an interoperability feature, not a backup substitute. Imports are bounded and validated before durable writes.

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
