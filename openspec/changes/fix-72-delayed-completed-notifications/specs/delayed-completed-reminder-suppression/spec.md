## ADDED Requirements

### Requirement: Delayed completed habit alarms are suppressed
The system SHALL suppress a delayed habit reminder notification when the fired occurrence date has already been completed.

#### Scenario: Completed habit occurrence fires after reminder advances
- **WHEN** a habit reminder alarm fires with a scheduled occurrence date that is present in the habit completion history
- **THEN** the system does not display a habit reminder notification for that occurrence

#### Scenario: Stale legacy completion flag does not suppress a different occurrence
- **WHEN** a habit reminder alarm fires for a scheduled occurrence date that is not present in habit completion history
- **THEN** the system does not suppress the notification solely because the legacy completion flag is true

### Requirement: Delayed completed task alarms are suppressed
The system SHALL suppress a delayed task reminder notification when the current task state is completed or missing at delivery time.

#### Scenario: Completed task alarm fires after in-app completion
- **WHEN** a task reminder alarm fires for a task that is currently marked completed
- **THEN** the system does not display a task reminder notification

#### Scenario: Deleted task alarm fires
- **WHEN** a task reminder alarm fires for a task id that no longer exists
- **THEN** the system does not display a task reminder notification

### Requirement: Stale task delivery does not trigger rollover side effects
The system SHALL avoid periodic rollover side effects when a delayed task alarm is no longer eligible to notify.

#### Scenario: Completed periodic task alarm fires after in-app completion
- **WHEN** a periodic task reminder alarm fires for a task that is currently marked completed
- **THEN** the system does not create or schedule a next occurrence from that delayed delivery
