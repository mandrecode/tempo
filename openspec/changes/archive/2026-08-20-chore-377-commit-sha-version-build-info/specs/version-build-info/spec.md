## ADDED Requirements

### Requirement: Build metadata includes source revision

The Android build SHALL expose the source commit identifier through the existing application version provider, alongside the semantic version name and version code. The build SHALL remain successful when no Git revision is available by exposing an empty commit identifier.

#### Scenario: Build from a Git checkout

- **WHEN** the app is built with a valid CI-provided commit SHA
- **THEN** `AppVersionInfo` contains the semantic version, version code, and the abbreviated source commit identifier

#### Scenario: Build without Git metadata

- **WHEN** the app is built without a valid CI-provided commit SHA
- **THEN** the build succeeds and `AppVersionInfo` exposes an empty commit identifier

### Requirement: User-facing build information identifies the revision

Settings SHALL display the version name and abbreviated commit identifier in the format `Version <version> (<commit>)`, and feedback submissions SHALL include the same combined build string.

#### Scenario: Settings displays build provenance

- **WHEN** the Settings About section is rendered with version `1.2.3` and commit `au4b6i`
- **THEN** it displays `Version 1.2.3 (au4b6i)`

#### Scenario: Settings omits unavailable provenance

- **WHEN** the Settings About section is rendered with version `1.2.3` and no commit identifier
- **THEN** it displays `Version 1.2.3` without empty parentheses or placeholder text

#### Scenario: Feedback carries build provenance

- **WHEN** a user opens the feedback form
- **THEN** the version query parameter contains the combined version and abbreviated commit identifier
