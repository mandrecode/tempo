## Context

Periodic completion archives the completed occurrence with recurrence fields stripped and stores a `nextInstanceId` link to the generated open occurrence. The current uncheck branch interprets that link as an instruction to reverse the whole completion: it deletes the next occurrence, restores recurrence from it, advances the archived reminder, and returns a rollback-specific result that triggers a snackbar. Issue #71 instead treats the two occurrences as independent once the next one exists.

The archived task already retains its original `reminderDate`; only its recurrence fields are stripped. The `nextInstanceId` is therefore sufficient to recognize the special uncheck case without a schema change.

## Goals / Non-Goals

**Goals:**

- Restore the archived occurrence as incomplete at its original reminder date with recurrence disabled.
- Preserve the linked next occurrence and all of its scheduler state.
- Clear the archived occurrence's link so repeated toggles cannot affect the next occurrence.
- Restore only subtasks auto-completed by the original parent completion.
- Avoid a cancellation snackbar because no occurrence is cancelled.

**Non-Goals:**

- Changing how periodic completion or overdue rollover creates and links future occurrences.
- Changing recurrence calculations, Room schema, or notification delivery.
- Recombining the restored overdue occurrence with the recurrence chain.

## Decisions

1. **Detach instead of roll back.** The special completed-task branch will update the archived task to `isCompleted = false`, clear `completedAt` and `nextInstanceId`, and retain its already-stripped recurrence fields and original `reminderDate`. The linked next occurrence will not be read, updated, deleted, cancelled, or rescheduled. This directly preserves the data invariant that the open next occurrence remains the sole owner of its future reminder. Restoring recurrence from the next occurrence was rejected because it moves the original occurrence out of Overdue and creates two recurrence owners.

2. **Keep the task update and subtask restoration transactional.** The detached parent update and restoration of subtasks whose `completedAt` matches the parent's completion timestamp will run in the repository transaction. This preserves the existing all-or-nothing relationship between parent and automatically completed subtasks. Android scheduler calls remain outside the transaction.

3. **Reuse normal uncheck presentation semantics.** The domain result will report an incomplete parent through the existing `ParentToggled` result rather than a rollback-specific result. The ViewModel will expand the task and process scheduling errors normally, with no cancellation snackbar. Keeping a renamed special result was rejected because the UI requires no distinct user feedback.

4. **Schedule only the restored archived occurrence and restored subtasks.** After the transaction, the existing update/scheduler behavior will handle the restored parent. Because its reminder is in the past and recurrence is null, no future alarm is created. Restored subtasks keep their existing scheduler behavior. The linked next occurrence is deliberately untouched, which makes the operation idempotent with respect to that occurrence.

## Risks / Trade-offs

- [The restored task has a stale or unexpected recurrence value from legacy data] → Explicitly clear all recurrence fields during detachment rather than relying only on current archive invariants.
- [A scheduler side effect fails after the database transaction commits] → Preserve the existing post-commit error reporting; the next occurrence remains valid and untouched.
- [Previously completed subtasks are incorrectly reopened] → Continue matching the parent's exact completion timestamp so only auto-completed subtasks are restored.
- [Removing the rollback result affects callers] → Update exhaustive result handling and unit tests in the same change; the use case is internal to the app.

## Migration Plan

No data migration is required. Existing archived tasks with `nextInstanceId` adopt the corrected behavior on their next uncheck. Rollback is the normal application-code rollback because persistence shape is unchanged.

## Open Questions

None.
