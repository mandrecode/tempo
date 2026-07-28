## Context

Task reminders are exact `AlarmManager` alarms armed by `TaskReminderSchedulerImpl` and delivered by `TaskReminderReceiver`, which builds and posts the notification inline. `schedulePendingTask` returns `ScheduleResult.Skipped` whenever `triggerAtMillis < currentTimeMillis`, so a reminder whose moment has passed — device off, app force-stopped, alarm dropped, or notification swiped away — is never delivered again. `RescheduleRemindersWorker` re-arms alarms every 24h and on boot, but it inherits the same past-is-skipped rule for non-periodic tasks; only periodic tasks get special treatment through `RollOverduePeriodicTaskUseCase`, which preserves the overdue occurrence and spawns the next one.

The feature adds a second, task-independent trigger: one daily alarm that sweeps for overdue incomplete tasks and re-posts their reminder notifications, without touching task data. See `proposal.md` for motivation and `specs/missed-task-reminder-catch-up/spec.md` for the normative requirements.

## Goals / Non-Goals

**Goals:**
- Deliver a notification for every incomplete task whose reminder time has passed, once per day at a user-configured time (default 09:00).
- Keep task state untouched: no reminder-date rewrite, no completion, no recurrence side effects.
- Reuse the existing task reminder notification verbatim (content, channel, actions, per-task notification id).
- Survive reboot, time/timezone change, package replacement, and force-stop-then-relaunch.
- Keep the whole selection rule pure-Kotlin and unit-testable without Robolectric.

**Non-Goals:**
- Habits and habit chains (their reminders auto-advance; nothing is silently lost).
- Grouped/digest notifications, snooze, per-task opt-out, quiet hours.
- Including the new preference in backup export/import (`BackupSettings` versioning is out of scope here).
- Changing `TaskReminderSchedulerImpl`'s past-is-skipped rule or `RollOverduePeriodicTaskUseCase`.

## Decisions

### D1: One daily `AlarmManager` alarm, self-re-arming — not a WorkManager periodic worker

The catch-up is a single exact alarm (`setExactAndAllowWhileIdle`, `RTC_WAKEUP`) armed for the next occurrence of the configured local time; after it fires, the receiver arms the next day's alarm as its last step.

*Why:* the trigger must land at a wall-clock time the user picked. `PeriodicWorkRequest` has no wall-clock anchor — its 24h period drifts from the enqueue moment and can be deferred by Doze/battery policy, so "09:00" would decay into "sometime in the morning, eventually". Reminder delivery in this app is already `AlarmManager`-based, so this keeps one mental model.

*Alternative considered:* one-time `WorkRequest` with an initial delay recomputed after each run. Same re-arm bookkeeping as the alarm, still no exactness guarantee and an extra process wake-up path. Rejected.

*Alternative considered:* piggybacking on `RescheduleRemindersWorker`. Rejected — it runs on WorkManager's schedule, not the user's chosen time, and mixing "re-arm alarms" with "notify the user" in one worker confuses two different failure modes.

### D2: Fixed request code for the catch-up alarm

`RequestCodeGenerator` partitions Int request codes by entity type (tasks 0–999,999, habits, chains, live activity). The catch-up alarm gets a new dedicated constant in the next free range (`4 * RANGE_SIZE`), exposed as `RequestCodeGenerator.forMissedReminderCatchUp()`. Reusing a task code would let a task alarm cancel the catch-up.

### D3: Extract the notification builder into `TaskReminderNotifier`

`TaskReminderReceiver.showNotification` moves to `infrastructure/notifications/TaskReminderNotifier`, an injectable class with `fun notify(task: Task)`. Both `TaskReminderReceiver` and the new catch-up receiver call it.

