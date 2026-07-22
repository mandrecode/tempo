## 1. Implementation

- [x] 1.1 In `app/src/main/java/com/mandrecode/tempo/core/ui/components/TempoLoadingIndicator.kt`, replace the `CircularProgressIndicator` with Material 3 Expressive's indeterminate `LoadingIndicator(modifier, color)`, sized at `96.dp` with `MaterialTheme.colorScheme.primary` color, and drop the visible message `Text`.
- [x] 1.2 Add `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` on `TempoLoadingIndicator`, matching the existing pattern in `SettingsScreen.kt`.
- [x] 1.3 Remove the now-unused `CircularProgressIndicator` import; add the `LoadingIndicator`/`ExperimentalMaterial3ExpressiveApi` imports.

## 2. Verification

- [x] 2.1 Update `app/src/debug/java/com/mandrecode/tempo/core/ui/components/TempoLoadingIndicatorPreviews.kt` if it references the old spinner visuals, and confirm the preview still renders.
- [x] 2.2 Manually verify the Tasks and Routines (habits) first-load states in the app/emulator show the morphing Expressive shape.
- [x] 2.3 Run `./gradlew ktlintFormat` then `./gradlew ktlintCheck`.
- [x] 2.4 Run `./gradlew :app:detekt`.
- [x] 2.5 Run `./gradlew testDebugUnitTest`.
- [x] 2.6 Run `openspec validate feat-79-expressive-loading-shapes --strict`.
- [x] 2.7 Update `TasksContentTest.showsLoadingIndicator_whenLoading` and `RoutinesContentTest.showsLoadingIndicator_whenLoading` (instrumented tests) to assert via `onNodeWithContentDescription` instead of `onNodeWithText`, since the message is no longer visible text.
- [x] 2.8 Run `./gradlew connectedDebugAndroidTest` (or CI's Instrumented Tests job) to confirm the updated assertions pass.
