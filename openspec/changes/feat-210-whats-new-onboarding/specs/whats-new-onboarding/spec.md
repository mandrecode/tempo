## ADDED Requirements

### Requirement: Show the latest feature once per version
The system SHALL present the single most recent registered feature entry to the user, at most once per app version, after first-run onboarding has completed.

#### Scenario: Newer feature entry not yet seen
- **WHEN** the app starts, onboarding is completed, and the registry's latest entry has a `versionCode` greater than the persisted `lastSeenVersionCode`
- **THEN** the system shows the "what's new" bottom sheet for that entry

#### Scenario: Latest entry already seen
- **WHEN** the app starts and the registry's latest entry's `versionCode` is less than or equal to the persisted `lastSeenVersionCode`
- **THEN** the system does not show the "what's new" bottom sheet

#### Scenario: Onboarding not yet completed
- **WHEN** the app starts and first-run onboarding has not been completed
- **THEN** the system does not show the "what's new" bottom sheet, regardless of registry state

#### Scenario: Onboarding replay in progress
- **WHEN** the user replays onboarding from Settings (`OnboardingRoute(isReplay = true)`)
- **THEN** the system does not show the "what's new" bottom sheet during the replay

### Requirement: Persist last-seen version after dismissal
The system SHALL update the persisted `lastSeenVersionCode` to match the shown entry's `versionCode` once the user dismisses the "what's new" bottom sheet, so the same entry is never shown again.

#### Scenario: User dismisses the sheet
- **WHEN** the user dismisses the "what's new" bottom sheet (tapping the primary action, dragging it away, or tapping outside)
- **THEN** the system persists `lastSeenVersionCode` as the dismissed entry's `versionCode`
- **AND** the sheet does not reappear on subsequent app launches for that same entry

### Requirement: Display format for the latest feature
The system SHALL display the latest feature using the legend format "New features in vX.Y.Z: <feature title>" followed by a short description, inside the app's standard modal bottom sheet component.

#### Scenario: Sheet content rendering
- **WHEN** the "what's new" bottom sheet is shown for a registry entry with `versionName = "1.2.0"` and title "See what's new"
- **THEN** the sheet displays the legend text "New features in v1.2.0: See what's new"
- **AND** displays the entry's description text below the legend
- **AND** is rendered via the shared `TempoModalBottomSheet` component

### Requirement: Feature registry holds only the current feature
The system SHALL maintain a single, in-code "latest feature" entry (version code, version name, title, description) that developers replace when shipping a new feature; no historical entries are retained in code, since only the current entry is ever shown.

#### Scenario: Shipping a new feature replaces the registry entry
- **WHEN** a new feature ships and its entry replaces the previous `WhatsNewRegistry.latest` value
- **THEN** only the new entry is eligible to be shown to the user
- **AND** the previous entry's code and now-unused strings are removed rather than retained
