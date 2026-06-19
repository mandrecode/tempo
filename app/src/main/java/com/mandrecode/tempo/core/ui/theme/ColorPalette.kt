package com.mandrecode.tempo.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/**
 * Represents a color option in the picker with light and dark variants for better contrast.
 * Can be displayed as a solid circle or a segmented one showing multiple theme colors.
 */
@Stable
data class ColorOption(
    val light: Color,
    val dark: Color,
    val labelKey: String,
    val lightAccents: List<Color>? = null,
    val darkAccents: List<Color>? = null,
    val isSegmented: Boolean = false,
) {
    /**
     * Gets the 3 accent variations for this color based on the current theme.
     * Returns: [top_half, bottom_left, bottom_right] colors for the segmented circle.
     */
    fun getAccents(isDarkTheme: Boolean): List<Color> {
        if (!isSegmented) {
            val color = if (isDarkTheme) dark else light
            return listOf(color, color, color)
        }

        val accents = if (isDarkTheme) darkAccents else lightAccents
        if (accents != null && accents.size >= 3) return accents

        val baseColor = if (isDarkTheme) dark else light
        return if (isDarkTheme) {
            listOf(
                baseColor, // Top (Tone 80)
                baseColor.copy(alpha = 0.4f), // BL
                baseColor.copy(alpha = 0.6f), // BR
            )
        } else {
            listOf(
                baseColor, // Top (Tone 40)
                baseColor.copy(alpha = 0.2f), // BL
                baseColor.copy(alpha = 0.4f), // BR
            )
        }
    }

    /**
     * Gets the primary color for this option based on the current theme.
     */
    fun getColor(isDarkTheme: Boolean): Color = if (isDarkTheme) dark else light
}

/**
 * Provides Material You colors extracted from the current theme's ColorScheme.
 * Currently only the wallpaper-based dynamic theme.
 */
fun getMaterialYouColors(colorScheme: ColorScheme): List<ColorOption> =
    listOf(
        // Current Dynamic Theme (Wallpaper based)
        ColorOption(
            light = colorScheme.primary,
            dark = colorScheme.primary,
            labelKey = "color_dynamic",
            isSegmented = true,
            lightAccents =
                listOf(
                    colorScheme.primary, // Top
                    colorScheme.primaryContainer, // BL
                    colorScheme.tertiaryContainer, // BR
                ),
            darkAccents =
                listOf(
                    colorScheme.primary, // Top
                    colorScheme.primaryContainer, // BL
                    colorScheme.tertiaryContainer, // BR
                ),
        ),
    )

/**
 * Provides the monochromatic theme option (black in light theme, white in dark theme).
 * Rendered as a solid swatch and placed between the Material You section and the regular palette.
 */
fun getMonochromeColor(): ColorOption =
    ColorOption(
        light = Color.Black,
        dark = Color.White,
        labelKey = "color_monochrome",
    )

/**
 * Provides the official set of basic colors from Material You that adapt dynamically to light/dark theme.
 * These follow the M3 tonal palette logic (Tone 40 for Light, Tone 80 for Dark).
 */
fun getPastelColors(): List<ColorOption> =
    listOf(
        // Blue
        ColorOption(
            light = PastelBlueLight,
            dark = PastelBlueDark,
            labelKey = "color_m3_blue",
        ),
        // Green
        ColorOption(
            light = PastelGreenLight,
            dark = PastelGreenDark,
            labelKey = "color_m3_green",
        ),
        // Yellow
        ColorOption(
            light = PastelYellowLight,
            dark = PastelYellowDark,
            labelKey = "color_m3_yellow",
        ),
        // Orange
        ColorOption(
            light = PastelOrangeLight,
            dark = PastelOrangeDark,
            labelKey = "color_m3_orange",
        ),
        // Red
        ColorOption(
            light = PastelRedLight,
            dark = PastelRedDark,
            labelKey = "color_m3_red",
        ),
        // Pink
        ColorOption(
            light = PastelPinkLight,
            dark = PastelPinkDark,
            labelKey = "color_m3_pink",
        ),
        // Purple
        ColorOption(
            light = PastelPurpleLight,
            dark = PastelPurpleDark,
            labelKey = "color_m3_purple",
        ),
        // Cyan
        ColorOption(
            light = PastelCyanLight,
            dark = PastelCyanDark,
            labelKey = "color_m3_cyan",
        ),
        // Tempo Green (brand colors)
        ColorOption(
            light = TempoBrandPrimary,
            dark = TempoBrandBackground,
            labelKey = "color_tempo_green",
        ),
    )

/**
 * Resolves a color key to a Color object based on the current theme.
 */
fun resolveColor(
    colorKey: String?,
    colorScheme: ColorScheme,
    isDarkTheme: Boolean,
): Color? {
    if (colorKey == null) return null

    // Check Material You colors first
    getMaterialYouColors(colorScheme).find { it.labelKey == colorKey }?.let {
        return it.getColor(isDarkTheme)
    }

    // Check Pastel colors
    getPastelColors().find { it.labelKey == colorKey }?.let {
        return it.getColor(isDarkTheme)
    }

    // Check Monochrome
    getMonochromeColor().takeIf { it.labelKey == colorKey }?.let {
        return it.getColor(isDarkTheme)
    }

    return null
}

/**
 * Resolves a color to its closest ColorOption labelKey.
 */
fun resolveColorToKey(
    color: Color?,
    colorScheme: ColorScheme,
    isDarkTheme: Boolean,
): String? {
    if (color == null) return null

    val allOptions = getMaterialYouColors(colorScheme) + getPastelColors() + getMonochromeColor()
    return allOptions.find { it.getColor(isDarkTheme) == color }?.labelKey
}
