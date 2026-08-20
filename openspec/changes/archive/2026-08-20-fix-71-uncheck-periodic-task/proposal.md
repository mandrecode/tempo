## Why

Unchecking a completed periodic task currently rolls back its generated next occurrence: the open occurrence is deleted, recurrence is restored to the completed occurrence, its reminder is advanced, and a misleading “Next occurrence cancelled” message appears. [GitHub issue #71](https://github.com/mandrecode/tempo/issues/71) requires the completed occurrence to return as an overdue, non-periodic task while the already-created next occurrence remains open.

## What Changes

- When a completed periodic occurrence is unchecked, keep the linked next occurrence intact.
- Restore the unchecked occurrence with its original reminder date, no recurrence metadata, no completion timestamp, and no link to the next occurrence.
- Preserve the existing restoration behavior for subtasks that were auto-completed with the parent occurrence.
- Treat the action as a normal uncheck in presentation behavior, without showing the “Next occurrence cancelled” message.
- Add regression coverage for the domain and presentation behavior.
- Non-goals: changing periodic completion, overdue rollover creation, recurrence calculation, or database schema.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-reminder-rollover`: Define how an archived periodic occurrence is restored when the user unchecks it after a next occurrence already exists.

## Impact

- Task completion/uncheck domain behavior in `ToggleTaskCompletionUseCase`.
- Task presentation handling for completion-toggle results and snackbars.
- Unit tests for the task domain use case and ViewModel.
- No new dependencies, persistence migrations, or public APIs.
