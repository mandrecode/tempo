## ADDED Requirements

### Requirement: Future habit reminders remain scheduled after completion
The system SHALL schedule a habit alarm when a habit has a future reminder date, even if the legacy completion flag is true.

#### Scenario: Completed habit has future reminder
- **WHEN** a completed habit is advanced to a future reminder occurrence
- **THEN** the system schedules the future habit reminder alarm

### Requirement: Habit notifications respect occurrence completion
The system SHALL decide whether to display a habit reminder notification using the completion history for the reminder occurrence date.

#### Scenario: Reminder occurrence is already completed
- **WHEN** a habit reminder alarm fires for a date already present in the habit completion history
- **THEN** the system does not show the habit reminder notification

#### Scenario: Legacy completion flag is stale for a future occurrence
- **WHEN** a habit reminder alarm fires for a date that is not present in the habit completion history
- **THEN** the system may show the habit reminder notification regardless of the legacy completion flag
