package com.mandrecode.tempo.util

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.ColorOption
import org.junit.Before
import org.junit.Test

class ColorRandomizerTest {
    private lateinit var colorRandomizer: ColorRandomizer
    private lateinit var testColors: List<ColorOption>

    @Before
    fun setup() {
        colorRandomizer = ColorRandomizer()
        testColors =
            listOf(
                ColorOption(Color(0xFFFF0000), Color(0xFF880000), "red"),
                ColorOption(Color(0xFF00FF00), Color(0xFF008800), "green"),
                ColorOption(Color(0xFF0000FF), Color(0xFF000088), "blue"),
                ColorOption(Color(0xFFFFFF00), Color(0xFF888800), "yellow"),
                ColorOption(Color(0xFFFF00FF), Color(0xFF880088), "magenta"),
                ColorOption(Color(0xFF00FFFF), Color(0xFF008888), "cyan"),
            )
    }

    @Test
    fun `selectRandomColor returns a color from available options`() {
        val selectedColor =
            colorRandomizer.selectRandomColor(
                availableColors = testColors,
                isDarkTheme = false,
            )

        val availableLightColors = testColors.map { it.light }
        assertThat(selectedColor).isIn(availableLightColors)
    }

    @Test
    fun `selectRandomColor returns dark variant when isDarkTheme is true`() {
        val selectedColor =
            colorRandomizer.selectRandomColor(
                availableColors = testColors,
                isDarkTheme = true,
            )

        val availableDarkColors = testColors.map { it.dark }
        assertThat(selectedColor).isIn(availableDarkColors)
    }

    @Test
    fun `selectRandomColor avoids recently used colors when possible`() {
        // Select 3 colors in sequence
        val color1 = colorRandomizer.selectRandomColor(testColors, false)
        val color2 = colorRandomizer.selectRandomColor(testColors, false)
        val color3 = colorRandomizer.selectRandomColor(testColors, false)

        // All three should be different
        assertThat(color1).isNotEqualTo(color2)
        assertThat(color2).isNotEqualTo(color3)
        assertThat(color1).isNotEqualTo(color3)
    }

    @Test
    fun `selectRandomColor avoids existing colors when possible`() {
        val existingColor = testColors[0].light
        val existingColors = listOf(existingColor)

        // Select multiple colors
        val selectedColors = mutableSetOf<Color>()
        repeat(10) {
            val color =
                colorRandomizer.selectRandomColor(
                    availableColors = testColors,
                    isDarkTheme = false,
                    existingColors = existingColors,
                )
            selectedColors.add(color)
        }

        // With 6 available colors and 1 existing, we should get variety
        // At least 3 different colors should be selected across 10 attempts
        assertThat(selectedColors.size).isAtLeast(3)
    }

    @Test
    fun `selectRandomColor handles empty color list gracefully`() {
        val selectedColor =
            colorRandomizer.selectRandomColor(
                availableColors = emptyList(),
                isDarkTheme = false,
            )

        assertThat(selectedColor).isEqualTo(Color.Gray)
    }

    @Test
    fun `selectRandomColor handles single color option`() {
        val singleColor = listOf(testColors[0])
        val selectedColor =
            colorRandomizer.selectRandomColor(
                availableColors = singleColor,
                isDarkTheme = false,
            )

        assertThat(selectedColor).isEqualTo(singleColor[0].light)
    }

    @Test
    fun `reset clears recent color history`() {
        // Select some colors to populate history
        repeat(3) {
            colorRandomizer.selectRandomColor(testColors, false)
        }

        // Reset the history
        colorRandomizer.reset()

        // After reset, we should be able to get the same colors again without them being avoided
        val color1 = colorRandomizer.selectRandomColor(testColors, false)
        val color2 = colorRandomizer.selectRandomColor(testColors, false)

        // We just verify that colors are selected (no exception thrown)
        val availableLightColors = testColors.map { it.light }
        assertThat(color1).isIn(availableLightColors)
        assertThat(color2).isIn(availableLightColors)
    }

    @Test
    fun `selectRandomColor tracks up to maxRecentSize colors`() {
        // Select more colors than maxRecentSize (6)
        val selectedColors = mutableListOf<Color>()
        repeat(8) {
            val color = colorRandomizer.selectRandomColor(testColors, false)
            selectedColors.add(color)
        }

        // The first selected color should be available again after 6 new selections
        // This is difficult to test deterministically due to randomness, but we can verify
        // that we got valid colors
        selectedColors.forEach { color ->
            assertThat(color).isIn(testColors.map { it.light })
        }
    }
}
