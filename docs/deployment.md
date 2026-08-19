# Deployment

This guide describes production boundaries, not a one-click hosting guarantee.

## Recommended topology

```text
Internet / internal clients
        |
   TLS reverse proxy
        |
  React static assets + /api proxy
        |
   Spring Boot service
        |
    PostgreSQL
```

Keep PostgreSQL off the public internet. Restrict database access to the application/administrative paths that require it.

## Required production decisions

- TLS certificate/termination and HTTP security headers.
- Strong database credentials and least-privilege database role.
- Secret manager/environment injection for DB/SMTP/bootstrap values.
- Explicit `APP_CORS_ALLOWED_ORIGINS` matching trusted frontend origins.
- Whether SMTP notifications are enabled and which provider/account is authorized.
- Session TTL appropriate to shared library workstations and risk.
- Reverse-proxy rate limits, request/body limits, timeouts, and trusted forwarded-header policy.
- Central logs/metrics with secret/PII redaction and retention.
- Backup location, encryption, retention, RPO/RTO, and restore drills.

## Bootstrap administrator

Bootstrap credentials are for controlled first-run provisioning. Do not keep a bootstrap password permanently configured after a production administrator account is established. Never place the password in Compose files, images, source control, shell history shared with others, or CI logs.

## Database migrations

Flyway runs schema migrations during application startup. For production, review each new migration before deployment, back up according to policy, and understand locks/runtime for production data size. In stricter environments, run migrations as a separately authorized deployment step.

## Frontend configuration

Build the frontend with the intended `VITE_API_BASE_URL`. Because Vite variables are compiled into the client bundle, they are public configuration—not secret storage.

## Security headers

At the reverse proxy/static host, configure a deployment-appropriate Content Security Policy, HSTS after HTTPS is proven, `X-Content-Type-Options: nosniff`, framing policy, referrer policy, and secure caching rules. Test the actual application before enforcing a restrictive CSP.

## Health and readiness

The backend exposes actuator health for deployment checks. Do not expose unnecessary actuator endpoints publicly. A healthy process does not prove all user journeys work; perform release smoke tests after deployment.

## Scaling

Start with one modular application and PostgreSQL. Before adding instances, verify session storage semantics, scheduled notification behavior, locking, and database connection limits. Introduce distributed infrastructure only with a measured reason and updated architecture/threat model.
