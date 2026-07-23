package com.mandrecode.tempo.features.widget.presentation

// Unlike TempoTheme's own light/dark-vs-dynamic-vs-Tempo decision (core/ui/theme/Theme.kt), the
// widget always follows OS dynamic color when the device supports it, ignoring the app's own
// "use Tempo colors" preference — widgets are host-surface UI expected to blend with the
// launcher/wallpaper theme. Tempo's brand colors are only a fallback for devices/OS versions that
// don't support dynamic color, so the widget is never left with an unbranded default.
// Kept in its own file with no other top-level declarations: TempoGlanceColorScheme.kt's top-level
// `val` eagerly constructs real Glance/Compose objects at class-init time, which isn't safe to
// trigger from a plain JVM unit test (no Android framework/Robolectric).
internal fun shouldUseTempoStaticColors(dynamicColorSupported: Boolean): Boolean = !dynamicColorSupported
