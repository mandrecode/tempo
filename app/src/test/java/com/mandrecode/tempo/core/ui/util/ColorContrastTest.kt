package com.mandrecode.tempo.core.ui.util

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ColorContrastTest {
    private val panel = Color(0xFFF7FAF9)
    private val text = Color(0xFF1A1C1C)

    @Test
    fun `black on white is the maximum ratio`() {
        assertThat(contrastRatio(Color.Black, Color.White)).isWithin(0.01f).of(21f)
    }

    @Test
    fun `a pastel too faint for text is nudged to a readable ratio`() {
        val pastel = Color(0xFFB8E6D8)
        assertThat(contrastRatio(pastel, panel)).isLessThan(TEXT_CONTRAST_RATIO)

        val readable = pastel.readableOn(panel, toward = text)

        assertThat(contrastRatio(readable, panel)).isAtLeast(TEXT_CONTRAST_RATIO)
    }

    @Test
    fun `a colour that already reads is returned unchanged`() {
        val deep = Color(0xFF00695C)
        assertThat(contrastRatio(deep, panel)).isAtLeast(TEXT_CONTRAST_RATIO)

        assertThat(deep.readableOn(panel, toward = text)).isEqualTo(deep)
    }

    @Test
    fun `the nudge works on a dark panel too`() {
        val darkPanel = Color(0xFF101414)
        val dim = Color(0xFF304D47)

        val readable = dim.readableOn(darkPanel, toward = Color(0xFFE0E3E2))

        assertThat(contrastRatio(readable, darkPanel)).isAtLeast(TEXT_CONTRAST_RATIO)
    }
}
