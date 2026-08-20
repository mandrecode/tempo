## ADDED Requirements

### Requirement: User-controlled automatic removal
The system SHALL present completed-task retention as an opt-in preference that is disabled by default.

#### Scenario: Existing or new installation uses the default
- **WHEN** the user has not configured completed-task retention
- **THEN** automatic removal is disabled and completed tasks remain until manually deleted

#### Scenario: User disables automatic removal
- **WHEN** the user disables automatic removal
- **THEN** the system persists the disabled state and cancels scheduled cleanup work

### Requirement: Configurable retention period
The system SHALL let the user step through retention periods of 1, 3, 5, 7, 14, 21, 30, 45, 90, 180, or 365 days, SHALL retain 30 days as the stored default interval, and SHALL persist the selected value across process restarts.

#### Scenario: User changes the retention period
- **WHEN** the user presses plus or minus in the retention control
- **THEN** the system persists that value and displays it after the settings screen is recreated

#### Scenario: User reaches a retention boundary
- **WHEN** the selected retention period is the first or last preset
- **THEN** the control disables the action that would move beyond the supported range

### Requirement: Prompt and periodic cleanup
The system SHALL request cleanup promptly after an enabled retention policy is saved and SHALL continue requesting cleanup approximately once per day while the policy remains enabled.

#### Scenario: User enables automatic removal
- **WHEN** the user saves an enabled retention policy
- **THEN** the system enqueues unique immediate cleanup and unique daily periodic cleanup

#### Scenario: User changes an enabled retention period
- **WHEN** the user changes the number of days while automatic removal is enabled
- **THEN** the system replaces pending immediate cleanup and updates the periodic cleanup schedule

#### Scenario: Scheduled worker runs after feature is disabled
- **WHEN** a previously queued cleanup worker starts after automatic removal has been disabled
- **THEN** it completes without deleting tasks

### Requirement: Cutoff-based task-tree removal
The system SHALL atomically remove each completed top-level task whose non-null completion timestamp is at or before the current local date-time minus the configured retention period, together with that task's subtasks.

#### Scenario: Completed task is older than the cutoff
- **WHEN** cleanup runs for a top-level task completed at or before the cutoff
- **THEN** the task and all of its subtasks are deleted

#### Scenario: Completed task is newer than the cutoff
- **WHEN** cleanup runs for a top-level task completed after the cutoff
- **THEN** the task and its subtasks are retained

#### Scenario: Task is incomplete or has no completion timestamp
- **WHEN** cleanup examines an incomplete task or a completed task without `completedAt`
- **THEN** the task is retained

#### Scenario: Archived periodic predecessor has a next occurrence
- **WHEN** an eligible completed periodic predecessor references a spawned next occurrence
- **THEN** cleanup removes the predecessor tree and preserves the next occurrence

### Requirement: Retry-safe cleanup
The system SHALL make completed-task cleanup idempotent so retries cannot delete additional ineligible task data.

#### Scenario: Cleanup repeats with unchanged cutoff
- **WHEN** cleanup successfully runs more than once with the same task data and cutoff
- **THEN** subsequent runs delete no additional tasks beyond those eligible in the first run
