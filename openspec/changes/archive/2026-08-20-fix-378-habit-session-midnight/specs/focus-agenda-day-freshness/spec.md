## ADDED Requirements

### Requirement: Habit completion uses the current Focus day
When the Focus screen remains open across a local calendar-day boundary, the system SHALL refresh the agenda observation to the current local day before processing a habit or habit-chain completion event.

#### Scenario: Completing a habit after midnight
- **WHEN** a user checks or unchecks a habit in an open Focus screen after the local date has advanced
- **THEN** the system records the completion for the new date and renders the habit's updated completion state from the new day's agenda

#### Scenario: Completing a chain after midnight
- **WHEN** a user toggles a habit chain in an open Focus screen after the local date has advanced
- **THEN** the system refreshes the Focus day once and records each member habit's completion for that new date

### Requirement: Single active Focus day observation
The system SHALL keep only one active Focus day observation after refreshing an open screen for a new local date.

#### Scenario: Repeated toggles on the new day
- **WHEN** a user toggles more habits after the Focus screen has refreshed for the new date
- **THEN** the system continues to render updates from the current day's agenda without retaining updates from the previous day's observer
