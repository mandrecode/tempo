## Why

Task, habit, and habit-chain reminders can silently stop firing after the app is force-closed, updated, or the phone reboots, and any in-progress habit-chain live activity notification disappears without being restored (GitHub issue #74). Android clears `AlarmManager` alarms on app update and force-stop, and a force-stopped app never receives the `BOOT_COMPLETED` broadcast until the user explicitly reopens it — so today's recovery path (`BootAndTimeReceiver` → `RescheduleRemindersWorker`, plus a 24h periodic refresh enqueued only from `MainActivity`) leaves two gaps: no trigger at all on app update (`MY_PACKAGE_REPLACED` is not handled), and up to a 24h silent gap after a force-stop before the periodic worker re-arms alarms. Habit-chain live activities are tracked only in an in-memory set, so their ongoing notification is never recreated after any of these events even though the underlying chain progress is still in Room.

## What Changes

- Handle `ACTION_MY_PACKAGE_REPLACED` in the existing boot/time receiver so an app update immediately re-arms all persisted task/habit/chain reminders via `RescheduleRemindersWorker`, the same convergence point already used for boot and time-change recovery.
- Enqueue an immediate one-off `RescheduleRemindersWorker` run at app startup (alongside the existing periodic 24h refresh, at the same deferred post-first-frame point mandated by `startup-performance`), so reopening a force-stopped app re-arms alarms right away instead of waiting up to 24h.
- Persist the set of habit-chain IDs with an active live-activity notification (SharedPreferences-backed, following the existing preferences-repo pattern) so that identity survives process death, app update, and reboot.
- Extend `RescheduleRemindersWorker` to resync each persisted active chain's live activity notification from current Room state after rescheduling reminders, recreating the ongoing notification if it was cleared (reboot) or correcting/dismissing it if the chain's state changed while the app was closed (self-healing — reuses the existing dismiss-on-stale-state logic in `HabitChainLiveActivityManager`).

## Capabilities

### New Capabilities
- `notification-recovery`: automatic restoration of scheduled task/habit/chain reminders and in-progress habit-chain live-activity notifications after the app is force-closed and reopened, updated, or the device reboots.

### Modified Capabilities
(none — existing specs are unaffected; this introduces new requirements under a new capability rather than changing existing ones)

## Impact

- `app/src/main/AndroidManifest.xml` — add `MY_PACKAGE_REPLACED` intent-filter action to the existing receiver.
- `infrastructure/reminders/receivers/BootAndTimeReceiver.kt` — handle the new action.
- `MainActivity.kt` — enqueue an immediate one-off reschedule work item in addition to the periodic refresh.
- `infrastructure/reminders/workers/RescheduleRemindersWorker.kt` — after rescheduling alarms, resync persisted active chain live activities.
- `infrastructure/liveactivity/HabitChainLiveActivityManager.kt` — persist/load `activeChains` via a new SharedPreferences-backed store instead of purely in-memory.
- New: a small SharedPreferences-backed repository for active live-activity chain IDs, bound in `core/di/PreferencesRepositoryModule.kt`.
- No Room schema changes; no new permissions (`MY_PACKAGE_REPLACED` requires none).
