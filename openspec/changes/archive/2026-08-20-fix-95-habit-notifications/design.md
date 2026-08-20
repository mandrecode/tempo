## Context

Habit completion uses both date-specific `completionHistory` and a legacy `isCompleted` boolean. The boolean is updated for today's UI state, but it is not a durable representation of whether a future reminder occurrence is complete.

Current habit reminder scheduling skips any `Habit` with `isCompleted == true`. When a user completes today's habit, `ToggleHabitCompletionUseCase` advances the habit's `reminderDate` and calls `UpdateHabitUseCase`, which schedules that advanced habit while `isCompleted` is still true. The scheduler skips the future alarm, so no next notification is registered. WorkManager resync has the same problem because it delegates to the same scheduler.

Related issue #73 correctly identified that an already-completed occurrence should not notify, but the fix applied that rule at scheduling time. Issue #72 is the same occurrence-date class of problem for delayed alarms: if Android delivers an overdue alarm after the user completed it in-app, the receiver must suppress the original occurrence even if the stored reminder has already advanced.

Issue #91 is adjacent but separate: live habit-chain notification taps also need to carry the scheduled date into navigation so the app opens the correct day after midnight.

## Goals / Non-Goals

**Goals:**
- Schedule future habit alarms based on reminder time and exact-alarm availability, independent of the legacy completion flag.
- Avoid redundant notifications when the specific reminder occurrence date is already completed.
- Keep side effects idempotent: scheduling an existing future reminder replaces the same PendingIntent, and cancellation still dismisses the matching notification.

**Non-Goals:**
- Do not change Room schema or migrate the legacy `isCompleted` field.
- Do not alter task reminder rollover behavior.
- Do not change notification permission/channel UX.

## Decisions

- Treat `Habit.reminderDate` as the scheduling source of truth.
  - Rationale: scheduling future alarms is about whether the reminder time is valid and in the future.
  - Alternative considered: clear `isCompleted` when advancing to tomorrow. That would couple scheduling to UI state cleanup and risks changing habit list behavior outside notifications.

- Treat `completionHistory` as the notification display source of truth.
  - Rationale: completion history is date-specific, which matches a reminder occurrence date.
  - Alternative considered: keep using `isCompleted` in the receiver. That reproduces the cross-day stale flag failure.

- Keep Android scheduler side effects outside repository transactions.
  - Rationale: existing use cases already update persistence before calling schedulers; this change only adjusts scheduler/receiver decision logic.

## Risks / Trade-offs

- Stale alarm for an already-completed occurrence could still reach the receiver -> mitigate by checking `completionHistory` for the scheduled reminder date before showing.
- Legacy `isCompleted` remains confusing for infrastructure code -> mitigate with tests that lock scheduling/display behavior to date-specific semantics.
