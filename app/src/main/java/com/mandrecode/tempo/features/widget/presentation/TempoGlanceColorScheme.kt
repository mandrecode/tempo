package com.mandrecode.tempo.features.widget.presentation

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.glance.material3.ColorProviders
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

// Bridges Tempo's brand color tokens (core/ui/theme/Color.kt) into Glance, so the widget's chrome
// tracks the same light/dark palette as the rest of the app rather than dynamic wallpaper colors.
val TempoGlanceColorScheme =
    ColorProviders(
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
