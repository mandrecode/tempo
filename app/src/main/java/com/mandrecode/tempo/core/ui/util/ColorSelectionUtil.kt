package com.mandrecode.tempo.core.ui.util

import androidx.compose.ui.graphics.Color
import com.mandrecode.tempo.core.ui.theme.ColorOption

/**
 * Selects a random color from available options, avoiding existing colors when possible.
 * This is a UI-layer helper that works with ColorOptions and doesn't maintain state.
 *
 * @param availableOptions List of color options to choose from
 * @param isDarkTheme Whether to use dark or light variant
 * @param existingColors Colors already in use (to avoid when possible)
 * @return Selected color
 */
fun selectRandomColor(
    availableOptions: List<ColorOption>,
    isDarkTheme: Boolean,
    existingColors: List<Color> = emptyList(),
): Color {
    if (availableOptions.isEmpty()) {
        return Color.Gray // Fallback
    }

    // Get actual colors based on theme
    val colors = availableOptions.map { it.getColor(isDarkTheme) }

    // Filter out existing colors if possible
    val availableColors = colors.filterNot { it in existingColors }

    return if (availableColors.isNotEmpty()) {
        availableColors.random()
    } else {
        // If all colors are used, just pick any
        colors.random()
    }
}
