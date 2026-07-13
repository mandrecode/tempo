package com.mandrecode.tempo.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.luminance
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SurfaceColorsTest {
    @Test
    fun givenLightColorScheme_whenResolvingSurfaces_thenCardIsLighterThanScreen() {
        val colorScheme = lightColorScheme()

        assertThat(colorScheme.primaryScreenContainer).isEqualTo(colorScheme.surfaceContainer)
        assertThat(colorScheme.neutralCardContainer).isEqualTo(colorScheme.surfaceContainerLowest)
        assertThat(colorScheme.neutralCardContainer.luminance())
            .isGreaterThan(colorScheme.primaryScreenContainer.luminance())
    }

    @Test
    fun givenDarkColorScheme_whenResolvingSurfaces_thenCardIsDarkerThanScreen() {
        val colorScheme = darkColorScheme()

        assertThat(colorScheme.primaryScreenContainer).isEqualTo(colorScheme.surfaceContainer)
        assertThat(colorScheme.neutralCardContainer).isEqualTo(colorScheme.surfaceContainerLowest)
        assertThat(colorScheme.neutralCardContainer.luminance())
            .isLessThan(colorScheme.primaryScreenContainer.luminance())
    }
}
