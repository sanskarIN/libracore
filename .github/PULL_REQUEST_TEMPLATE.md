## Summary

Describe what changed and why.

## Verification

- [ ] Backend `mvn clean verify` (when affected)
- [ ] Frontend lint/typecheck/tests/build (when affected)
- [ ] Database migration tested from the supported prior schema (when affected)
- [ ] Manual smoke test for changed user journey

## Review checklist

- [ ] Change is focused and does not include unrelated churn.
- [ ] Tests cover new behavior/regressions.
- [ ] Authorization/ownership rules remain enforced server-side.
- [ ] No credentials, bearer tokens, production endpoints, or real personal data are included.
- [ ] Accessibility impact was considered for UI changes.
- [ ] Privacy/security impact was considered.
- [ ] Performance impact was considered for data-heavy paths.
- [ ] Documentation/configuration/changelog were updated when behavior changed.
- [ ] Schema changes use a new Flyway migration rather than rewriting history.

## Screenshots / evidence

Add UI captures, logs, query plans, benchmark results, or migration evidence when useful. Sanitize sensitive information first.