*Why:* requirement "Catch-up notification is the task reminder notification" is only durable if there is literally one builder. Copying the builder guarantees drift the first time the notification changes. The extraction is behavior-preserving — including the periodic-only `EXTRA_ORIGINAL_REMINDER_DATE` embedding, the `RequestCodeGenerator.forTask` id (which is what makes catch-up *replace* rather than duplicate an existing notification), the channel bootstrap, and the `canPostNotifications` early return.

### D4: Selection lives in a pure domain use case

`GetOverdueIncompleteTasksUseCase(now: LocalDateTime): List<Task>` reads `taskRepository.getTasksWithReminders()` and keeps tasks where `!isCompleted && reminderDate != null && reminderDate < now`. Same DAO query `RescheduleRemindersWorker` already uses; no new Room query, no schema change.

Note the selection is deliberately *not* filtered by `parentTaskId` or `periodicity`: any task that would have produced a reminder notification can produce a catch-up notification, which mirrors `TaskReminderReceiver.shouldProcessTaskReminder` (`task != null && !task.isCompleted`).

### D5: Preferences follow the `CompletedTaskRetentionPreferences` shape

`MissedReminderPreferences` (interface in `features/tasks/domain/repository/`, impl `MissedReminderPreferencesImpl` in `core/data/preferences/`, bound in `PreferencesRepositoryModule`) exposes `isEnabled: StateFlow<Boolean>` and `catchUpTime: StateFlow<LocalTime>` with synchronous setters, backed by its own `SharedPreferences` file. The time is persisted as minute-of-day `Int` and clamped to `0..1439` on read, so a corrupt value falls back to the 09:00 default rather than throwing. `LocalTime` is `kotlinx-datetime`, so the interface stays pure Kotlin.

Defaults: `DEFAULT_ENABLED = true`, `DEFAULT_CATCH_UP_TIME = LocalTime(9, 0)`. Enabled-by-default is what the issue asks for; users who dislike it have a one-tap switch.

### D6: Arming is centralized in `MissedReminderScheduler`

Domain interface `MissedReminderScheduler { fun sync() }` with `MissedReminderSchedulerImpl` in `infrastructure/reminders/`. `sync()` reads the preferences and either cancels the alarm (disabled) or arms it for the next occurrence of the configured time, computed as: today at `catchUpTime` if that is strictly after `now`, otherwise tomorrow at `catchUpTime`, resolved through `TimeZone.currentSystemDefault()`. It is idempotent — calling it repeatedly leaves exactly one armed alarm, because the fixed request code + `FLAG_UPDATE_CURRENT` replaces any previous one.

`sync()` is called from: app startup (next to `ReminderRefreshScheduler.enqueueImmediateRefresh`), the Settings ViewModel after either preference changes, `RescheduleRemindersWorker` (24h safety net), `BootAndTimeReceiver` (boot, time change, timezone change, package replace), and the catch-up receiver itself after each sweep.

### D7: Inexact fallback instead of failure

If `canScheduleExactAlarms()` is false, `sync()` arms `setAndAllowWhileIdle` instead of `setExactAndAllowWhileIdle`. A catch-up delayed by minutes is still useful, and the app must not require the exact-alarm grant to be usable. No new permission is declared.

### D8: Settings placement

A new `RemindersSection` composable, built from the existing `SettingsSection`/`SettingsSwitchItem` primitives, placed above the "Completed tasks" section in `SettingsContent`. The time row is revealed with the same `AnimatedVisibility` pattern `CompletedTaskRetentionSection` uses for its stepper, and opens the existing `core/ui/components/TempoTimePickerDialog`. New `UiState` fields `missedReminderCatchUpEnabled: Boolean` and `missedReminderCatchUpTime: LocalTime`; new events `MissedReminderCatchUpToggled(enabled)` and `MissedReminderCatchUpTimeChanged(time)`.

## Data invariants and idempotency

