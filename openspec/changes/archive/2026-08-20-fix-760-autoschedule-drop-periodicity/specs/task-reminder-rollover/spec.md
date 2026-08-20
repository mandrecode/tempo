## MODIFIED Requirements

### Requirement: Preserve overdue periodic task occurrence
The system SHALL preserve an incomplete periodic task whose reminder is overdue when reminder delivery or reminder resync processes it.

#### Scenario: Overdue periodic task is processed
- **WHEN** an incomplete periodic task has a reminder date earlier than the current time
- **THEN** the original task remains incomplete with its existing reminder date
- **AND** its recurrence metadata is stripped after a next occurrence is linked

#### Scenario: Overdue periodic task spawns next occurrence
- **WHEN** an incomplete periodic task with no existing next occurrence is processed
- **THEN** the system creates a separate incomplete task for the next occurrence using the same recurrence rules

### Requirement: Completion flow remains distinct
The system SHALL keep overdue rollover behavior separate from periodic completion behavior.

#### Scenario: Overdue rollover occurs
- **WHEN** an overdue periodic task is processed without the user marking it completed
- **THEN** the system does not mark the task completed and does not complete subtasks
- **AND** the original task no longer participates in periodic completion because recurrence metadata was stripped during rollover

#### Scenario: User completes a periodic task
- **WHEN** the user marks a periodic task completed
- **THEN** the existing periodic completion flow archives the completed occurrence and manages the next occurrence without creating duplicates
