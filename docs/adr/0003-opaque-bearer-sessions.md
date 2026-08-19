# ADR 0003: Opaque Bearer Sessions

- Status: Accepted
- Date: 2026-08-19

## Context

LibraCore needs revocable staff/member sessions with server-side role and account state. A self-contained token would make immediate revocation and state changes more complex.

## Decision

Use opaque bearer tokens returned after credential verification. Store session state server-side with expiry/revocation and construct the authenticated application principal from trusted server data. The browser stores the active session in `sessionStorage`.

## Consequences

- Logout and administrative revocation can invalidate active sessions server-side.
- Each authenticated request requires session validation/storage access.
- Bearer tokens remain high-value secrets and require TLS plus XSS-safe UI practices.
- Browser `sessionStorage` limits persistence but does not protect a token from malicious script running in the same origin.
- Role/ownership authorization remains server-side; client route visibility is only UX.
- Future external identity/SSO support should integrate at the identity/session boundary rather than weakening domain authorization.
