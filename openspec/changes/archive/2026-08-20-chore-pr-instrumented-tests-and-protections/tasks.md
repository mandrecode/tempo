## 1. CI update

- [x] 1.1 Enable the existing instrumented test job for ready pull requests targeting `main`.
- [x] 1.2 Keep aggregate CI status failing when instrumented tests fail and passing when checks are intentionally skipped.

## 2. Repository protection

- [x] 2.1 Verify `main` is protected by the repository ruleset and required CI checks.
- [x] 2.2 Verify `google-play` deployments require approval from `sherrerapiqueras`.
- [x] 2.3 Verify no other collaborators can merge protected PRs.

## 3. Verification

- [x] 3.1 Run `openspec validate chore-pr-instrumented-tests-and-protections --strict`.
- [x] 3.2 Validate the workflow YAML syntax or parseability locally.
