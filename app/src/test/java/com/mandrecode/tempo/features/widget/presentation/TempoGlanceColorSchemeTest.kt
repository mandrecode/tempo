package com.mandrecode.tempo.features.widget.presentation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TempoGlanceColorSchemeTest {
    @Test
    fun givenUseTempoColorsEnabled_thenStaticTempoColorsAreUsedRegardlessOfDynamicSupport() {
        assertThat(shouldUseTempoStaticColors(useTempoColorsPreference = true, dynamicColorSupported = true)).isTrue()
        assertThat(shouldUseTempoStaticColors(useTempoColorsPreference = true, dynamicColorSupported = false)).isTrue()
    }

    @Test
    fun givenUseTempoColorsDisabledAndDynamicSupported_thenDynamicColorsAreUsed() {
        assertThat(shouldUseTempoStaticColors(useTempoColorsPreference = false, dynamicColorSupported = true)).isFalse()
    }

    @Test
    fun givenUseTempoColorsDisabledAndDynamicUnsupported_thenStaticTempoColorsAreUsedAsFallback() {
        assertThat(shouldUseTempoStaticColors(useTempoColorsPreference = false, dynamicColorSupported = false)).isTrue()
    }
}
