## Context

`WidgetThemePreference.kt` exposes a pure function `shouldUseTempoStaticColors(useTempoColorsPreference: Boolean, dynamicColorSupported: Boolean): Boolean`, currently `useTempoColorsPreference || !dynamicColorSupported`. `QuickAddTaskWidget.kt`'s `provideGlance` reads `themePreferencesRepository.getUseTempoColors()` from `ThemePreferencesRepository` (the same app-level preference used by in-app theming) and passes it into `QuickAddTaskWidgetContent`, which calls `shouldUseTempoStaticColors` to decide between `TempoGlanceColorScheme` and Glance's dynamic `GlanceTheme` colors.

## Goals / Non-Goals

**Goals:**
- Widget always uses OS dynamic color on Android 12+ devices that support it, regardless of the app's own "use Tempo colors" preference.
- Widget still falls back to `TempoGlanceColorScheme` on devices/OS versions without dynamic color support, so it's never left with an undefined/default look.

**Non-Goals:**
- Not changing the app's own in-app "use Tempo colors" preference or its Settings UI — that preference continues to control in-app theming exactly as before; only the widget stops consulting it.
- Not adding a separate, widget-specific theme preference/setting.

## Decisions

- **Reduce `shouldUseTempoStaticColors` to `!dynamicColorSupported`, dropping the `useTempoColorsPreference` parameter entirely** rather than keeping the parameter and ignoring it at call sites. A function that takes a parameter it doesn't use is a worse contract than one that doesn't take it, and keeping it would let a future edit accidentally reintroduce the dependency on the app preference.
  - *Alternative considered:* keep the parameter but always pass `false` from the widget. Rejected — it leaves a dead, easily-misused parameter in the function signature instead of removing the coupling at the source.
- **Stop reading `themePreferencesRepository.getUseTempoColors()` in `QuickAddTaskWidget.kt` entirely**, rather than reading it and discarding it. Removing the read also removes the (now pointless) `ThemePreferencesRepository` dependency from the widget's color-resolution path if it isn't needed elsewhere in that file.
- **No new widget-level setting.** The issue is explicit that the widget should just always follow the OS, not gain its own independent preference — that would reintroduce the same complexity issue #231 is removing.

## Risks / Trade-offs

- [Users who intentionally chose "use Tempo colors" to get consistent branding may be surprised the widget looks different from the app] → Acceptable per issue #231's explicit ask; widgets are host-surface UI that users generally expect to blend with their launcher/wallpaper theme rather than carry in-app branding.
- [Existing widget instances already placed on a home screen] → Glance widgets re-render on next update/preference change automatically; no migration step needed since this is a pure color-resolution logic change, not a data/schema change.
