## MODIFIED Requirements

### Requirement: Show the latest feature once per version
The system SHALL present the single most recent registered feature entry to the user, at most once per registered entry, after first-run onboarding has completed.

#### Scenario: Newer feature entry not yet seen
- **WHEN** the app starts, onboarding is completed, and the registry's latest entry has an `id` different from the persisted `lastSeenEntryId`
- **THEN** the system shows the "what's new" bottom sheet for that entry

#### Scenario: Latest entry already seen
- **WHEN** the app starts and the registry's latest entry's `id` equals the persisted `lastSeenEntryId`
- **THEN** the system does not show the "what's new" bottom sheet

#### Scenario: Onboarding not yet completed
- **WHEN** the app starts and first-run onboarding has not been completed
- **THEN** the system does not show the "what's new" bottom sheet, regardless of registry state

#### Scenario: Onboarding replay in progress
- **WHEN** the user replays onboarding from Settings (`OnboardingRoute(isReplay = true)`)
- **THEN** the system does not show the "what's new" bottom sheet during the replay

### Requirement: Persist last-seen version after dismissal
The system SHALL update the persisted `lastSeenEntryId` to match the shown entry's `id` once the user dismisses the "what's new" bottom sheet, so the same entry is never shown again.

#### Scenario: User dismisses the sheet
- **WHEN** the user dismisses the "what's new" bottom sheet (tapping the primary action, dragging it away, or tapping outside)
- **THEN** the system persists `lastSeenEntryId` as the dismissed entry's `id`
- **AND** the sheet does not reappear on subsequent app launches for that same entry

### Requirement: Display format for the latest feature
The system SHALL display the latest feature using the legend format "New features in vX.Y.Z: <feature title>" followed by a short description, inside the app's standard modal bottom sheet component, where "X.Y.Z" is the app's actual running version rather than an author-supplied value.

#### Scenario: Sheet content rendering
- **WHEN** the "what's new" bottom sheet is shown for a registry entry with title "See what's new" while the app's real running version is `1.4.0`
- **THEN** the sheet displays the legend text "New features in v1.4.0: See what's new"
- **AND** displays the entry's description text below the legend
- **AND** is rendered via the shared `TempoModalBottomSheet` component

### Requirement: Feature registry holds only the current feature
The system SHALL maintain a single, in-code "latest feature" entry (a stable `id`, title, description) that developers replace when shipping a new feature; no historical entries are retained in code, since only the current entry is ever shown. The entry SHALL NOT carry an author-supplied release version — the version is always sourced from the app's actual build info at display time.

#### Scenario: Shipping a new feature replaces the registry entry
- **WHEN** a new feature ships and its entry (with a new `id`) replaces the previous `WhatsNewRegistry.latest` value
- **THEN** only the new entry is eligible to be shown to the user
- **AND** the previous entry's code and now-unused strings are removed rather than retained

#### Scenario: Author does not predict a release version
- **WHEN** an author adds or replaces the registry's `latest` entry
- **THEN** they assign a stable, human-chosen `id` describing the feature
- **AND** they are not required to guess, compute, or recheck any version number against `version.txt` or the release cadence
