package com.mandrecode.tempo.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.luminance
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PageSurfaceContrastTest {
    @Test
    fun givenLightColorScheme_whenApplyingPageContrast_thenNativeRolesFormReverseHierarchy() {
        val baseColorScheme = lightColorScheme()

        val colorScheme = baseColorScheme.withPageSurfaceContrast(darkTheme = false)

        assertThat(colorScheme.background).isEqualTo(baseColorScheme.surfaceContainer)
        assertThat(colorScheme.surface).isEqualTo(baseColorScheme.surfaceContainerLowest)
        assertThat(colorScheme.surfaceContainerLow).isEqualTo(baseColorScheme.surfaceContainerLowest)
        assertThat(colorScheme.surfaceContainer).isEqualTo(baseColorScheme.surface)
        assertThat(colorScheme.surfaceContainerHigh).isEqualTo(baseColorScheme.surfaceContainerLow)
        assertThat(colorScheme.surfaceContainerHighest).isEqualTo(baseColorScheme.surfaceContainerHigh)
        assertThat(colorScheme.surfaceContainerLow.luminance()).isGreaterThan(colorScheme.background.luminance())
        assertThat(colorScheme.primary).isEqualTo(baseColorScheme.primary)
    }

    @Test
    fun givenDarkColorScheme_whenApplyingPageContrast_thenNativeRolesFormReverseHierarchy() {
        val baseColorScheme = darkColorScheme()

        val colorScheme = baseColorScheme.withPageSurfaceContrast(darkTheme = true)

        assertThat(colorScheme.background).isEqualTo(baseColorScheme.surfaceContainer)
        assertThat(colorScheme.surface).isEqualTo(baseColorScheme.surfaceContainerLowest)
        assertThat(colorScheme.surfaceContainerLow).isEqualTo(baseColorScheme.surfaceContainerLowest)
        assertThat(colorScheme.surfaceContainer).isEqualTo(baseColorScheme.surfaceContainerLow)
        assertThat(colorScheme.surfaceContainerHigh).isEqualTo(baseColorScheme.surfaceContainer)
        assertThat(colorScheme.surfaceContainerHighest).isEqualTo(baseColorScheme.surfaceContainerHigh)
        assertThat(colorScheme.surfaceContainerLow.luminance()).isLessThan(colorScheme.background.luminance())
        assertThat(colorScheme.primary).isEqualTo(baseColorScheme.primary)
    }
}
