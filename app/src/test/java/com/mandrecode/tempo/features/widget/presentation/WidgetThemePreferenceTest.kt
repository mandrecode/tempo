package com.mandrecode.tempo.features.widget.presentation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WidgetThemePreferenceTest {
    @Test
    fun givenDynamicColorSupported_thenDynamicColorsAreUsed() {
        assertThat(shouldUseTempoStaticColors(dynamicColorSupported = true)).isFalse()
    }

    @Test
    fun givenDynamicColorUnsupported_thenStaticTempoColorsAreUsedAsFallback() {
        assertThat(shouldUseTempoStaticColors(dynamicColorSupported = false)).isTrue()
    }
}
