## Context

Reminder delivery already has one convergence point for re-deriving `AlarmManager` schedules from Room state: `RescheduleRemindersWorker` (`infrastructure/reminders/workers/RescheduleRemindersWorker.kt`). It is triggered from two places today:

1. `BootAndTimeReceiver` — a `BroadcastReceiver` for `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED` — enqueues a one-off run.
2. `ReminderRefreshScheduler.enqueuePeriodicRefresh()` — enqueues a 24h `PeriodicWorkRequest` (`ExistingPeriodicWorkPolicy.KEEP`) from `MainActivity`'s post-first-frame `LaunchedEffect`, per `startup-performance`'s "Deferred Maintenance Startup Work" constraint (`Application.onCreate()` must not enqueue this work).

Two recovery gaps exist against this baseline (see proposal): no handler for `ACTION_MY_PACKAGE_REPLACED` (app update), and no immediate re-arm on app reopen after a force-stop, where alarms and WorkManager jobs were cleared and `BOOT_COMPLETED` cannot fire (the OS withholds implicit broadcasts from stopped apps until the user explicitly launches them — which is exactly the moment `MainActivity` starts).

Separately, `HabitChainLiveActivityManager.activeChains` (`infrastructure/liveactivity/HabitChainLiveActivityManager.kt:41-43`) is an in-memory `ConcurrentHashMap`-backed set with no persistence, so a chain's ongoing live-activity notification is never recreated after process death even though `HabitRepositoryImpl.refreshHabitChainLiveActivity()` (`features/routines/data/repository/HabitRepositoryImpl.kt:208-237`) already knows how to fully rebuild a chain's live-activity notification (progress, current habit, content) purely from Room state given just a chain ID.

## Goals / Non-Goals

**Goals:**
- Reminders (tasks, habits, chains) automatically resume firing after force-close-then-reopen, app update, and reboot, without the user having to wait up to 24h or manually re-touch the reminder.
- A habit chain's live-activity notification is recreated/resynced after these same three events if it was active when the app stopped.
- Reuse the existing `RescheduleRemindersWorker` convergence point rather than adding a parallel recovery path.
- Stay consistent with `startup-performance`'s constraint that maintenance work is deferred past the first Compose frame, not run in `Application.onCreate()`.

**Non-Goals:**
- Changing the exact-alarm permission model (`USE_EXACT_ALARM` vs `SCHEDULE_EXACT_ALARM`) — out of scope, previously deferred in `fix-751-harden-infra-reminders`.
- Persisting full live-activity payload (completed count, current habit) — this is recomputed from Room via the existing `refreshHabitChainLiveActivity` path; only the chain-is-active identity needs persisting.
- Changing notification permission education/prompting behavior.
- Reducing the 24h periodic refresh interval — it remains a background safety net; this change adds an immediate path on top of it.

## Decisions

**1. Extend `BootAndTimeReceiver`'s existing intent-filter with `ACTION_MY_PACKAGE_REPLACED` rather than adding a new receiver.**
The receiver already enqueues `RescheduleRemindersWorker` unconditionally for any of its handled actions — the same reschedule-everything behavior is exactly right for an app update. `MY_PACKAGE_REPLACED` is delivered directly to the updated package (not a stopped-app-restricted broadcast), so no manifest permission is needed. Alternative considered: a dedicated `PackageUpdateReceiver` — rejected as needless duplication for identical handling logic.

**2. Add an immediate one-off `RescheduleRemindersWorker` enqueue in `MainActivity`'s existing post-first-frame `LaunchedEffect`, alongside the periodic refresh enqueue.**
This is the only reliable moment to recover from a force-stop: the OS will not deliver `BOOT_COMPLETED` (or run previously-scheduled WorkManager jobs) to a stopped app, but explicitly launching the app via the launcher clears the stopped state and runs this code. Use `enqueueUniqueWork("ImmediateRescheduleRemindersWorker", ExistingWorkPolicy.KEEP, ...)` — `KEEP` avoids redundant duplicate runs if the app is foregrounded multiple times in quick succession (e.g. rotation, multi-window) while a previous run is still in flight. Alternative considered: shortening the periodic interval — rejected, doesn't help a user who reopens shortly after force-stop and doesn't fix the app-update gap.

