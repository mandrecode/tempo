# Phase 3: Harden Reminders, Notifications & Receivers Infrastructure

## Why

GitHub issue [#751](https://github.com/mandrecode/tempo/issues/751) (Phase 3 of [#461](https://github.com/mandrecode/tempo/issues/461)) addresses infrastructure hardening for reminders, notifications, and broadcast receivers to improve reliability, security, and observability.

The audit identified five key deficiencies:

1. **Silent notification failures on Android 13+**: `NotificationManager.notify()` silently no-ops when `POST_NOTIFICATIONS` permission is denied. Without guards and logging, dropped notifications are unobservable, making it impossible to diagnose why reminders don't appear.

2. **Exported receiver for protected broadcasts**: `BootAndTimeReceiver` declares `android:exported="true"` in the manifest (line 68). Protected broadcasts (`BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`) only deliver to unexported receivers; the `exported="true"` is redundant and increases attack surface.

3. **Ad-hoc CoroutineScope in receivers**: Both `TaskReminderReceiver` and `HabitReminderReceiver` use `CoroutineScope(Dispatchers.IO)` instead of injected `@IoDispatcher`, harming testability and repeating the dispatcher pattern throughout the codebase.

4. **Missing explicit retry backoff**: `RescheduleRemindersWorker` uses Work's default backoff strategy on retry (exponential with unspecified bounds). Without an explicit `BackoffPolicy`, the timing and behavior are implicit and unpredictable.

5. **USE_EXACT_ALARM permission not evaluated**: `SCHEDULE_EXACT_ALARM` exists; `USE_EXACT_ALARM` is a play-store policy choice flagged for decision. For now, this is deferred.

## What Changes

### 1. Guard NotificationManager.notify() with POST_NOTIFICATIONS Permission Check

- Add a static `canPostNotifications(context: Context): Boolean` helper in an infrastructure utility (e.g., in a notification utilities file).
- In `TaskReminderReceiver.showNotification()` (line ~122), guard the `notificationManager.notify()` call with a check. If the permission is denied, log a warning-level message so operators can observe the drop.
- In `HabitReminderReceiver.showHabitNotification()` (line ~186) and `showHabitChainNotification()` (line ~248), apply the same guard and logging.
- In `HabitChainLiveActivityManager.updateLiveActivity()` (lines ~207, ~217), guard both `notificationManager.notify()` calls with the permission check and logging.

### 2. Set BootAndTimeReceiver android:exported="false"

- In `AndroidManifest.xml`, change the `BootAndTimeReceiver` declaration (line ~68) from `android:exported="true"` to `android:exported="false"`.
- Protected broadcasts still deliver to unexported receivers, so no behavior change occurs; only security posture improves.

### 3. Inject @IoDispatcher into Receiver onReceive Methods

- Add `@Inject lateinit var ioDispatcher: CoroutineDispatcher` annotated with `@IoDispatcher` to both `TaskReminderReceiver` and `HabitReminderReceiver`.
- Replace `CoroutineScope(Dispatchers.IO)` with `CoroutineScope(ioDispatcher)` in both receivers' `onReceive()` methods.

### 4. Add Explicit BackoffPolicy to RescheduleRemindersWorker

- In `BootAndTimeReceiver.onReceive()`, wrap the `OneTimeWorkRequestBuilder<RescheduleRemindersWorker>()` call with an explicit `BackoffPolicy` (e.g., `BackoffPolicy.linear(initialDelay, maxDelay)` where both are reasonable intervals like 2 seconds and 2 minutes).
- This makes retry timing observable and predictable.

### 5. Defer USE_EXACT_ALARM & DataStore Migration

- **USE_EXACT_ALARM**: Play-store policy decision; skip for now (mark as D5).
- **D4 (DataStore Migration)**: SharedPreferences work for non-sensitive prefs; DataStore is a nice-to-have. Deferred.

## Non-Goals

- Do not change notification content, channels, or behavior (only add guards).
- Do not modify reminder scheduling algorithms or timings.
- Do not add new receiver types or change broadcast handling logic.
- Do not alter domain models, use cases, or repository contracts.

## Capabilities

### New Capabilities

- `notifications-reliability`: Defines permission guards and error-handling guarantees for notification posting on Android 13+, ensuring silent failures are observable.
- `receiver-hardening`: Specifies security and dependency-injection improvements for broadcast receivers and background work scheduling.

### Modified Capabilities

- None.

## Impact

- `infrastructure/reminders/receivers/TaskReminderReceiver.kt`: add `@IoDispatcher` injection, guard `notify()` with permission check and logging.
- `infrastructure/reminders/receivers/HabitReminderReceiver.kt`: add `@IoDispatcher` injection, guard both `notify()` calls with permission check and logging.
- `infrastructure/liveactivity/HabitChainLiveActivityManager.kt`: guard both `notify()` calls with permission check and logging.
- `infrastructure/reminders/receivers/BootAndTimeReceiver.kt`: add explicit `BackoffPolicy` to `RescheduleRemindersWorker` retry configuration.
- `app/src/main/AndroidManifest.xml`: change `BootAndTimeReceiver` `android:exported="false"` (line ~68).
- Utility function (or extension) for permission checks: add `canPostNotifications()` helper.
