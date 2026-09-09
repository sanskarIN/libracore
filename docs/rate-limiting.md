# Rate limiting and edge request controls

LibraCore expects internet-facing deployments to put explicit abuse controls in front of the Spring Boot service. Rate limiting is a deployment boundary, not a substitute for authentication, authorization, validation, session expiry, or database constraints.

The repository includes [`deploy/nginx/libracore.conf`](../deploy/nginx/libracore.conf) as a reference Nginx configuration. It is deliberately conservative and uses example hostnames/certificate paths. Operators must tune it from measured traffic and their own network topology before production use.

## What the reference protects

The example establishes two per-client-IP request zones:

- authentication login: `5` requests per minute with a small burst;
- normal `/api/` traffic: `20` requests per second with a bounded burst.

Rejected requests receive HTTP `429 Too Many Requests`. The example also caps request bodies at `2m`, matching LibraCore's backend multipart/request boundary, applies finite upstream timeouts, and restricts `/actuator/health` to same-host probes.

These values are safe starting points for testing, not universal production SLOs. Shared school/library NAT gateways, load tests, batch jobs, accessibility tools, integrations, and legitimate high-volume circulation desks can change the correct policy substantially.

## Trusted client identity

Rate limits are only meaningful if the edge uses a trustworthy client identity.

The reference assumes Nginx is directly internet-facing and keys limits from `$binary_remote_addr`. If another CDN, WAF, load balancer, or reverse proxy sits in front of Nginx, do not blindly rate-limit the untrusted `X-Forwarded-For` header. Configure the proxy's real-IP/trusted-proxy feature for the exact upstream address ranges first, then verify that clients cannot spoof the address used for limiting.

Likewise, the Spring application must not treat arbitrary forwarded headers as an authorization signal. Authentication continues to use LibraCore's bearer-session model.

## Login protection

Credential endpoints deserve a tighter budget than ordinary authenticated traffic. The reference gives `/api/auth/login` its own request zone so repeated credential guessing cannot consume the normal API allowance.

For a public deployment, also monitor failed-login volume and consider provider/WAF controls that can combine source reputation, account-level signals, or challenge mechanisms. Avoid revealing whether an account exists through different edge responses.

## API and import/export traffic

The generic API budget absorbs short user-interface bursts while bounding sustained request floods. CSV import/export endpoints retain their application-level row, cell, column, character, and export-record bounds; reverse-proxy limits are an additional layer rather than a replacement.

If a deployment introduces trusted machine integrations, give those integrations an explicit authenticated path and separately measured policy rather than globally increasing anonymous/public budgets.

## Request-size alignment

Keep edge body limits aligned with the application's supported maximums. An edge that accepts much larger requests still spends bandwidth and buffering resources before Spring rejects the content; an edge set too low can reject legitimate LibraCore operations before application validation runs.

Whenever backend upload/import limits change, review the reverse-proxy configuration in the same release.

## Timeouts

The reference uses finite connect/read/send timeouts. Production values should be long enough for supported database operations but short enough that dead upstreams do not hold connections indefinitely.

Do not hide slow database queries by continually increasing proxy timeouts. Measure the operation, inspect query plans and connection-pool pressure, and fix the bottleneck where practical.

## Health endpoint exposure

The reference only permits local access to `/actuator/health`. A managed platform may instead require a private load-balancer or orchestrator network. Permit only the actual trusted probe source and keep unrelated actuator endpoints unavailable externally unless there is a documented operational need and an appropriate security boundary.

## Validation checklist

Before enabling an internet-facing policy:

1. replace `library.example.org` and certificate/static-file paths;
2. confirm which component is the first trusted reverse proxy;
3. validate real client-IP handling with spoofed-forwarded-header tests;
4. run representative login, normal API, circulation, reporting, and CSV workloads;
5. verify legitimate bursts pass and abusive sustained traffic receives `429`;
6. verify request bodies larger than the supported boundary are rejected at the edge;
7. verify `/actuator/health` is reachable only from the intended probe network;
8. observe rate-limit counters/logs and tune from evidence;
9. document any deployment-specific exceptions and their owner/review date.

## Monitoring and incident use

Capture enough edge telemetry to identify rate-limit saturation without logging bearer tokens, passwords, CSV contents, or unnecessary member data. Useful fields normally include timestamp, route class, response status, request duration, upstream status, and privacy-reviewed source-network information.

During an incident, temporary stricter limits can reduce load, but emergency controls should have an explicit rollback condition. Permanent tuning belongs in reviewed deployment configuration so operators can reproduce it.

## Application-level limits later

A reverse proxy is the recommended first production boundary for LibraCore 2.x. If future deployments require per-account quotas, distributed limits across multiple application instances, or differentiated API-client budgets, add those capabilities only with an updated architecture/threat-model review. Do not introduce in-memory per-instance counters that appear globally authoritative in a multi-instance deployment.
