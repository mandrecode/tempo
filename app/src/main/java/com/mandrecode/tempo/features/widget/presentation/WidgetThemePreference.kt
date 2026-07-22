package com.mandrecode.tempo.features.widget.presentation

// Mirrors TempoTheme's own light/dark-vs-dynamic-vs-Tempo decision (core/ui/theme/Theme.kt), minus
// the legacy "neither Tempo nor dynamic" branch: a widget always prefers Tempo's brand colors over
// unbranded Material defaults, so the fallback here is Tempo rather than plain M3 lightColorScheme().
// Kept in its own file with no other top-level declarations: TempoGlanceColorScheme.kt's top-level
// `val` eagerly constructs real Glance/Compose objects at class-init time, which isn't safe to
// trigger from a plain JVM unit test (no Android framework/Robolectric).
internal fun shouldUseTempoStaticColors(
    useTempoColorsPreference: Boolean,
    dynamicColorSupported: Boolean,
): Boolean = useTempoColorsPreference || !dynamicColorSupported
