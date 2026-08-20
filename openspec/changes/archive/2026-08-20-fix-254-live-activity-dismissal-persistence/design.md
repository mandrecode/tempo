## Context

`fix-74-notification-recovery` added `ActiveLiveActivityPreferences`, a `SharedPreferences`-backed `Set<Long>` of chain IDs that currently have a live-activity notification. `HabitChainLiveActivityManager` adds an ID when it posts an in-progress live activity and removes it when the chain completes or the app cancels the notification. `RescheduleRemindersWorker.resyncActiveLiveActivities()` then walks that set and calls `HabitRepository.refreshHabitChainLiveActivity(chainId)` for every entry.

Two gaps make that set drift away from reality:

1. **No dismissal signal.** The notification carries no delete intent, so a user swipe removes the notification from the shade but leaves the ID persisted forever. Ongoing notifications have been user-dismissible since Android 14, and Android 16 promoted-ongoing notifications likewise, so this is the common case rather than an edge case.
2. **No date.** `refreshHabitChainLiveActivity(chainId)` defaults to today, so a record left over from an earlier day is rebuilt against today's completion state.

`MainActivity` enqueues `ReminderRefreshScheduler.enqueueImmediateRefresh` on every app open, so both gaps surface as "the discarded live notification comes back every time I open the app" (#254).

Constraints: the reboot / app-update / force-stop recovery from #74 must keep working; the app cannot distinguish "notification gone because the user removed it" from "notification gone because the process died" by inspecting the shade, because both look identical to `NotificationManager`.

## Goals / Non-Goals

**Goals:**
- A dismissed live activity stays dismissed across app opens and reschedule runs.
- A live activity belonging to a previous day is cleared rather than resurrected against today.
- Records already stuck from the #254 regression self-heal without user action.
- Reminder and live-activity recovery after reboot, app update, and force-stop is unchanged.

**Non-Goals:**
- Reverting #74.
- Changing live-activity content, actions, `ProgressStyle` rendering, or when a live activity is first created.
- Touching task / habit reminder notifications or the 24h periodic refresh.

## Decisions

### Observe dismissal with a notification delete intent

Attach `setDeleteIntent(...)` to the live-activity notification, targeting a new `DismissLiveActivityReceiver` that calls `HabitChainLiveActivityManager.dismissLiveActivity(chainId)` — which already clears both the in-memory set and the persisted record.

This is the only mechanism Android provides that fires on user removal and *not* on reboot, force-stop, or app-initiated `cancel()`, which is exactly the distinction #74's recovery needs and currently lacks. Because it is a manifest-registered `@AndroidEntryPoint` receiver, dismissal is recorded even when the app process is dead — the same pattern the existing `StartHabitChainReceiver` and `CompleteHabitReceiver` already use.

*Alternative considered — compare against `NotificationManager.getActiveNotifications()` on resync:* rejected. It cannot tell user dismissal from process death, and after a reboot the notification is legitimately absent, so this would break #74's core scenario.

*Alternative considered — revert #74 (the issue's fallback suggestion):* rejected. It restores predictability by reintroducing the original bug, where reminders and live activities silently vanish after an update or reboot.

The delete intent needs a request code that cannot collide with the existing content `PendingIntent` for the same chain, so `RequestCodeGenerator` gains a `forLiveActivityDismiss` range (`4,000,000+`) alongside the existing task / habit / chain / live-activity ranges. `getBroadcast` vs `getActivity` would already keep them distinct, but a dedicated range keeps the partitioning scheme explicit and self-documenting.

### Date-stamp the persisted record

`ActiveLiveActivityPreferences` changes from `Set<Long>` to a chain ID → `LocalDate` mapping, persisted as a `StringSet` of `"<chainId>|<iso-date>"` entries in the existing `active_live_activity_prefs` file. `HabitChainLiveActivityManager.updateLiveActivity` already receives `scheduledDate: LocalDate?` and holds a `Clock`, so it writes `scheduledDate ?: today`.

`RescheduleRemindersWorker.resyncActiveLiveActivities()` then splits on the date: rebuild when it equals today, otherwise dismiss (clearing record and any lingering notification). This closes the "past live notification" half of the report even for users who never dismiss anything.

Storing the date in the same key, in the same prefs file, means entries written by the current build parse to `null` and are dropped on first read — so users already stuck in the #254 loop are cleaned up by the upgrade itself, with no separate migration step. `getActiveChainIds()` is kept as a thin derived accessor so `HabitChainLiveActivityManager`'s in-memory `activeChains` set and `hasActiveLiveActivity` keep working unchanged.

*Alternative considered — a second `SharedPreferences` key holding dates:* rejected. Two keys can disagree, and a combined entry makes the record atomically consistent by construction.

### Keep the every-open immediate refresh

The immediate refresh also re-arms alarms after a force-stop, which is independently valuable, and once records are dismissal-aware and date-scoped a redundant refresh is a no-op re-post of an identical notification. Gating the resync on "first run in this process" would leave the underlying stale-record problem in place and only reduce how often it is visible.

## Risks / Trade-offs

- **Users who dismissed a live activity on the current build lose their record on upgrade rather than keeping it** → intended: dropping undated entries is what self-heals #254, and a genuinely in-progress chain re-posts its live activity on the next habit toggle or chain reminder.
- **A device whose OS suppresses delete intents would keep the stale-record behaviour within a day** → the date scoping still bounds the damage to the same calendar day instead of forever.
- **Dismissal fires an extra broadcast and a short process start when the app is not running** → negligible; the receiver only writes one `SharedPreferences` entry and cancels an already-absent notification.
- **Timezone/day-rollover: a chain live activity started before midnight is cleared shortly after midnight** → matches the existing rule that live activities are only relevant for today's progress (`refreshHabitChainLiveActivity` already returns early for non-today dates), so this makes recovery consistent with the in-app path rather than diverging from it.
