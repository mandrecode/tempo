## Why

`WhatsNewRegistry.latest` requires the author to hand-guess the semantic version a feature will ship in (based on commit type: `feat` = minor, `fix` = patch, `BREAKING CHANGE` = major) before the release actually happens. That guess is wrong as often as not — it is already stale today (registry says `1.3.0` while `version.txt` says `1.4.0`), and a prior fix (`fix(#210)`) had to correct the same class of mistake after v1.1.0 shipped. The real, correct version is already known automatically at build time via `AppVersionProvider` (backed by `BuildConfig.VERSION_NAME`/`VERSION_CODE`, itself derived from `version.txt`), so the manual guess is unnecessary and error-prone. Closes #220.

## What Changes

- Remove the manually-authored `versionCode`/`versionName` fields from `WhatsNewEntry` — authors no longer predict a future release version.
- Add a stable `id: String` field to `WhatsNewEntry`, chosen once by the author (e.g. a feature slug), used only to identify "has the user seen this entry" — not for ordering or release-version prediction.
- Change the "seen" persistence (`WhatsNewPreferencesRepository`) from an `Int` version code compared by ordering to a nullable entry `id` compared by equality.
- Source the version number shown in the "what's new" bottom sheet ("New in vX.Y.Z: ...") from the real, DI-provided `AppVersionProvider` instead of the hardcoded registry field, so the displayed version is automatically correct for whatever build is actually running.
- Update `WhatsNewRegistry`'s authoring doc comment and `AGENTS.md`'s New Feature Checklist to remove the "recheck the guessed version against `version.txt` before merge" instruction.
- **BREAKING**: `WhatsNewEntry.versionCode`/`versionName` are removed; `WhatsNewPreferencesRepository`'s `lastSeenVersionCode: StateFlow<Int>` / `setLastSeenVersionCode(Int)` API is replaced with `lastSeenEntryId: StateFlow<String?>` / `setLastSeenEntryId(String)`. Internal-only API surface (no external consumers), and existing persisted `lastSeenVersionCode` prefs values become irrelevant under the new key — the "what's new" sheet will show once more for existing users after this ships, which is an acceptable one-time reset.

## Capabilities

### Modified Capabilities
- `whats-new-onboarding`: the "seen" gating and dismissal-persistence requirements change from version-code ordering to entry-id equality; the display-format requirement changes from a registry-authored version string to the real app version at runtime; the registry-authoring requirement drops the version fields.

## Impact

- `app/src/main/java/com/mandrecode/tempo/features/whatsnew/presentation/model/WhatsNewEntry.kt` — field changes.
- `app/src/main/java/com/mandrecode/tempo/features/whatsnew/presentation/WhatsNewRegistry.kt` — entry construction + doc comment.
- `app/src/main/java/com/mandrecode/tempo/core/data/preferences/WhatsNewPreferencesRepository.kt` and `WhatsNewPreferencesRepositoryImpl.kt` — API and persisted key change.
- `app/src/main/java/com/mandrecode/tempo/core/ui/MainViewModel.kt` and `app/src/main/java/com/mandrecode/tempo/core/ui/model/MainUiState.kt` — gating logic, inject `AppVersionProvider`, expose real version name in UI state.
- `app/src/main/java/com/mandrecode/tempo/MainActivity.kt` and `app/src/main/java/com/mandrecode/tempo/features/whatsnew/presentation/components/WhatsNewBottomSheet.kt` — pass/display real version name instead of `entry.versionName`.
- `app/src/debug/java/com/mandrecode/tempo/features/whatsnew/presentation/components/WhatsNewBottomSheetPreviews.kt`, `app/src/androidTest/.../WhatsNewBottomSheetTest.kt`, `app/src/test/java/com/mandrecode/tempo/core/ui/MainViewModelTest.kt`, `WhatsNewPreferencesRepositoryTest.kt` — test updates.
- `AGENTS.md` New Feature Checklist item 5.
