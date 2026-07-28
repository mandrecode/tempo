package com.mandrecode.tempo.util

import android.content.Context
import android.text.format.DateFormat
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.datetime.LocalTime
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

    @Test
    fun `formatTimeOfDay uses 24h clock when the system prefers it`() {
        withHourFormat(is24Hour = true) { context ->
            assertThat(DateTimeFormatter.formatTimeOfDay(LocalTime(hour = 9, minute = 0), context))
                .isEqualTo("09:00")
            assertThat(DateTimeFormatter.formatTimeOfDay(LocalTime(hour = 21, minute = 5), context))
                .isEqualTo("21:05")
        }
    }

    @Test
    fun `formatTimeOfDay uses 12h clock when the system prefers it`() {
        withHourFormat(is24Hour = false) { context ->
            assertThat(DateTimeFormatter.formatTimeOfDay(LocalTime(hour = 9, minute = 0), context))
                .startsWith("9:00")
            assertThat(DateTimeFormatter.formatTimeOfDay(LocalTime(hour = 21, minute = 5), context))
                .startsWith("9:05")
        }
    }

    private fun withHourFormat(
        is24Hour: Boolean,
        block: (Context) -> Unit,
    ) {
        mockkStatic(DateFormat::class)
        try {
            val context = mockk<Context>()
            every { DateFormat.is24HourFormat(context) } returns is24Hour
            block(context)
        } finally {
            unmockkStatic(DateFormat::class)
        }
    }
}
