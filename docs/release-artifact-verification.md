# Release Artifact Verification

LibraCore release artifacts must be verified against the exact source commit used for publication.

## Required evidence

For every stable release, retain:

- release tag and exact commit SHA;
- backend artifact name and SHA-256 checksum;
- frontend build output identity where applicable;
- manifest versions for backend, frontend, and lockfile;
- successful release-blocking workflow results;
- tag-scoped validation result;
- migration verification result;
- packaged startup/readiness evidence;
- browser smoke and accessibility evidence.

## Verification order

1. Identify the final candidate commit.
2. Run all blocking CI checks.
3. Build the release artifacts from that verified commit.
4. Calculate SHA-256 checksums.
5. Confirm artifact names contain the intended release version.
6. Confirm manifests still resolve to the intended release version.
7. Create the release tag without rewriting history.
8. Run tag-scoped validation.
9. Publish only after tag-scoped validation succeeds.

## Incident handling

If an artifact fails verification, do not publish it. If a published release is discovered to be invalid, publish a corrected release according to the project's versioning policy rather than force-moving the existing tag.

## Security

Checksums provide integrity evidence but do not replace provenance, CI, code review, dependency analysis, or secure artifact storage. Never place secrets or credentials into release artifacts or verification logs.
