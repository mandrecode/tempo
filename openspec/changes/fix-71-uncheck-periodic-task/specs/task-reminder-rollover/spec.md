## MODIFIED Requirements

### Requirement: Completion flow remains distinct
The system SHALL keep overdue rollover behavior separate from periodic completion and uncheck behavior.

#### Scenario: Overdue rollover occurs
- **WHEN** an overdue periodic task is processed without the user marking it completed
- **THEN** the system does not mark the task completed, does not complete subtasks, and does not strip recurrence metadata

#### Scenario: User completes a periodic task
- **WHEN** the user marks a periodic task completed
- **THEN** the periodic completion flow archives the completed occurrence and manages the next occurrence without creating duplicates

#### Scenario: User unchecks a completed periodic occurrence with an open next occurrence
- **WHEN** the user unchecks an archived completed occurrence that links to an existing open next occurrence
- **THEN** the archived occurrence becomes incomplete with its original reminder date, no recurrence metadata, no completion timestamp, and no next-occurrence link
- **AND** the existing next occurrence and its future reminder remain unchanged
- **AND** the system does not report that the next occurrence was cancelled
