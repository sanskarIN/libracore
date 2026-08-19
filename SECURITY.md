# Security Policy

LibraCore treats authentication, authorization, member data, circulation history, backups, and operational audit records as security-sensitive.

## Supported versions

The active `main` development line and the latest tagged release receive security fixes. Older pre-release snapshots are not supported once superseded.

## Reporting a vulnerability

Do **not** open a public issue for a vulnerability that could expose data, credentials, authentication bypasses, injection, privilege escalation, or destructive actions.

Report privately to **sanskarin@outlook.in** or **supportramsandesh@gmail.com** with:

- affected version/commit;
- reproduction conditions and impact;
- minimal proof-of-concept details needed to verify safely;
- suggested mitigation, if known.

Please avoid accessing data that is not yours, persistence, denial of service, social engineering, or destructive testing. Test only systems you own or are explicitly authorized to assess.

## Security expectations

- Passwords are hashed with maintained framework primitives; plaintext passwords are never stored.
- Protected API operations enforce authorization server-side.
- Session tokens are bearer secrets and must be handled like passwords.
- CORS, bootstrap credentials, SMTP credentials, and database access are environment-configured.
- Real secrets and personal production data must never be committed.
- Database migrations, backups, and restores must be tested in controlled environments before production use.
- Logs and audit events must not contain passwords, authorization headers, session tokens, or SMTP/database secrets.

## Deployment responsibility

Operators are responsible for TLS termination, database/network isolation, secret storage, patching, backup protection, retention policy, monitoring, and least-privilege deployment configuration. Development defaults are not a production security boundary.

See also [`THREAT_MODEL.md`](THREAT_MODEL.md), [`PRIVACY.md`](PRIVACY.md), and [`docs/deployment.md`](docs/deployment.md).
