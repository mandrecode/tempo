package com.mandrecode.tempo.features.widget.presentation

import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import com.mandrecode.tempo.core.ui.theme.TempoDarkBackground
import com.mandrecode.tempo.core.ui.theme.TempoDarkOnBackground
import com.mandrecode.tempo.core.ui.theme.TempoDarkOnPrimary
import com.mandrecode.tempo.core.ui.theme.TempoDarkOnSurface
import com.mandrecode.tempo.core.ui.theme.TempoDarkPrimary
import com.mandrecode.tempo.core.ui.theme.TempoDarkSurface
import com.mandrecode.tempo.core.ui.theme.TempoLightBackground
import com.mandrecode.tempo.core.ui.theme.TempoLightOnBackground
import com.mandrecode.tempo.core.ui.theme.TempoLightOnPrimary
import com.mandrecode.tempo.core.ui.theme.TempoLightOnSurface
import com.mandrecode.tempo.core.ui.theme.TempoLightPrimary
import com.mandrecode.tempo.core.ui.theme.TempoLightSurface

// Built from androidx.glance.color.ColorProvider(day, night), which checks the live
// Configuration.uiMode on every getColor(context) call, rather than a resolved Compose Color
// baked in once at composition time. A GlanceTheme built from a pre-resolved ColorScheme only
// updates on the next explicit GlanceAppWidget re-render; this instead lets the widget follow a
// system light/dark change immediately, the same as day/night-qualified Android resources would
// — but without needing resource indirection, since Glance's own ColorProvider(day:, night:) does
// the day/night check itself. See design.md decision 4.
// (androidx.glance.unit.ColorProvider(@ColorRes id) — the resource-based overload — was
// considered too, but it's marked @RestrictedApi to androidx.glance itself and trips lint.)
//
// Only primary/onPrimary/background/onBackground are actually rendered by this minimal widget
// (features/widget/presentation/QuickAddTaskWidget.kt); colorProviders() requires a value for
// every M3 role, so the unused ones are mapped onto the closest of those four.
private val primary = ColorProvider(day = TempoLightPrimary, night = TempoDarkPrimary)
private val onPrimary = ColorProvider(day = TempoLightOnPrimary, night = TempoDarkOnPrimary)
private val background = ColorProvider(day = TempoLightBackground, night = TempoDarkBackground)
private val onBackground = ColorProvider(day = TempoLightOnBackground, night = TempoDarkOnBackground)
private val surface = ColorProvider(day = TempoLightSurface, night = TempoDarkSurface)
private val onSurface = ColorProvider(day = TempoLightOnSurface, night = TempoDarkOnSurface)

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
