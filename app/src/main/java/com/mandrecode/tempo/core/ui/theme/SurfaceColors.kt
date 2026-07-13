package com.mandrecode.tempo.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** Muted surface shared by primary screen backgrounds and their continuous chrome. */
val ColorScheme.primaryScreenContainer: Color
    get() = surfaceContainer

/** Neutral card surface that is lighter in light themes and darker in dark themes. */
val ColorScheme.neutralCardContainer: Color
    get() = surfaceContainerLowest
