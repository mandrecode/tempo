## 1. Domain

- [x] 1.1 Add `MissedReminderPreferences` interface in `features/tasks/domain/repository/` with `isEnabled: StateFlow<Boolean>`, `catchUpTime: StateFlow<LocalTime>`, `setEnabled(enabled)`, `setCatchUpTime(time)`, and companion constants `DEFAULT_ENABLED = true`, `DEFAULT_CATCH_UP_TIME = LocalTime(9, 0)`, plus a `normalizeMinuteOfDay` helper clamping to `0..1439`
- [x] 1.2 Add `GetOverdueIncompleteTasksUseCase` in `features/tasks/domain/usecase/` returning tasks from `TaskRepository.getTasksWithReminders()` where `!isCompleted && reminderDate != null && reminderDate < now`
- [x] 1.3 Add `MissedReminderScheduler` interface (`fun sync()`) in `features/tasks/domain/scheduler/`

## 2. Data / DI

- [x] 2.1 Add `MissedReminderPreferencesImpl` in `core/data/preferences/` backed by its own `SharedPreferences` file, persisting the time as minute-of-day `Int` and clamping on read (mirror `CompletedTaskRetentionPreferencesImpl`)
- [x] 2.2 Bind `MissedReminderPreferences` in `core/di/PreferencesRepositoryModule.kt`
- [x] 2.3 Bind `MissedReminderScheduler` in `core/di/InfrastructureModule.kt` next to the other scheduler providers

## 3. Notification extraction

- [x] 3.1 Create `infrastructure/notifications/TaskReminderNotifier` with `fun notify(task: Task)` by moving `TaskReminderReceiver.showNotification` verbatim, including channel bootstrap, `canPostNotifications` early return, `RequestCodeGenerator.forTask` id, the periodic-only `EXTRA_ORIGINAL_REMINDER_DATE` embedding, and the "Mark as completed" action
- [x] 3.2 Inject `TaskReminderNotifier` into `TaskReminderReceiver` and delete the inlined builder, keeping delivery behavior identical

## 4. Catch-up scheduling and delivery

- [x] 4.1 Add `forMissedReminderCatchUp()` to `RequestCodeGenerator` using the next free range (`4 * RANGE_SIZE`), documented in the class KDoc partition table
- [x] 4.2 Add `MissedReminderAlarmScheduler` interface + `AndroidMissedReminderAlarmScheduler` in `infrastructure/reminders/scheduler/` exposing `canScheduleExactAlarms()`, `scheduleCatchUp(triggerAtMillis)` (exact when permitted, `setAndAllowWhileIdle` otherwise), and `cancelCatchUp()`
- [x] 4.3 Add `MissedReminderSchedulerImpl` in `infrastructure/reminders/`: cancel when disabled, otherwise arm the next occurrence of `catchUpTime` (today if strictly after `now`, else tomorrow) via `TimeZone.currentSystemDefault()`; idempotent across repeated calls
- [x] 4.4 Add `MissedReminderCatchUpRunner` (the sweep: enabled check, notify every task from `GetOverdueIncompleteTasksUseCase` with a single injected-`Clock` `now`, re-arm via `MissedReminderScheduler.sync()` in a `finally`) plus a thin `MissedReminderCatchUpReceiver` that calls it inside `goAsync()` — the split keeps the sweep unit-testable without Robolectric
- [x] 4.5 Register the receiver in `AndroidManifest.xml` (`exported="false"`, no new permission)
- [x] 4.6 Call `MissedReminderScheduler.sync()` from `RescheduleRemindersWorker`, which already covers startup (`enqueueImmediateRefresh`), boot, time/timezone change, package replace, and the 24h refresh — no separate `MainActivity` or `BootAndTimeReceiver` wiring needed

## 5. Settings UI

- [x] 5.1 Add `missedReminderCatchUpEnabled: Boolean` and `missedReminderCatchUpTime: LocalTime` to `SettingsContract.UiState`, plus `MissedReminderCatchUpToggled` and `MissedReminderCatchUpTimeChanged` events
- [x] 5.2 Wire `SettingsViewModel` to collect both preference flows and, on each event, persist the preference and then call `MissedReminderScheduler.sync()`
- [x] 5.3 Add `RemindersSection.kt` using `SettingsSection`/`SettingsSwitchItem`, with the time row revealed by `AnimatedVisibility` and opening `TempoTimePickerDialog`
- [x] 5.4 Render the section in `SettingsContent` above the completed-tasks section and add previews under `src/debug/`
- [x] 5.5 Add the new strings to `values/strings.xml` and matching entries to `values-es/strings.xml` (section title, switch label, time row label, dialog title, formatted time content descriptions)

## 6. What's New

- [x] 6.1 Replace `WhatsNewRegistry.latest` with a `missed-reminder-catch-up` entry and update `whats_new_title` / `whats_new_description` in both `values/` and `values-es/`, removing the previous entry's now-unused strings

## 7. Tests

- [x] 7.1 Unit-test `MissedReminderPreferencesImpl`: defaults, persistence round-trip, out-of-range minute-of-day falls back to 09:00
- [x] 7.2 Unit-test `GetOverdueIncompleteTasksUseCase`: overdue incomplete included; completed, no-reminder, and future-reminder excluded; reminder earlier the same day included; no repository writes
- [x] 7.3 Unit-test `MissedReminderSchedulerImpl`: disabled cancels; time later today arms today; time already passed arms tomorrow; repeated `sync()` leaves one alarm; inexact fallback when exact alarms are unavailable
- [x] 7.4 Unit-test `MissedReminderCatchUpRunner`: notifies each selected task once, skips the sweep when disabled, re-arms after a successful sweep, and still re-arms when notifying throws
- [x] 7.5 Unit-test `DateTimeFormatter.formatTimeOfDay` and confirm `RequestCodeGenerator.forMissedReminderCatchUp()` cannot collide with a task/habit/chain code
- [x] 7.6 Extend `SettingsViewModelTest` for the two new events (preference persisted, `sync()` called)
- [x] 7.7 Add a Compose UI test for `RemindersSection` (switch reveals/hides the time row, time row opens the picker)

## 8. Verification

- [x] 8.1 `./gradlew ktlintFormat` then `./gradlew ktlintCheck`
- [x] 8.2 `./gradlew compileDebugKotlin` and `./gradlew testDebugUnitTest`
- [x] 8.3 `./gradlew :app:detekt` (no new baseline entries) and `./gradlew lintDebug` (no `MissingTranslation` / `ExtraTranslation`)
- [x] 8.4 `./gradlew koverVerifyDebug`
- [x] 8.5 `openspec validate feat-257-missed-reminder-catch-up --strict`
- [x] 8.6 Manual smoke on device: set a reminder in the past, set the catch-up time a minute ahead, confirm the notification arrives, its "Mark as completed" action works, and the task's reminder date is unchanged
