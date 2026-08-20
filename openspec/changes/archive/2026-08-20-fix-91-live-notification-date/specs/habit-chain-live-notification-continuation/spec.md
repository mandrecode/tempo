## ADDED Requirements

### Requirement: Live notification tap restores scheduled date
The system SHALL open a habit-chain live activity notification in the routines context for the scheduled date represented by that notification.

#### Scenario: Opening overnight live notification after midnight
- **GIVEN** a habit-chain live activity notification is active for a scheduled date before today
- **WHEN** the user taps the notification after midnight
- **THEN** the routines screen selected date is set to the notification scheduled date
- **AND** the matching habit-chain sheet is opened for that date

#### Scenario: Opening live notification without scheduled date metadata
- **GIVEN** a habit-chain notification tap does not include scheduled date metadata
- **WHEN** the app handles the notification action
- **THEN** the matching habit-chain sheet is opened using the current routines selected date
