## Why

The repository is now public and stable enough for pull requests to exercise the same emulator-backed confidence checks used after merge. Merge and deployment authority should also stay limited to the repository owner so public contributions cannot bypass review, release, or deployment safeguards.

## What Changes

- Run instrumented Android tests for pull requests targeting `main`, not only for pushes to `main`.
- Keep skipped release-please checks and draft PR behavior unchanged.
- Confirm repository protection and deployment approval settings only allow `sherrerapiqueras` to administer protected merges and approve Google Play deployments.

Non-goals:

- Do not change the instrumented test command, emulator API level, or test implementation.
- Do not add new Android dependencies.
- Do not change release publishing behavior beyond verifying deployment approval ownership.

## Capabilities

### New Capabilities

- `pull-request-ci-protection`: PR checks include instrumented tests and protected repository/deployment controls remain owner-gated.

## Impact

- `.github/workflows/ci.yml` PR check behavior.
- GitHub repository ruleset and environment protection settings.
