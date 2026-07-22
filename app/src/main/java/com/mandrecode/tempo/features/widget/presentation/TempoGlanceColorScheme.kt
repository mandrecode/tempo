package com.mandrecode.tempo.features.widget.presentation

import android.content.Context
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.glance.color.ColorProviders
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
import com.mandrecode.tempo.util.dynamicColorScheme
import com.mandrecode.tempo.util.supportsDynamicColor
import androidx.glance.material3.ColorProviders as material3ColorProviders

// Bridges Tempo's brand color tokens (core/ui/theme/Color.kt) into Glance, used when the user has
// "use Tempo colors" enabled, or as a fallback on devices that don't support dynamic color.
val TempoGlanceColorScheme: ColorProviders =
    material3ColorProviders(
        light =
            lightColorScheme(
                primary = TempoLightPrimary,
                onPrimary = TempoLightOnPrimary,
                background = TempoLightBackground,
                onBackground = TempoLightOnBackground,
                surface = TempoLightSurface,
                onSurface = TempoLightOnSurface,
            ),
        dark =
            darkColorScheme(
                primary = TempoDarkPrimary,
                onPrimary = TempoDarkOnPrimary,
                background = TempoDarkBackground,
                onBackground = TempoDarkOnBackground,
                surface = TempoDarkSurface,
                onSurface = TempoDarkOnSurface,
            ),
    )

fun resolveGlanceColorProviders(
    context: Context,
    useTempoColorsPreference: Boolean,
): ColorProviders =
    // The `&& supportsDynamicColor` conjunct is redundant with shouldUseTempoStaticColors's own
    // check (kept in sync via the shared parameter), but Android Lint's NewApi check only
    // recognizes an SDK guard when it's directly visible in the branch condition, not threaded
    // through a helper function's return value.
    if (!shouldUseTempoStaticColors(useTempoColorsPreference, supportsDynamicColor) && supportsDynamicColor) {
        material3ColorProviders(
            light = dynamicColorScheme(context, isDark = false),
            dark = dynamicColorScheme(context, isDark = true),
        )
    } else {
        TempoGlanceColorScheme
    }
