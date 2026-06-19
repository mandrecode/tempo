## Context

`HabitRepositoryImpl.toggleHabitCompletion` updates chain completion history and habit-chain live activity only when:

- the selected date is today, or
- the action came from a notification (`fromNotification = true`).

This prevents stale updates for arbitrary past edits, but it also blocks a valid flow: the user starts a chain from a notification (which creates a live activity for a scheduled past date after midnight) and then continues checking habits inside the app (`fromNotification = false`).

## Decision

For non-today, in-app toggles, update chain state only for chains that currently have an active live activity session.

### Data and behavior invariants

- Habit completion history is still updated for the selected date.
- Live activity updates remain immediate for today and notification-triggered actions.
- Past-date in-app updates remain skipped unless a chain is actively represented in live activity.
- When multiple chains include the same habit, only chains with active live activity are updated in the past-date in-app path.

## Implementation notes

- Compute `chainIdsForUpdate` in `HabitRepositoryImpl.toggleHabitCompletion`:
  - today or `fromNotification`: all chain IDs containing the habit.
  - past date + in-app action: only chain IDs where `liveActivityManager.hasActiveLiveActivity(chainId)` is true.
- Reuse existing downstream logic (ordered habits, chain completionHistory update, `updateLiveActivityForChain`) for selected chain IDs.
- Keep transaction boundaries unchanged (`withTransaction` for DB writes/snapshot collection, live activity updates after commit).

## Risks / Trade-offs

- Active-live-activity tracking is process-memory based, so continuation behavior depends on the live activity having been started in the current process lifecycle.
- This intentionally avoids broadening historical edit behavior to all chains, preserving existing expectations.
