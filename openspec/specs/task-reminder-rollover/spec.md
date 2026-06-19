# task-reminder-rollover Specification

## Purpose
Define how overdue, incomplete periodic tasks are preserved during reminder delivery and resync, so a missed occurrence keeps its recurrence metadata and is neither dropped nor duplicated.
## Requirements
### Requirement: Preserve overdue periodic task occurrence
The system SHALL preserve an incomplete periodic task whose reminder is overdue when reminder delivery or reminder resync processes it.

#### Scenario: Overdue periodic task is processed
- **WHEN** an incomplete periodic task has a reminder date earlier than the current time
- **THEN** the original task remains incomplete with its existing reminder date and recurrence metadata

#### Scenario: Overdue periodic task spawns next occurrence
- **WHEN** an incomplete periodic task with no existing next occurrence is processed
- **THEN** the system creates a separate incomplete task for the next occurrence using the same recurrence rules

### Requirement: Rollover is idempotent
The system SHALL create at most one next occurrence for each overdue periodic task occurrence.

#### Scenario: Same overdue task is processed repeatedly
- **WHEN** an overdue periodic task already links to an existing next occurrence
- **THEN** the system does not create another task and schedules the existing next occurrence if needed

#### Scenario: Linked next occurrence is missing
- **WHEN** an overdue periodic task links to a next occurrence that no longer exists
- **THEN** the system clears the stale link and creates one replacement next occurrence

### Requirement: Only latest occurrence owns future reminder
The system SHALL schedule future reminders from the spawned next occurrence rather than the overdue original occurrence.

#### Scenario: Next occurrence is created
- **WHEN** an overdue periodic task spawns a next occurrence
- **THEN** the system does not re-schedule the overdue task and schedules the spawned task's future reminder

#### Scenario: Existing next occurrence is reused
- **WHEN** an overdue periodic task already has a valid next occurrence
- **THEN** the system schedules the valid next occurrence and does not schedule the overdue task

### Requirement: Completion flow remains distinct
The system SHALL keep overdue rollover behavior separate from periodic completion behavior.

#### Scenario: Overdue rollover occurs
- **WHEN** an overdue periodic task is processed without the user marking it completed
- **THEN** the system does not mark the task completed, does not complete subtasks, and does not strip recurrence metadata

#### Scenario: User completes a periodic task
- **WHEN** the user marks a periodic task completed
- **THEN** the existing periodic completion flow archives the completed occurrence and manages the next occurrence without creating duplicates

