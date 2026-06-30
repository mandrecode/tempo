## ADDED Requirements

### Requirement: Completed overdue periodic task does not roll over from delayed alarm
The system SHALL NOT roll over an overdue periodic task when the task is already completed by the time a delayed reminder alarm is processed.

#### Scenario: Completed periodic task is processed by delayed reminder delivery
- **WHEN** a delayed reminder delivery processes an overdue periodic task that is currently completed
- **THEN** the system does not create a next occurrence and does not schedule a future reminder from that delivery
