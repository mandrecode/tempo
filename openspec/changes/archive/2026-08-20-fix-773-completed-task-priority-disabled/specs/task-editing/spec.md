# task-editing Specification

## ADDED Requirements

### Requirement: Completed task priority is read-only

When a completed task or subtask is edited, Tempo SHALL prevent priority changes while still allowing users to see its current priority.

#### Scenario: Completed task has priority

- **WHEN** the user edits a completed task with a priority
- **THEN** the priority is shown as read-only
- **AND** the user cannot change or clear the priority

#### Scenario: Completed subtask has priority

- **WHEN** the user edits a completed subtask with a priority
- **THEN** the priority is shown as read-only
- **AND** the user cannot change or clear the priority

#### Scenario: Completed subtask has no priority

- **WHEN** the user edits a completed subtask without a priority
- **THEN** priority editing controls are unavailable
