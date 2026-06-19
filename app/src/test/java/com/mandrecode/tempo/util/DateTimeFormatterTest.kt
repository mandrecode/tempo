package com.mandrecode.tempo.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DateTimeFormatterTest {
    @Test
    fun `formatEnumPatterns_haveDifferentFormats`() {
        assertThat(DateTimeFormatter.Format.Short.pattern24h)
            .isNotEqualTo(DateTimeFormatter.Format.Full.pattern24h)
    }

    @Test
    fun `formatEnumPatterns_haveDifferent12hFormats`() {
        assertThat(DateTimeFormatter.Format.Short.pattern12h)
            .isNotEqualTo(DateTimeFormatter.Format.Full.pattern12h)
    }

    @Test
    fun `shortFormat_24h_containsExpectedPattern`() {
        assertThat(DateTimeFormatter.Format.Short.pattern24h).contains("HH:mm")
    }

    @Test
    fun `shortFormat_12h_containsExpectedPattern`() {
        assertThat(DateTimeFormatter.Format.Short.pattern12h).contains("h:mm a")
    }

    @Test
    fun `fullFormat_24h_containsDayOfWeek`() {
        assertThat(DateTimeFormatter.Format.Full.pattern24h).startsWith("E")
    }

    @Test
    fun `fullFormat_12h_containsDayOfWeek`() {
        assertThat(DateTimeFormatter.Format.Full.pattern12h).startsWith("E")
    }

    @Test
    fun `shortFormat_containsYear`() {
        assertThat(DateTimeFormatter.Format.Short.pattern24h).contains("yyyy")
        assertThat(DateTimeFormatter.Format.Short.pattern12h).contains("yyyy")
    }

    @Test
    fun `fullFormat_containsYear`() {
        assertThat(DateTimeFormatter.Format.Full.pattern24h).contains("yyyy")
        assertThat(DateTimeFormatter.Format.Full.pattern12h).contains("yyyy")
    }
}
