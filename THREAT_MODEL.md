# LibraCore Threat Model

## Scope

This model covers the React web client, Spring Boot API, PostgreSQL database, authentication/session subsystem, CSV exchange, notification adapter, backup/restore scripts, and normal operator deployment boundary.

## Assets

- member identity/contact information;
- account credentials and active sessions;
- catalog and copy inventory integrity;
- circulation, reservation, fine, and audit history;
- database/SMTP/deployment secrets;
- backups and exported CSV data;
- service availability and trustworthy audit evidence.

## Trust boundaries

1. Browser ↔ HTTP API.
2. API ↔ PostgreSQL.
3. API ↔ SMTP provider when enabled.
4. Operator workstation ↔ import/export files.
5. Backup/restore tooling ↔ database/storage.
6. CI/repository ↔ deployment environment.

## Primary threats and mitigations

| Threat | Example | Main mitigations |
|---|---|---|
| Credential theft | password/session token disclosure | password hashing, opaque sessions, no secret logging, TLS required in production |
| Broken access control | member invoking staff endpoint | Spring Security role checks and ownership checks in service/controller boundaries |
| Injection | malicious query/import values | parameterized Spring JDBC, validation, bounded inputs, CSV parser limits |
| Session abuse | stolen/replayed bearer token | server-side expiry/revocation, logout, short configured TTL, protected transport |
| Data exfiltration | broad export/backup access | staff authorization, least privilege, protected files/storage, audit trails |
| Malicious/accidental import | malformed or duplicate CSV | size/header validation, deterministic parsing, transactional changes, explicit results |
| Business-rule races | double issue or conflicting reservation | database transactions, locking/constraints, state validation |
| Audit tampering | hiding privileged operations | append-oriented audit records and restricted DB access; external log retention recommended |
| Notification leakage | sensitive content sent to wrong recipient | minimal message content, verified member data, controlled SMTP configuration |
| Dependency compromise | vulnerable Maven/npm package | lock/update policy, CI checks, CodeQL/dependency review where available |
| Backup compromise | copied database dump | restricted storage, encryption/retention policies, tested restore procedures |

## Abuse cases

- Repeated login guessing.
- Staff using search/export beyond legitimate duties.
- Member attempting to modify another member's reservation or loan.
- Crafted CSV designed to exhaust memory or confuse spreadsheet consumers.
- Accession/barcode collisions intended to issue the wrong copy.
- Operator accidentally enabling bootstrap credentials or permissive CORS in production.

Rate limiting should be enforced at the reverse proxy/API gateway or added at the application edge before internet-facing deployment. Exported CSV should be treated as untrusted when opened in spreadsheet software; avoid introducing formula interpretation from untrusted fields.

## Residual risks

A self-hosted application cannot protect data from a fully compromised database administrator, operating system, browser, reverse proxy, or SMTP account. LibraCore also cannot define an institution's lawful retention/consent policy. Those risks require deployment, governance, and operational controls outside the application.

## Review triggers

Revisit this model when adding external identity providers, object/file uploads, public registration, payments, cloud storage, mobile clients, multi-tenant hosting, third-party analytics, or new network integrations.
