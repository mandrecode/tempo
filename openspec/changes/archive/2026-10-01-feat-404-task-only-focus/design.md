## Context

Focus interleaves tasks, habits and chains. Agenda counts drive the hero; a separate daily recorder writes aggregate history and is also invoked by routine completion. Undated tasks already have a planning sheet. An active Spaces proposal also touches Focus.

## Goals / Non-Goals

**Goals:** Make every current Focus surface task-only, preserve Today-before-Overdue ordering, subtask deduplication, sessions and deliberate planning, and document the history transition.

**Non-Goals:** Changing task reminders/recurrence, starting sessions on undated tasks without planning, changing streak scoring, rewriting historical records, implementing Spaces, or redesigning adaptive layout.

## Decisions

- Remove routine agenda variants and all Focus routine events/editors rather than merely hiding cards. Observe tasks, categories and session totals only.
- Keep the shared `isOnFocusDay` membership for both agenda and daily recording. Undated top-level open tasks remain a footer count and planning-sheet population.
- Record task counts only from deployment onward, recomputing the current day idempotently. Preserve past aggregate rows and focus minutes. Historical records cannot distinguish tasks from routines, so reconstruction or reset would lose valid history.
- Remove the daily activity recorder call from routine completion; routine changes must no longer influence Focus. Keep task mutation/session recording paths.
- Retain the one-day observer mechanism and task-triggered refresh; replace obsolete habit midnight coverage with task coverage.
- Update Spaces artifacts with a task-only compatibility note without implementing or otherwise changing that proposal.

## Risks / Trade-offs

- Historical streak/heatmap can include pre-change routines → document snapshot semantics and test past rows remain unchanged.
- Existing screenshot/debug fixtures instantiate routine variants → update fixtures to meaningful task-only samples and review any changed screenshot references.
- Removing branches resolves baseline suppressions → prune only resolved entries and ratchet the CI ceiling down.

## Migration Plan

No schema migration or scheduler changes. Today's counts are refreshed with the task-only population; earlier records remain intact. Rollback restores old membership without recovering historical per-type detail.
