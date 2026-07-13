package com.mandrecode.tempo.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class TempoThemeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenLightTempoPalette_whenProvidingTheme_thenReverseSurfaceHierarchyIsExposed() {
        val colorScheme = captureTempoColorScheme(darkTheme = false)

        assertThat(colorScheme.background).isEqualTo(TempoLightSurfaceContainer)
        assertThat(colorScheme.surface).isEqualTo(TempoLightSurfaceContainerLowest)
        assertThat(colorScheme.surfaceContainerLow).isEqualTo(TempoLightSurfaceContainerLowest)
        assertThat(colorScheme.primary).isEqualTo(TempoLightPrimary)
    }

    @Test
    fun givenDarkTempoPalette_whenProvidingTheme_thenReverseSurfaceHierarchyIsExposed() {
        val colorScheme = captureTempoColorScheme(darkTheme = true)

        assertThat(colorScheme.background).isEqualTo(TempoDarkSurfaceContainer)
        assertThat(colorScheme.surface).isEqualTo(TempoDarkSurfaceContainerLowest)
        assertThat(colorScheme.surfaceContainerLow).isEqualTo(TempoDarkSurfaceContainerLowest)
        assertThat(colorScheme.primary).isEqualTo(TempoDarkPrimary)
    }

    private fun captureTempoColorScheme(darkTheme: Boolean): ColorScheme {
        var capturedColorScheme: ColorScheme? = null
        composeTestRule.setContent {
            TempoTheme(
                darkTheme = darkTheme,
                dynamicColor = false,
                useTempoColors = true,
            ) {
                val colorScheme = MaterialTheme.colorScheme
                SideEffect {
                    capturedColorScheme = colorScheme
                }
            }
        }
        composeTestRule.waitForIdle()

        return checkNotNull(capturedColorScheme)
    }
}
