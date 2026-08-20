## 1. Domain model

- [x] 1.1 In `app/src/main/java/com/mandrecode/tempo/features/whatsnew/presentation/model/WhatsNewEntry.kt`, replace `versionCode: Int` / `versionName: String` with `id: String`.

## 2. Registry

- [x] 2.1 In `app/src/main/java/com/mandrecode/tempo/features/whatsnew/presentation/WhatsNewRegistry.kt`, replace the `versionCode`/`versionName` construction with an `id` value for the current entry, and rewrite the doc comment to describe assigning a stable `id` instead of guessing/rechecking a release version.

## 3. Preferences

- [x] 3.1 In `app/src/main/java/com/mandrecode/tempo/core/data/preferences/WhatsNewPreferencesRepository.kt`, change `lastSeenVersionCode: StateFlow<Int>` / `setLastSeenVersionCode(versionCode: Int)` to `lastSeenEntryId: StateFlow<String?>` / `setLastSeenEntryId(id: String)`.
- [x] 3.2 In `WhatsNewPreferencesRepositoryImpl.kt`, switch the backing `SharedPreferences` key from an int (`last_seen_version_code`) to a string (`last_seen_entry_id`), default `null`, and change the idempotency guard from `>=` ordering to `==` equality.
- [x] 3.3 Update `app/src/test/java/com/mandrecode/tempo/core/data/preferences/WhatsNewPreferencesRepositoryTest.kt` for the new string-id API and key.

## 4. MainViewModel + UI state

- [x] 4.1 Inject `AppVersionProvider` into `MainViewModel` (`app/src/main/java/com/mandrecode/tempo/core/ui/MainViewModel.kt`).
- [x] 4.2 Change the gating condition from `it.versionCode > lastSeenVersionCode` to `it.id != lastSeenEntryId`, and update `onWhatsNewDismissed()` to call `setLastSeenEntryId(WhatsNewRegistry.latest.id)`.
- [x] 4.3 Add a `whatsNewVersionName: String` (or equivalent) field to `MainUiState.Success` (`app/src/main/java/com/mandrecode/tempo/core/ui/model/MainUiState.kt`) populated from `AppVersionProvider.getVersionInfo().versionName`.
- [x] 4.4 Update `app/src/test/java/com/mandrecode/tempo/core/ui/MainViewModelTest.kt`: mock `AppVersionProvider`, replace `lastSeenVersionCode`/`WhatsNewRegistry.latest.versionCode` references with `lastSeenEntryId`/`WhatsNewRegistry.latest.id`, and assert the new version-name field.

## 5. UI call sites

- [x] 5.1 Update `WhatsNewBottomSheet` (`app/src/main/java/com/mandrecode/tempo/features/whatsnew/presentation/components/WhatsNewBottomSheet.kt`) to take a `versionName: String` parameter and use it in place of `entry.versionName` in the legend text.
- [x] 5.2 Update the call site in `app/src/main/java/com/mandrecode/tempo/MainActivity.kt` to pass the new version-name value from `MainUiState.Success`.
- [x] 5.3 Update `app/src/debug/java/com/mandrecode/tempo/features/whatsnew/presentation/components/WhatsNewBottomSheetPreviews.kt` to pass a literal preview version string.
- [x] 5.4 Update `app/src/androidTest/java/com/mandrecode/tempo/features/whatsnew/presentation/components/WhatsNewBottomSheetTest.kt` to pass an explicit version string and assert against it instead of `entry.versionName`.

## 6. Docs

- [x] 6.1 Update `AGENTS.md`'s New Feature Checklist item 5 ("What's New") to drop the "recheck versionCode/versionName against version.txt before merging" instruction and describe assigning a stable `id` instead.

## 7. Verification

- [x] 7.1 Run `./gradlew ktlintFormat` then `./gradlew ktlintCheck`.
- [x] 7.2 Run `./gradlew testDebugUnitTest`.
- [x] 7.3 Run `./gradlew :app:detekt`.
- [x] 7.4 Run `openspec validate fix-220-whats-new-version-linking --strict` (or the project's equivalent validation command) and confirm it passes.
