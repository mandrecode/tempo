package com.mandrecode.tempo.util

import androidx.compose.ui.graphics.Color
import com.mandrecode.tempo.core.ui.theme.ColorOption

/**
 * Manages random color selection with repetition avoidance.
 * Keeps track of recently used colors and prefers unused colors.
 */
class ColorRandomizer {
    private val recentColors = mutableListOf<Color>()
    private val maxRecentSize = 6 // Track last 6 colors to avoid immediate repetition

    /**
     * Selects a random color from available options, avoiding recently used colors when possible.
     *
     * @param availableColors List of color options to choose from
     * @param isDarkTheme Whether to use dark or light variant
     * @param existingColors Colors already in use (to avoid when possible)
     * @return Selected color
     */
    fun selectRandomColor(
        availableColors: List<ColorOption>,
        isDarkTheme: Boolean,
        existingColors: List<Color> = emptyList(),
    ): Color {
        if (availableColors.isEmpty()) {
            return Color.Gray // Fallback
        }

        // Get actual colors based on theme
        val colors = availableColors.map { if (isDarkTheme) it.dark else it.light }

        // Filter out recently used colors and existing colors
        val allUsedColors = (recentColors + existingColors).toSet()
        val availableUnusedColors = colors.filterNot { it in allUsedColors }

        // Select a color
        val selectedColor =
            if (availableUnusedColors.isNotEmpty()) {
                availableUnusedColors.random()
            } else {
                // If all colors have been used recently, just pick a random one
                colors.random()
            }

        // Track this color
        recentColors.add(selectedColor)
        if (recentColors.size > maxRecentSize) {
            recentColors.removeAt(0)
        }

        return selectedColor
    }

    /**
     * Clears the recent color history.
     */
    fun reset() {
        recentColors.clear()
    }
}
