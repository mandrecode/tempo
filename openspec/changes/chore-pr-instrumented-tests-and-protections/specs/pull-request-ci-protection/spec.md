# pull-request-ci-protection Specification

## Purpose
Ensure public pull requests receive emulator-backed Android validation before merge while repository and deployment authority remain limited to the owner.

## ADDED Requirements
### Requirement: Pull requests run instrumented tests
Ready pull requests targeting `main` SHALL run the Android instrumented test job used by CI.

#### Scenario: Ready PR targets main
- **WHEN** a non-draft pull request targeting `main` is opened, reopened, or marked ready for review
- **THEN** CI runs `:app:connectedDebugAndroidTest` through the instrumented test job

#### Scenario: Draft PR remains skipped
- **WHEN** a pull request targeting `main` is still a draft
- **THEN** CI does not require the instrumented test job until the PR is ready for review

### Requirement: Instrumented tests gate aggregate CI
The aggregate CI status SHALL fail when the instrumented test job runs and fails.

#### Scenario: Instrumented tests fail on a ready PR
- **WHEN** the instrumented test job completes with a failure
- **THEN** the aggregate CI status job fails and blocks merge through required checks

#### Scenario: Instrumented tests are intentionally skipped
- **WHEN** CI intentionally skips checks for an allowed automation path
- **THEN** the aggregate CI status job treats the skipped instrumented test as non-blocking

### Requirement: Protected operations remain owner-gated
Repository merge and deployment controls SHALL keep privileged operations limited to `sherrerapiqueras`.

#### Scenario: Google Play deployment approval is required
- **WHEN** the `google-play` environment is used
- **THEN** `sherrerapiqueras` is the required deployment reviewer

#### Scenario: Main branch remains protected
- **WHEN** a change is merged into `main`
- **THEN** the protected branch ruleset requires the configured CI status checks before the merge can complete