**3. Persist active live-activity chain IDs via a new SharedPreferences-backed repository, not a Room column.**
Follows the established `core/di/PreferencesRepositoryModule.kt` pattern (e.g. `NavigationPreferencesRepositoryImpl`) used for lightweight, non-relational UI/runtime state. A Room migration is unnecessary complexity for a transient "is this chain's live activity currently on screen" flag that is derived, not canonical, data — the canonical progress data already lives in `habits`/`habit_chains` completion history. New interface `ActiveLiveActivityPreferences` in `core/data/preferences/`, storing a `Set<String>` of chain IDs (`SharedPreferences` string-set), injected into `HabitChainLiveActivityManager`.

**4. `HabitChainLiveActivityManager` loads persisted chain IDs into `activeChains` on construction, and writes through on every add/remove.**
Keeps `hasActiveLiveActivity()` correct immediately after process recreation even before any explicit resync runs (e.g., in-app toggle logic in `HabitRepositoryImpl` that gates past-date sync on `hasActiveLiveActivity()`), and keeps the persisted set from drifting out of sync with the in-memory set.

**5. `RescheduleRemindersWorker` resyncs habit-chain live activities after rescheduling alarms, by calling `HabitRepository`'s existing `refreshHabitChainLiveActivity(chainId, date = null, fromNotification = false)` for each persisted active chain ID.**
This reuses `HabitChainLiveActivityManager.updateLiveActivity()`'s existing self-healing branches unchanged: if the chain's reminder is still in the future with nothing completed, or everything is already completed, the live activity is dismissed as stale (matches `allUncheckedWithFutureReminder` / `allCompletedFromApp` logic at `HabitChainLiveActivityManager.kt:71-85`) — otherwise the ongoing notification is rebuilt with current progress. No new dismissal/rebuild logic is needed; the worker only needs to enumerate the persisted chain IDs and call the existing repository method. Alternative considered: reading `activeChains` directly from the manager inside the worker — rejected because the worker runs (potentially) before `HabitChainLiveActivityManager` has been constructed in the current process; going through the persisted preferences repository is the actual source of truth across process boundaries.

## Risks / Trade-offs

- [Risk] `MY_PACKAGE_REPLACED` fires before the updated APK's Hilt graph is warm in some edge cases → Mitigation: `BootAndTimeReceiver` is `@AndroidEntryPoint`-free (it only touches `WorkManager` directly, no injected dependencies), so it has no Hilt-initialization ordering dependency; `RescheduleRemindersWorker` itself is a `HiltWorker` resolved lazily by `WorkManager` when it actually runs, not at enqueue time.
- [Risk] Enqueuing immediate work on every `MainActivity` launch could run more often than needed (e.g., user backgrounds/foregrounds repeatedly) → Mitigation: `ExistingWorkPolicy.KEEP` collapses concurrent/duplicate requests into the already-running or already-queued one.
- [Risk] Persisted `activeChains` could go stale if the app is uninstalled/reinstalled or preferences are cleared independently of Room (e.g. "clear storage" without "clear data") → Mitigation: existing self-healing dismiss logic in decision 5 means a stale entry just results in one harmless resync call that dismisses/cleans it up; no user-visible incorrect notification persists.
- [Trade-off] This does not guarantee sub-second recovery after force-stop — recovery only happens when the user reopens the app, which is an inherent Android platform constraint (stopped apps cannot self-wake), not something this design can fully close.

## Migration Plan

No data migration. Rollout is a standard app update:
1. New `ActiveLiveActivityPreferences` reads an empty set on first run after update (no prior persisted state) — equivalent to today's behavior (in-memory only) until a chain's live activity is next started, at which point it begins persisting.
2. `MY_PACKAGE_REPLACED` handling and the immediate reschedule enqueue are additive; no rollback risk beyond reverting the change.