- **No writes.** The catch-up path performs zero repository writes. `reminderDate`, `isCompleted`, `periodicity`, `parentTaskId`, and `nextInstanceId` are read-only inputs. This is what keeps `task-reminder-rollover`'s requirements intact and makes the sweep safe to run twice.
- **At most one armed catch-up alarm** at any time, enforced by the fixed request code — re-running `sync()` from several call sites (startup + worker + boot) cannot produce duplicates or duplicate notifications.
- **At most one notification per task per sweep**, keyed by `RequestCodeGenerator.forTask(id)`; a re-run of the same sweep updates the same notification rather than adding one.
- **Read consistency:** the sweep resolves `now` once (injected `Clock`) and uses that single instant for every comparison, so a task cannot be judged against two different "now"s within one run.
- **Preference writes are synchronous** (`SharedPreferences.edit` + `StateFlow` update, as in `CompletedTaskRetentionPreferencesImpl`), so `sync()` called right after a setter always observes the new value.

## Transaction boundaries vs. Android scheduler side effects

- There is **no database transaction** in this feature — the sweep is a single read (`getTasksWithReminders()`) followed by pure filtering. Nothing to roll back.
- **Scheduler side effects** are: (a) `AlarmManager.setExactAndAllowWhileIdle` / `setAndAllowWhileIdle` / `cancel` for the catch-up alarm, and (b) `NotificationManager.notify` per selected task. Both happen outside any transaction and are not undone if a later step fails.
- Ordering inside the receiver: post notifications first, **then** re-arm tomorrow's alarm, all inside a single `goAsync()` window with the re-arm in a `finally`. A failure while notifying must not cost the user the next day's catch-up; the 24h `RescheduleRemindersWorker` and boot receiver are the outer safety nets if even that fails.
- The Settings ViewModel writes the preference **before** calling `sync()`; the scheduler reads state from preferences rather than from event payloads, so a lost/duplicated `sync()` call still converges to the persisted intent.

## Risks / Trade-offs

- **An overdue periodic occurrence nags forever.** `RollOverduePeriodicTaskUseCase` intentionally keeps the missed occurrence incomplete with its old reminder date, so the catch-up will surface it every morning until the user acts. → Accepted and intended ("repeat until resolved"); completing or deleting the occurrence, or clearing its reminder, ends it. Auto-remove of completed tasks then cleans up.
- **A long-neglected backlog produces a burst of notifications.** Ten overdue tasks means ten notifications at 09:00. → Mitigated by them sharing one channel and collapsing under the system's automatic bundling; a digest notification is a deliberate non-goal for this change.
- **Notification IDs collide across tasks whose ids differ by exactly 1,000,000** — a pre-existing `RequestCodeGenerator` property, not introduced here, and now reachable from a second code path. → Unchanged risk profile; deliberately not fixed in this change.
- **Exact-alarm denial delays the catch-up** (D7). → Accepted; the reminder is a nudge, not a deadline.
- **Enabled by default changes behavior for existing users on upgrade** — the first launch after update arms a 09:00 catch-up that may fire for old, long-ignored tasks. → The What's New sheet announces the feature and the switch is in Settings; this is the behavior the issue asks for.
- **The preference is not backed up.** A Replace import will not restore the catch-up time. → Documented as a non-goal; the default is restored instead of a wrong value.

## Migration Plan

No data migration: no Room entity, DAO, or schema change, and no existing preference is read or rewritten. Rollout is the normal app update — on first launch the new preference file is absent, the defaults (enabled, 09:00) apply, and startup `sync()` arms the first alarm. Rollback is equally clean: an older build never arms the alarm and simply ignores the orphan preference file; any alarm armed by the newer build dies with the app's alarm set on reinstall/downgrade.

## Open Questions

- Should the catch-up notification visually mark itself as a *missed* reminder (e.g. subtext with the original reminder date) rather than being byte-identical to the original? Current design says identical, for the strongest "one builder" guarantee; adding subtext later is additive and needs new localized strings.
- Should the preference join `BackupSettings` in a later change, alongside the other Settings values already exported?
