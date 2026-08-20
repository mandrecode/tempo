## Context

The app already centralizes build metadata in `AppVersionInfo`, populated from `BuildConfig` by `AppVersionProviderImpl`. Settings uses the version name for its About section and feedback form, while release builds are assembled from a checked-out Git tag in CI. The issue’s example establishes a short SHA suffix in the user-facing build string.

## Goals / Non-Goals

**Goals:**

- Make the source revision available to application code without adding a runtime Git dependency.
- Keep the existing `AppVersionProvider` abstraction as the boundary for consumers.
- Format the user-facing build identifier as `<version name> (<short SHA>)`.
- Ensure local, CI, and source-archive builds remain buildable.

**Non-Goals:**

- Changing semantic versioning or Play version codes.
- Showing a full 40-character SHA in the UI.
- Adding a repository/network call or persisting build metadata.

## Decisions

- Generate a `COMMIT_SHA` `BuildConfig` string during Gradle configuration from the CI-provided commit SHA. Validate it as a hexadecimal SHA and truncate it to seven characters; use an empty string when it is missing or malformed. This avoids launching an external process during configuration and keeps source-archive builds successful without exposing placeholder text.
- Normalize the value to a short, stable identifier in the build configuration (seven characters when a full SHA is available). This keeps Settings readable and matches the issue’s abbreviated example. A fallback remains visible rather than silently omitting provenance.
- Add `commitSha` to `AppVersionInfo` and let consumers derive the display string. The provider remains the single source of truth and existing consumers can continue using `versionName` independently.
- Pass the combined version/build string to the feedback URL so submitted reports retain the same provenance shown in Settings.

## Risks / Trade-offs

- [Missing or malformed CI SHA] → Emit an empty identifier and keep the build successful; the UI omits the optional suffix.
- [Non-standard CI SHA] → Accept only a safe non-blank value and shorten it consistently; tests cover the formatting helper/provider contract.
- [UI string changes] → Update English and Spanish resources together and adjust relevant UI assertions.
