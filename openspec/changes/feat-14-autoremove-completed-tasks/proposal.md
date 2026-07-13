## Why

Completed tasks currently remain in Tempo until the user deletes them manually, so long-running task lists accumulate stale history. [GitHub issue #14](https://github.com/mandrecode/tempo/issues/14) requests an automatic, user-controlled retention policy that removes old completed tasks without affecting active work.

## What Changes

- Add an opt-in retention preference with a compact stepper for choosing a supported cleanup interval.
- Persist the enabled state and retention period across app restarts.
- Run background cleanup daily while the feature is enabled and reschedule cleanup when the setting changes.
- Delete only completed top-level tasks whose completion time is at or before the configured cutoff, including their subtasks; preserve incomplete tasks and recently completed tasks.
- Keep automatic removal disabled by default so existing installations retain their current behavior until the user enables it.

## Capabilities

### New Capabilities
- `completed-task-retention`: User configuration, persistence, scheduling, and cutoff behavior for automatically removing old completed tasks.

### Modified Capabilities

None.

## Impact

- Settings MVI contract, ViewModel, Compose content, previews, and localized resources.
- SharedPreferences-backed settings persistence.
- Task domain repository/use-case surface and Room DAO cleanup query.
- WorkManager scheduling and a Hilt-enabled cleanup worker.
- Unit and UI tests for persistence, cutoff behavior, scheduling, worker execution, and settings interaction.
- No Room schema version change and no new dependency are expected because tasks already store `completedAt` and WorkManager is already present.
