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

## Reverse proxy and rate limits

Internet-facing deployments should enforce request-rate and request-size controls before traffic reaches Spring Boot. LibraCore includes a syntax-checked Nginx starting point at [`deploy/nginx/libracore.conf`](../deploy/nginx/libracore.conf) and an operator guide in [`docs/rate-limiting.md`](rate-limiting.md).

The reference separates login throttling from the normal API budget, returns HTTP 429 for exceeded edge limits, caps request bodies at 2 MiB, uses finite upstream timeouts, and restricts the health endpoint to local probes. Its example values are not production SLOs: tune them from representative traffic, especially where many library clients share one NAT address.

If a CDN, WAF, or load balancer sits in front of the reference proxy, configure and verify trusted real-client-IP handling before using source addresses for rate limiting. Never trust arbitrary public `X-Forwarded-For` values as client identity.

## Bootstrap administrator

Bootstrap credentials are for controlled first-run provisioning. Do not keep a bootstrap password permanently configured after a production administrator account is established. Never place the password in Compose files, images, source control, shell history shared with others, or CI logs.

## Database migrations

Flyway runs schema migrations during application startup. For production, review each new migration before deployment, back up according to policy, and understand locks/runtime for production data size. In stricter environments, run migrations as a separately authorized deployment step.

## Frontend configuration

Build the frontend with the intended `VITE_API_BASE_URL`. Because Vite variables are compiled into the client bundle, they are public configuration—not secret storage.

## Security headers

At the reverse proxy/static host, configure a deployment-appropriate Content Security Policy, HSTS after HTTPS is proven, `X-Content-Type-Options: nosniff`, framing policy, referrer policy, and secure caching rules. Test the actual application before enforcing a restrictive CSP.

The Nginx reference includes conservative `nosniff`, framing, and referrer-policy headers. It intentionally does not guess a final Content Security Policy or HSTS lifetime because those require validating the deployment's actual asset origins, TLS behavior, and rollback plan.

## Health and readiness

The backend exposes actuator health for deployment checks. Do not expose unnecessary actuator endpoints publicly. The Nginx reference permits `/actuator/health` only from the local host; adapt that allow-list to the exact private probe network used by the deployment platform. A healthy process does not prove all user journeys work; perform release smoke tests after deployment.

## Scaling

Start with one modular application and PostgreSQL. Before adding instances, verify session storage semantics, scheduled notification behavior, locking, and database connection limits. Introduce distributed infrastructure only with a measured reason and updated architecture/threat model.
