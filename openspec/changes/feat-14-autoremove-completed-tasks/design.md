## Context

Tasks already persist a nullable `completedAt` `LocalDateTime`, and manual completed-task deletion removes completed top-level tasks plus their subtasks. Tempo already depends on WorkManager and uses Hilt workers for reminder maintenance. Settings persist lightweight values with SharedPreferences-backed repositories and expose them as flows to `SettingsViewModel`.

This change crosses presentation, domain, data, and Android infrastructure. It must preserve current retention behavior for existing users, avoid a Room migration, and remain correct when Android delays background work.

## Goals / Non-Goals

**Goals:**

- Let users enable or disable automatic completed-task removal and choose a retention period from 1 through 365 days.
- Persist a default retention value of 30 days while leaving automatic removal disabled by default.
- Remove eligible completed top-level tasks and their subtasks atomically.
- Run cleanup promptly after enabling or changing the policy and continue attempting cleanup daily.
- Make cleanup idempotent and safe to retry.

**Non-Goals:**

- Guarantee cleanup at an exact wall-clock time; WorkManager execution remains inexact.
- Add a recycle bin, undo flow, cloud synchronization, or configurable hour of day.
- Delete completed subtasks independently while their top-level parent remains active.
- Change manual "delete completed" behavior or the Room schema.

## Decisions

### Persist retention through a focused repository contract

Define a completed-task retention preferences contract with reactive enabled and day values, backed by SharedPreferences. Values read from storage are clamped to the supported 1..365 range, with 30 days as the default.

This keeps Android persistence out of domain behavior and lets settings observe changes. Reusing `AppPreferencesRepository` was considered, but that repository currently owns only language and would become an unrelated settings bucket.

### Orchestrate configuration in a use case

A configuration use case will validate/clamp the selected day value, persist the policy, and tell a domain scheduler interface to enable/reschedule or cancel cleanup. Settings remains a thin MVI orchestrator while the multi-component workflow is testable without Android.

Direct WorkManager calls from `SettingsViewModel` were rejected because they would couple presentation to Android infrastructure.

### Use one immediate request plus unique periodic work

The Android scheduler will enqueue a unique one-time cleanup when an enabled policy is saved, and unique periodic work with a 24-hour interval for continued maintenance. Reconfiguration replaces the scheduled policy; disabling cancels both unique work names.

Periodic WorkManager is preferred over app-start-only cleanup because it also operates when users do not reopen the task screen. Exact alarms are unnecessary and inappropriate for non-time-critical maintenance.

### Compute a local timestamp cutoff at execution time

The worker reads the latest persisted policy on every run. If disabled, it succeeds without deletion. If enabled, it computes `currentLocalDateTime - retentionDays` using `kotlinx-datetime` and invokes a domain cleanup use case.

Reading settings at execution time prevents queued work from applying a stale policy. A task is eligible when it is completed, has a non-null `completedAt`, is top-level, and `completedAt` is at or before the cutoff. Tasks lacking a completion timestamp are retained because their age cannot be established safely.

### Delete parent task trees in a Room transaction

The DAO selects eligible top-level task IDs. The repository deletes subtasks for those IDs and then deletes the parents inside the existing Room transaction boundary. WorkManager scheduling occurs outside this database transaction.

The operation is idempotent: rerunning with the same cutoff finds no already-deleted IDs. A periodic task's active next occurrence is not a subtask and is therefore preserved even when its archived completed predecessor is removed.

### Present a switch and bounded numeric input

Settings adds a task-cleanup section containing a switch and, while enabled, a numeric day input with decrement/increment controls. The UI prevents values outside 1..365 and emits policy changes through the settings contract. All copy is localized in English and Spanish, and preview coverage remains under `src/debug`.

## Risks / Trade-offs

- [WorkManager may run later than 24 hours under system constraints] -> Treat the retention period as a minimum age and document behavior through UI wording rather than promising an exact deletion time.
- [Local time-zone changes can shift the effective instant represented by `completedAt`] -> Use the same local date-time model already stored for task completion so comparisons remain consistent with existing data.
- [Automatic deletion is destructive] -> Keep it disabled by default, expose the active retention value clearly, and never delete records with missing completion timestamps.
- [Rapid setting changes can enqueue overlapping immediate requests] -> Use unique one-time work with replacement semantics and have the worker re-read the latest preferences.

## Migration Plan

No database migration is required. Existing users receive disabled automatic cleanup and a stored-or-default 30-day value. Rolling back the app leaves only harmless SharedPreferences keys and WorkManager records; an older build will ignore the keys, while the unique work can no longer instantiate once removed and will fail without changing task data.

## Open Questions

None.
