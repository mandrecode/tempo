## Why

Resolves [#257](https://github.com/mandrecode/tempo/issues/257). A task reminder that fires while the device is off, or that the user swipes away without acting on, is gone forever: `TaskReminderSchedulerImpl` returns `Skipped` for any reminder whose trigger time is already in the past, so a missed reminder is never re-delivered. Users lose track of tasks that were important enough to set a reminder for, and the only way to notice is to open the app.

## What Changes

- Add a daily **missed-reminder catch-up**: at a configured time (default `09:00`) the app notifies for every task that is still incomplete and whose reminder time has already passed.
- The catch-up **does not modify the task**. `reminderDate` keeps its original value, so due dates, overdue styling, and periodic recurrence metadata are untouched.
- The catch-up **repeats daily** until the task is completed or its reminder is cleared.
- Catch-up notifications reuse the existing task reminder notification (title, description, tap-to-open, "Mark as completed" action) and the existing task reminder channel, keyed by the same request code so a task never stacks duplicate notifications.
- Add a **Reminders** section to Settings with a switch to enable/disable the catch-up and a time picker to change the catch-up time. Default: enabled at `09:00`.
- Changing the time or toggling the switch re-arms the catch-up immediately; the alarm is also re-armed on boot, time/timezone change, package replace, and on the periodic reminder refresh.
- Announce the feature through the What's New sheet.

Non-goals:

- Habits and habit chains are out of scope. Their reminders auto-advance to the next matching repeat day (`HabitReminderDateUtil.advanceReminderIfNeeded`), so they do not silently disappear the way task reminders do.
- No digest/grouped summary notification, no per-task opt-out, no snooze action.
- No change to how the original reminder alarm is scheduled or to periodic-task rollover.

## Capabilities

### New Capabilities
- `missed-task-reminder-catch-up`: daily re-notification of overdue incomplete task reminders, its selection rules, its repeat behavior, and the user-configurable enable switch and time-of-day setting.

### Modified Capabilities

None. `task-reminder-rollover` keeps its current requirements — the catch-up observes overdue tasks without mutating them, so rollover behavior is unchanged.

## Impact

- **Domain** (`features/tasks/domain/`): new `MissedReminderPreferences` repository interface and a use case that selects overdue incomplete tasks for a given instant; new `MissedReminderScheduler` scheduler interface.
- **Data** (`core/data/preferences/`): `SharedPreferences`-backed preferences implementation bound in `core/di/PreferencesRepositoryModule.kt`; the scheduler implementation bound in `core/di/RepositoryModule.kt` alongside the other schedulers.
- **Infrastructure** (`infrastructure/reminders/`): new daily catch-up alarm scheduler and `BroadcastReceiver`; the task notification builder is extracted out of `TaskReminderReceiver` so both the original reminder and the catch-up post an identical notification; `BootAndTimeReceiver` and `RescheduleRemindersWorker` also re-arm the catch-up alarm.
- **UI** (`features/settings/presentation/`): new `RemindersSection` composable, new `UiState` fields and `UiEvent`s, reusing `core/ui/components/TempoTimePickerDialog`.
- **Resources**: new strings in `values/strings.xml` plus matching `values-es/strings.xml` entries; replaced What's New title/description.
- **Manifest**: one new `<receiver>` declaration. No new permissions — the catch-up uses the exact-alarm permission the app already requests, and degrades to an inexact alarm when it is unavailable.
- **Tests**: unit tests for the preferences, the overdue-selection use case, the catch-up scheduler, the receiver's selection logic, and the Settings ViewModel; UI test for the new Settings section.
