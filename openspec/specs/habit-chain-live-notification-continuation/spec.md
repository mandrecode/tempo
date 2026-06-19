# habit-chain-live-notification-continuation Specification

## Purpose
Define how an already-active habit-chain live activity notification stays synchronized when the user continues toggling habit completion from inside the app, including for scheduled dates other than today.
## Requirements
### Requirement: Active chain live activity continues syncing from in-app progress
The system SHALL keep an already-active habit-chain live activity synchronized when the user continues toggling completion from inside the app, including non-today dates.

#### Scenario: Continuing an overnight chain inside the app
- **GIVEN** a habit chain live activity is active for a scheduled date before today
- **WHEN** the user toggles one of that chain's habits from the routines UI
- **THEN** the chain completion aggregation is recomputed for that scheduled date
- **AND** the live activity notification is updated to reflect the new progress

### Requirement: Historical edits remain constrained when no live activity is active
The system SHALL preserve current behavior for historical in-app edits when there is no active live activity session.

#### Scenario: Past-date toggle without active session
- **GIVEN** the selected date is before today
- **AND** no live activity is active for the affected chain
- **WHEN** the user toggles habit completion from inside the app
- **THEN** the habit completion history is updated
- **AND** the chain live activity is not refreshed

