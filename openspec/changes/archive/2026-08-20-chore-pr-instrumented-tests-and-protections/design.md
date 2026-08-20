## Overview

Enable the existing `android-test` CI job for non-draft pull requests by removing its push-only condition while preserving the shared `check-context` skip gate. The existing aggregate `ci-status` job already depends on `android-test`, so it should treat instrumented tests as required whenever the job runs and tolerate only intentional skips.

Repository ownership controls are handled through GitHub settings rather than app code. The PR documents and verifies those settings, while direct GitHub configuration keeps `main` protected and `google-play` deployments reviewer-gated to `sherrerapiqueras`.

## Decisions

- Keep `reactivecircus/android-emulator-runner@v2`, API 31, and the current Gradle command unchanged to avoid expanding test scope.
- Let draft pull requests continue skipping CI through the existing workflow-level conditions.
- Keep release-please skip behavior unchanged so automated release PRs do not pick up unexpected expensive checks.
- Use GitHub repository settings for merge/deployment authority because workflow YAML cannot grant or deny merge permissions.

## Risks

- PR CI will become slower and consume more GitHub Actions minutes because an emulator boots for each ready PR.
- Emulator startup can be flaky; failures should remain visible as PR blockers rather than being hidden behind advisory checks.

## Verification

- Validate the OpenSpec change.
- Run workflow syntax/static checks where available.
- Inspect repository ruleset, collaborator list, workflow permissions, and `google-play` environment reviewers with `gh api`.
