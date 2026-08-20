## Why

The quick-add-task home-screen widget currently mirrors the app's in-app "use Tempo colors" theme preference, so a user who prefers Tempo's static brand colors inside the app also gets static colors on their home-screen widget, even on devices where Android 12+ dynamic (wallpaper-based) color is available. Issue #231 reports this as undesirable: home-screen widgets are expected to blend with the rest of the launcher/OS theme, and users shouldn't have to separately manage widget theming through an app-internal setting. The widget should always adopt OS dynamic color when the device supports it, independent of the app's own color preference.

## What Changes

- **BREAKING** (behavior change, no data/API break): The widget's `shouldUseTempoStaticColors` logic no longer reads the app's "use Tempo colors" preference. It now returns `true` only when the device does not support Android 12+ dynamic color (`!dynamicColorSupported`), regardless of the in-app preference.
- `QuickAddTaskWidget.kt` stops reading `themePreferencesRepository.getUseTempoColors()` and stops threading that preference into `QuickAddTaskWidgetContent`.
- `WidgetThemePreferenceTest.kt` is updated to reflect that the preference argument is dropped/ignored.

## Capabilities

### New Capabilities
(none)

### Modified Capabilities
- `quick-task-widget`: The "Widget appearance matches the app's theme preference" requirement is replaced — the widget now always follows OS dynamic color when supported, and only falls back to Tempo's static color scheme when the device doesn't support dynamic color, independent of the app's "use Tempo colors" setting.

## Impact

- `app/src/main/java/com/mandrecode/tempo/features/widget/presentation/WidgetThemePreference.kt` — simplify `shouldUseTempoStaticColors`.
- `app/src/main/java/com/mandrecode/tempo/features/widget/presentation/QuickAddTaskWidget.kt` — stop reading/threading the app preference.
- `app/src/test/java/com/mandrecode/tempo/features/widget/presentation/WidgetThemePreferenceTest.kt` — update coverage.
- No Room schema, migration, or public API changes. No other screens read `shouldUseTempoStaticColors`.

Closes #231
