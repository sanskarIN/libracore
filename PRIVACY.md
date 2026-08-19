# Privacy

LibraCore is self-hosted open-source library-management software. The project itself does not operate a hosted LibraCore service and does not receive an operator's member database by default.

## Data stored by a deployment

Depending on enabled features, a deployment can store:

- member identity and contact information;
- library-card and membership status data;
- catalog, copy, branch, and shelf information;
- loans, reservations, fines, and settlement history;
- application accounts, roles, and server-side session records;
- operational audit events;
- notification delivery metadata.

Passwords are represented by password hashes, not plaintext. Bearer session tokens are security-sensitive even when server storage uses derived representations.

## Purpose and minimization

Operators should collect only data required for library operations, define a lawful purpose and retention period, restrict staff access by role, and avoid placing sensitive free-form information in notes or logs.

## Local/browser storage

The web client stores the active access session in browser `sessionStorage` and appearance preference in `localStorage`. Signing out clears the stored application session. Shared/public workstations should use separate browser profiles or clear browser data after use.

## Import, export, backup, and deletion

CSV exports and database backups may contain personal data. Treat them with the same or stronger controls as the live database: encryption where appropriate, access restrictions, retention limits, verified deletion, and protected transfer.

Application-level deletion/retention requirements depend on an operator's legal and institutional obligations. Database administrators must ensure backup retention does not silently defeat deletion policy.

## Notifications

Mock notification mode is intended for development. SMTP mode sends configured messages through the operator-selected mail infrastructure; its provider policies also apply.

## Logs and telemetry

LibraCore should not log passwords, bearer tokens, authentication headers, or secret configuration. Operators should review infrastructure logs and retention because reverse proxies, databases, mail systems, and hosting platforms may record additional metadata.

## Contact

Privacy and support questions: **supportramsandesh@gmail.com**. Project/business contact: **sanskarin@outlook.in**.

See [`SECURITY.md`](SECURITY.md) and [`THREAT_MODEL.md`](THREAT_MODEL.md).
