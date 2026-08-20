## ADDED Requirements

### Requirement: Compatible Android build toolchain
The Android application build SHALL use Kotlin, KSP, and Hilt versions that are mutually compatible, and SHALL include every direct compile dependency required by Hilt-generated Java sources.

#### Scenario: Debug Hilt compilation
- **WHEN** the debug application variant is compiled from a clean dependency resolution
- **THEN** Hilt-generated sources SHALL compile without metadata-version or missing-annotation errors

### Requirement: Combined dependency verification
The consolidated dependency update SHALL pass the repository's required build, lint, unit, screenshot, static-analysis, schema, and available automated instrumented-test verification.

#### Scenario: Pre-publication verification
- **WHEN** the dependency-update branch is prepared for review
- **THEN** its verification results SHALL be recorded and any unavailable device-dependent check SHALL be explicitly reported
