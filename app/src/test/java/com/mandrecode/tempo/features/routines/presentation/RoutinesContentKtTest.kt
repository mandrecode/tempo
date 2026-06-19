package com.mandrecode.tempo.features.routines.presentation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RoutinesContentKtTest {
    @Test
    fun `formatTime pads single digit hour`() {
        assertThat(formatTime(8, 30)).isEqualTo("08:30")
    }

    @Test
    fun `formatTime pads single digit minute`() {
        assertThat(formatTime(14, 5)).isEqualTo("14:05")
    }

    @Test
    fun `formatTime formats midnight correctly`() {
        assertThat(formatTime(0, 0)).isEqualTo("00:00")
    }

    @Test
    fun `formatTime formats end of day correctly`() {
        assertThat(formatTime(23, 59)).isEqualTo("23:59")
    }

    @Test
    fun `formatTime formats noon correctly`() {
        assertThat(formatTime(12, 0)).isEqualTo("12:00")
    }
}
