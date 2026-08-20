## 1. Domain / Presentation Model

- [x] 1.1 Create `WhatsNewEntry` data class (`versionCode: Int`, `versionName: String`, `@StringRes titleRes: Int`, `@StringRes descriptionRes: Int`) under `features/whatsnew/presentation/model/WhatsNewEntry.kt`
- [x] 1.2 Create `WhatsNewRegistry` object under `features/whatsnew/presentation/WhatsNewRegistry.kt` holding a single `val latest: WhatsNewEntry`, set to this "what's new" onboarding feature itself (#210, `v1.2.0`) — the self-demoing entry, so the mechanism is verified end-to-end on release. Initially built as an append-only `List<WhatsNewEntry>` seeded with a second historical entry (Import/Export, #26), but simplified to a single value after review since only the latest entry is ever shown and the list only guaranteed permanent, unused growth

## 2. Data Layer (SharedPreferences repository)

- [x] 2.1 Define `WhatsNewPreferencesRepository` interface (`lastSeenVersionCode: StateFlow<Int>`, `fun setLastSeenVersionCode(versionCode: Int)`) in `core/data/preferences/WhatsNewPreferencesRepository.kt`
- [x] 2.2 Implement `WhatsNewPreferencesRepositoryImpl` in `core/data/preferences/WhatsNewPreferencesRepositoryImpl.kt` using `SharedPreferences` (`whats_new_preferences`, key `last_seen_version_code`, default `0`), mirroring `OnboardingPreferencesRepositoryImpl`'s synchronous-commit pattern
- [x] 2.3 Bind the new repository with `@Binds` in `core/di/RepositoryModule.kt` (moved to a new `PreferencesRepositoryModule.kt` to keep `RepositoryModule` under detekt's function-count ceiling; see design note below)
- [x] 2.4 Unit test `WhatsNewPreferencesRepositoryImpl` (default value, persistence across instances, update behavior)

## 3. Trigger Wiring

- [x] 3.1 Add `whatsNewEntry: WhatsNewEntry?` derivation to `MainUiState`/`MainViewModel`, combining `OnboardingPreferencesRepository.isCompleted`, `WhatsNewPreferencesRepository.lastSeenVersionCode`, and `WhatsNewRegistry.latest.versionCode`. Onboarding-replay guard implemented separately as `isOnboardingSectionActive` in `MainActivity`, fed by a new `onOnboardingActiveChange` callback threaded through `TempoNavHost`/`Navigation.kt`. Initially keyed off `TempoNavigator.section`, but a Settings-triggered replay (`navigator.navigate(OnboardingRoute(isReplay = true))`) pushes onto whichever back stack is already active without changing `section` — Copilot review caught this, so the signal now keys off `navigator.currentRoute is OnboardingRoute` instead, verified correct on-device (replay screen renders with no sheet on top)
- [x] 3.2 Add `onWhatsNewDismissed()` handler on `MainViewModel` that calls `whatsNewPreferencesRepository.setLastSeenVersionCode(WhatsNewRegistry.latest.versionCode)`
- [x] 3.3 Unit test `MainViewModel`'s entry-derivation logic for: not-yet-seen entry, already-seen entry, onboarding incomplete, and `onWhatsNewDismissed()` persistence

## 4. UI

- [x] 4.1 No dedicated `WhatsNewContract`/ViewModel — folded into `MainUiState`/`MainViewModel` per the thin-feature design decision
- [x] 4.2 Create `WhatsNewBottomSheet.kt` (Content composable) under `features/whatsnew/presentation/components/` using `TempoModalBottomSheet`, rendering the "New features in vX.Y.Z: <title>" legend + description + dismiss action. The "Got it" button uses the app's shared `rememberPressableButtonAnimation` squircle-press shape animation plus haptic feedback (matching `TempoConfirmDialog`/onboarding's primary CTA), not a stock Material3 `Button`
- [x] 4.3 Wire `WhatsNewBottomSheet` into `MainActivity.kt`'s composable tree, shown when `state.whatsNewEntry != null && !isOnboardingSectionActive`, calling `onWhatsNewDismissed()` on dismissal
- [x] 4.4 Add `@Preview` composables for `WhatsNewBottomSheet` under `src/debug/`
- [x] 4.5 Add `WhatsNewBottomSheetTest.kt` (androidTest) covering legend/description rendering and "Got it" dismissal, mirroring `HabitBottomSheetTest`/`TaskBottomSheetTest` conventions; verified passing on the `Pixel_10` AVD

## 5. Strings

- [x] 5.1 Add `whats_new_*` string resources to `app/src/main/res/values/strings.xml` (sheet legend template, dismiss button, and this feature's title/description); removed the Import/Export entry's strings once the registry moved to a single-entry model, since they became permanently unused
- [x] 5.2 Add matching translations to `app/src/main/res/values-es/strings.xml`

## 6. Documentation

- [x] 6.1 Add a line to `AGENTS.md`'s "New Feature Checklist" instructing authors to replace `WhatsNewRegistry.latest` (and delete the previous entry's now-unused strings) when shipping a new feature

## 7. Verification

- [x] 7.1 Run `./gradlew ktlintFormat` then `./gradlew ktlintCheck`
- [x] 7.2 Run `./gradlew :app:detekt` (required splitting `RepositoryModule` to stay under the `TooManyFunctions` threshold; baseline net-unchanged at 188 entries)
- [x] 7.3 Run `./gradlew testDebugUnitTest`
- [x] 7.4 Run `./gradlew lintDebug` (validates locale string parity)
- [x] 7.5 Manually verified on the `Pixel_10` AVD with a genuinely fresh install (`pm clear`): the sheet is correctly suppressed during onboarding, appears once immediately after skipping/completing onboarding with the exact legend/description/button copy, dismisses on tap, does not reappear after force-stop + relaunch, the "Got it" button visibly morphs its corner radius under a long-press (squircle shape animation, matching `TempoConfirmDialog`/onboarding's primary CTA), and triggering onboarding replay from Settings ("View onboarding") renders cleanly with no sheet on top.
- [x] 7.6 Run `openspec validate feat-210-whats-new-onboarding --strict`
