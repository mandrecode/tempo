## 1. Implementation

- [x] 1.1 Simplify `shouldUseTempoStaticColors` in `WidgetThemePreference.kt` to `!dynamicColorSupported`, dropping the `useTempoColorsPreference` parameter.
- [x] 1.2 Update `QuickAddTaskWidget.kt`'s `provideGlance`/`QuickAddTaskWidgetContent` to stop reading `themePreferencesRepository.getUseTempoColors()` and stop threading it into `shouldUseTempoStaticColors`.
- [x] 1.3 Update `WidgetThemePreferenceTest.kt` call sites and add/adjust test cases to cover the simplified function (dynamic-supported → false, dynamic-unsupported → true, regardless of any former preference input).

## 2. Verification

- [x] 2.1 Run `./gradlew ktlintFormat` and `./gradlew :app:detekt`.
- [x] 2.2 Run `./gradlew testDebugUnitTest` and confirm `WidgetThemePreferenceTest` passes.
- [x] 2.3 Run `./gradlew compileDebugKotlin` (or `assembleDebug`) to confirm the widget module still compiles cleanly after removing the preference read.
- [x] 2.4 Run `openspec validate fix-231-widget-dynamic-theme` before considering the change ready to implement.
