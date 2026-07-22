package com.mandrecode.tempo.features.widget.presentation

import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import androidx.glance.unit.ColorProvider
import com.mandrecode.tempo.R

// Built from real day/night-qualified Android color resources (res/values/colors.xml,
// res/values-night/colors.xml) rather than resolved Compose Color values. A GlanceTheme built
// from resolved colors bakes in whichever value was current at composition time and only updates
// on the next explicit GlanceAppWidget re-render; resource-based ColorProviders instead let the
// OS's own resource-configuration system switch light/dark automatically, the same way any other
// Android resource does. See design.md decision 4.
//
// Only primary/onPrimary/background/onBackground are actually rendered by this minimal widget
// (features/widget/presentation/QuickAddTaskWidget.kt); colorProviders() requires a value for
// every M3 role, so the unused ones are mapped onto the closest of those four.
private val primary = ColorProvider(R.color.tempo_glance_primary)
private val onPrimary = ColorProvider(R.color.tempo_glance_on_primary)
private val background = ColorProvider(R.color.tempo_glance_background)
private val onBackground = ColorProvider(R.color.tempo_glance_on_background)
private val surface = ColorProvider(R.color.tempo_glance_surface)
private val onSurface = ColorProvider(R.color.tempo_glance_on_surface)

val TempoGlanceColorScheme: ColorProviders =
    colorProviders(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary,
        onPrimaryContainer = onPrimary,
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = primary,
        onSecondaryContainer = onPrimary,
        tertiary = primary,
        onTertiary = onPrimary,
        tertiaryContainer = primary,
        onTertiaryContainer = onPrimary,
        error = surface,
        errorContainer = surface,
        onError = onSurface,
        onErrorContainer = onSurface,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surface,
        onSurfaceVariant = onSurface,
        outline = onSurface,
        inverseOnSurface = background,
        inverseSurface = onBackground,
        inversePrimary = primary,
    )
