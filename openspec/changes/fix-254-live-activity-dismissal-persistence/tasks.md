## 1. Date-scoped persistence

- [x] 1.1 Change `ActiveLiveActivityPreferences` to expose the active records as a chain ID → `LocalDate` map, keep a derived `getActiveChainIds()`, and replace `addActiveChainId` with a date-taking `setActiveChain(chainId, date)`
- [x] 1.2 Persist records in `ActiveLiveActivityPreferencesImpl` as `"<chainId>|<iso-date>"` entries in the existing `active_live_activity_prefs` key, dropping any entry that does not parse into an ID and a date
- [x] 1.3 Make `setActiveChain` replace an existing record for the same chain instead of adding a second entry

## 2. Dismissal signal

- [x] 2.1 Add a `forLiveActivityDismiss` range (`4,000,000+`) to `RequestCodeGenerator` and document it in the range comment
- [x] 2.2 Add `DismissLiveActivityReceiver` under `infrastructure/reminders/receivers/`, injecting `HabitChainLiveActivityManager` and calling `dismissLiveActivity` for the chain ID extra
- [x] 2.3 Register the receiver in `AndroidManifest.xml` with `android:exported="false"`
- [x] 2.4 Attach the delete `PendingIntent` to the live-activity notification in `HabitChainLiveActivityManager.updateLiveActivity`, on both the API 36+ `ProgressStyle` branch and the fallback branch

## 3. Manager and recovery wiring

- [x] 3.1 Update `HabitChainLiveActivityManager` to record `scheduledDate ?: today` via `setActiveChain` when it posts an in-progress live activity, still writing once per chain-session but re-writing when the date changes
- [x] 3.2 Update `RescheduleRemindersWorker.resyncActiveLiveActivities()` to rebuild only records dated today and to dismiss records for any other date
- [x] 3.3 Verify `dismissLiveActivity` and the completed-chain path still clear the record exactly once

## 4. Tests

- [x] 4.1 Extend `ActiveLiveActivityPreferencesTest`: round-trip with dates, replacement on same chain, removal, and undated legacy entries dropped
- [x] 4.2 Extend `HabitChainLiveActivityManagerTest`: date recorded on post, date replaced when the scheduled date changes, record cleared on dismissal and on completion
- [x] 4.3 Extend `RescheduleRemindersWorkerTest`: today's record is refreshed, a previous-day record is dismissed and not refreshed
- [x] 4.4 Add `DismissLiveActivityReceiverTest` covering `chainIdFrom`: a valid extra resolves to the chain, a missing extra resolves to null. Hilt's generated `onReceive` needs a real `Application`, so the decision is tested through a companion helper, matching `BootAndTimeReceiver.shouldRescheduleReminders`

## 5. Verification

- [x] 5.1 `./gradlew ktlintFormat` then `./gradlew ktlintCheck`
- [x] 5.2 `./gradlew :app:detekt` with no new baseline entries
- [x] 5.3 `./gradlew testDebugUnitTest` and `./gradlew koverVerifyDebug`
- [x] 5.4 `./gradlew assembleDebug`
- [x] 5.5 `openspec validate fix-254-live-activity-dismissal-persistence --strict`
- [x] 5.6 Manual smoke test: start a chain live activity, swipe it away, reopen the app, confirm it does not return; then confirm an undismissed same-day live activity still survives a force-stop and reopen
