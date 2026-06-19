package com.mandrecode.tempo.core.ui.util

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.ColorOption
import org.junit.Test

class ColorSelectionUtilTest {
    private val testOptions =
        listOf(
            ColorOption(Color.Red, Color(0xFF880000), "red"),
            ColorOption(Color.Green, Color(0xFF008800), "green"),
            ColorOption(Color.Blue, Color(0xFF000088), "blue"),
        )

    @Test
    fun `returns gray when options are empty`() {
        val result = selectRandomColor(emptyList(), isDarkTheme = false)
        assertThat(result).isEqualTo(Color.Gray)
    }

    @Test
    fun `returns light color when not dark theme`() {
        val singleOption = listOf(ColorOption(Color.Red, Color.Blue, "test"))
        val result = selectRandomColor(singleOption, isDarkTheme = false)
        assertThat(result).isEqualTo(Color.Red)
    }

    @Test
    fun `returns dark color when dark theme`() {
        val singleOption = listOf(ColorOption(Color.Red, Color.Blue, "test"))
        val result = selectRandomColor(singleOption, isDarkTheme = true)
        assertThat(result).isEqualTo(Color.Blue)
    }

    @Test
    fun `avoids existing colors when possible`() {
        val result =
            selectRandomColor(
                testOptions,
                isDarkTheme = false,
                existingColors = listOf(Color.Red, Color.Green),
            )
        assertThat(result).isEqualTo(Color.Blue)
    }

    @Test
    fun `falls back to any color when all are used`() {
        val lightColors = testOptions.map { it.light }
        val result =
            selectRandomColor(
                testOptions,
                isDarkTheme = false,
                existingColors = lightColors,
            )
        assertThat(result).isIn(lightColors)
    }

    @Test
    fun `returns a color from available options`() {
        val result = selectRandomColor(testOptions, isDarkTheme = false)
        assertThat(result).isIn(testOptions.map { it.light })
    }
}
