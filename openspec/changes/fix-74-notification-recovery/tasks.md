## 1. App-update recovery

- [x] 1.1 Add `android.intent.action.MY_PACKAGE_REPLACED` to the intent-filter of the `BootAndTimeReceiver` declaration in `app/src/main/AndroidManifest.xml`
- [x] 1.2 Handle `Intent.ACTION_MY_PACKAGE_REPLACED` in `BootAndTimeReceiver.onReceive()` alongside the existing boot/time actions, enqueuing `RescheduleRemindersWorker` the same way
- [x] 1.3 Add/update unit test for `BootAndTimeReceiver` covering `ACTION_MY_PACKAGE_REPLACED` (extracted pure `shouldRescheduleReminders(action)` companion function, following the `TaskReminderReceiver.shouldProcessTaskReminder` pattern already used in this codebase, since mocking `WorkManager`'s static `getInstance` in a JVM unit test proved unreliable)

## 2. Immediate reopen recovery

- [x] 2.1 Add a function to `ReminderRefreshScheduler` (or equivalent) that enqueues a one-off `RescheduleRemindersWorker` via `enqueueUniqueWork("ImmediateRescheduleRemindersWorker", ExistingWorkPolicy.KEEP, ...)`
- [x] 2.2 Call the new immediate-enqueue function from `MainActivity`'s existing post-first-frame `LaunchedEffect(Unit)` block, alongside `enqueuePeriodicRefresh()`
- [x] 2.3 Verify no change to `startup-performance` deferred-timing behavior (the call stays inside the existing post-first-frame block, not `Application.onCreate()`)

## 3. Persist active live-activity chain identity

- [x] 3.1 Create `ActiveLiveActivityPreferences` interface under `core/data/preferences/` with `getActiveChainIds(): Set<Long>`, `addActiveChainId(chainId: Long)`, `removeActiveChainId(chainId: Long)`
- [x] 3.2 Implement `ActiveLiveActivityPreferencesImpl` (SharedPreferences-backed string-set), following the `NavigationPreferencesRepositoryImpl` pattern
- [x] 3.3 Bind the new repository in `core/di/PreferencesRepositoryModule.kt`
- [x] 3.4 Inject `ActiveLiveActivityPreferences` into `HabitChainLiveActivityManager`; load persisted IDs into `activeChains` at construction
- [x] 3.5 Write through to `ActiveLiveActivityPreferences` in `updateLiveActivity()` (on add to `activeChains`) and in `dismissLiveActivity()` / the completed-removal branch (on remove from `activeChains`)
- [x] 3.6 Unit test `ActiveLiveActivityPreferencesImpl` and the updated `HabitChainLiveActivityManager` persistence behavior

## 4. Resync live activities during reschedule

- [x] 4.1 Inject `ActiveLiveActivityPreferences` and `HabitRepository` into `RescheduleRemindersWorker` (verify `HabitRepository` is already available or add it)
- [x] 4.2 After `rescheduleHabitChains(now)`, iterate persisted active chain IDs and call `habitRepository.refreshHabitChainLiveActivity(chainId, date = null, fromNotification = false)` for each
- [x] 4.3 Unit test `RescheduleRemindersWorker` verifying it resyncs each persisted active chain and leaves untouched chains alone

## 5. Verification

- [x] 5.1 Run `./gradlew ktlintFormat` and `./gradlew ktlintCheck`
- [x] 5.2 Run `./gradlew :app:detekt`
- [x] 5.3 Run `./gradlew testDebugUnitTest`
- [x] 5.4 Manual smoke test on Pixel 10 AVD: installed debug build, confirmed via `dumpsys jobscheduler` that `RescheduleRemindersWorker` jobs (periodic ~24h + immediate one-off) enqueue and complete successfully through Hilt DI on launch; confirmed `am force-stop` clears the scheduled job entirely, and relaunching the app immediately re-arms it (core fix for the force-close scenario), with no crashes in logcat. Full reboot and app-update-specific device scenarios were not separately re-verified on-device beyond this (they share the same `RescheduleRemindersWorker` convergence point and are covered by unit tests for the receiver/manifest changes).
- [x] 5.5 Live-activity resync verified via unit tests (`RescheduleRemindersWorkerTest`, `HabitChainLiveActivityManagerTest`, `ActiveLiveActivityPreferencesTest`); not separately re-verified with a live on-device chain notification in this pass.
- [x] 5.6 Run `openspec validate fix-74-notification-recovery --strict`
