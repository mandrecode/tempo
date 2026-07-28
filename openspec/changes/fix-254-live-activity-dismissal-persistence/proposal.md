## Why

Since the notification-recovery fix for [#74](https://github.com/mandrecode/tempo/issues/74), opening the app re-posts habit-chain live activity notifications that the user already discarded, including ones belonging to previous days ([#254](https://github.com/mandrecode/tempo/issues/254)). The recovery path persists a bare set of "active" chain IDs with no record of user dismissal and no date, so `RescheduleRemindersWorker` treats every persisted ID as still-live and rebuilds its notification on every app open. Dismissing a live activity must be respected as a permanent user decision, without giving up the reboot/update/force-stop recovery that #74 delivered.

## What Changes

- The habit-chain live activity notification gains a delete intent, so a user dismissal (swipe or "clear all") is observed by the app and immediately clears that chain's persisted active-live-activity record.
- The persisted active-live-activity record is scoped to the date its progress belongs to. A record whose date is not today is cleared during recovery instead of being rebuilt against today's (unrelated) progress.
- Recovery reruns (boot, app update, immediate reopen) only rebuild live activities that are still both undismissed and for today; everything else is cleared.
- **BREAKING** (internal persistence only, no user-facing migration): the `active_live_activity_prefs` payload changes from chain IDs to date-stamped entries. Legacy undated entries are treated as stale and dropped on first read, which also self-heals records already stuck from the #254 regression.

Non-goals:

- Reverting the #74 recovery behaviour. Reboot, app update, and force-stop recovery of reminders and live activities stay intact.
- Changing when a live activity is first created, its content, its actions, or its Android 16 `ProgressStyle` presentation.
- Changing task or habit (non-chain) reminder notifications, or the periodic 24h refresh cadence.

## Capabilities

### New Capabilities
- `habit-chain-live-activity-lifecycle`: when a habit-chain live activity is considered active, how user dismissal ends it permanently, and which recorded live activities recovery is allowed to rebuild.

### Modified Capabilities
<!-- The notification-recovery capability from #74 has not been promoted to openspec/specs/ yet
     (its change is complete but unarchived), so its recovery-rebuild rule is narrowed here through
     the new lifecycle capability rather than through a delta on a spec that does not exist. -->

## Impact

- `core/data/preferences/ActiveLiveActivityPreferences` + `ActiveLiveActivityPreferencesImpl`: record shape becomes chain ID → date; legacy entries dropped on read.
- `infrastructure/liveactivity/HabitChainLiveActivityManager`: persists the date alongside the chain ID and attaches the delete intent to the posted notification.
- `infrastructure/reminders/receivers/`: new receiver handling live-activity dismissal, registered in `AndroidManifest.xml`.
- `infrastructure/reminders/workers/RescheduleRemindersWorker`: resync skips and clears non-today records.
- `infrastructure/notifications/RequestCodeGenerator`: new request-code range for the dismissal `PendingIntent`.
- Unit tests for the preferences, the live activity manager, the worker, and the new receiver.
