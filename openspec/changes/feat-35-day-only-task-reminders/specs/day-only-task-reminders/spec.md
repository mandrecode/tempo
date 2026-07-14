## ADDED Requirements

### Requirement: Date-only task reminder selection
The system SHALL let a user save a task reminder after selecting a date without requiring manual clock-time selection, and SHALL resolve that reminder to the currently configured default task reminder time.

#### Scenario: User accepts the default time
- **WHEN** the user selects a future date and chooses the date-only reminder action
- **THEN** the task reminder is saved for that date at the currently configured default task reminder time

#### Scenario: Today's default time has passed
- **WHEN** the user selects the current date after the configured default task reminder time has passed
- **THEN** the date-only reminder action is unavailable and exact-time selection remains available

### Requirement: Exact-time task reminder selection
The system SHALL continue to let a user choose a specific clock time for a task reminder.

#### Scenario: User chooses an exact time
- **WHEN** the user selects a date, continues to exact-time selection, and confirms a time
- **THEN** the task reminder is saved for the selected date and exact confirmed time

### Requirement: Configurable default task reminder time
The system SHALL provide a Settings control for the default task reminder time, SHALL initialize it to 09:00 when no value has been saved, and SHALL persist a valid user-selected time across app restarts.

#### Scenario: No preference exists
- **WHEN** the app reads the default task reminder time before the user has configured it
- **THEN** the system exposes 09:00 as the default task reminder time

#### Scenario: User changes the default time
- **WHEN** the user confirms a valid time in the default task reminder time setting
- **THEN** the system persists and displays that time and uses it for subsequent date-only task reminder selections

#### Scenario: Stored preference is invalid
- **WHEN** a stored default reminder hour or minute falls outside its valid range
- **THEN** the system normalizes the value before exposing it as a task reminder time

### Requirement: Existing reminder stability
The system SHALL NOT change existing task reminder timestamps or scheduled alarms when the default task reminder time preference changes.

#### Scenario: Default time changes after reminders exist
- **WHEN** the user changes the default task reminder time while tasks already have reminders
- **THEN** those tasks retain their existing reminder date-times and scheduled alarm times
