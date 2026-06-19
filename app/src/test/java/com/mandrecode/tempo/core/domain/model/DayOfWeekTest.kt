package com.mandrecode.tempo.core.domain.model

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import org.junit.Test

class DayOfWeekTest {
    @Test
    fun `fromValue returns correct DayOfWeek for valid values`() {
        assertThat(DayOfWeek.fromValue(1)).isEqualTo(DayOfWeek.MONDAY)
        assertThat(DayOfWeek.fromValue(2)).isEqualTo(DayOfWeek.TUESDAY)
        assertThat(DayOfWeek.fromValue(3)).isEqualTo(DayOfWeek.WEDNESDAY)
        assertThat(DayOfWeek.fromValue(4)).isEqualTo(DayOfWeek.THURSDAY)
        assertThat(DayOfWeek.fromValue(5)).isEqualTo(DayOfWeek.FRIDAY)
        assertThat(DayOfWeek.fromValue(6)).isEqualTo(DayOfWeek.SATURDAY)
        assertThat(DayOfWeek.fromValue(7)).isEqualTo(DayOfWeek.SUNDAY)
    }

    @Test
    fun `fromValue returns null for invalid values`() {
        assertThat(DayOfWeek.fromValue(0)).isNull()
        assertThat(DayOfWeek.fromValue(8)).isNull()
        assertThat(DayOfWeek.fromValue(-1)).isNull()
    }

    @Test
    fun `fromKotlinDayOfWeek maps all days correctly`() {
        assertThat(DayOfWeek.fromKotlinDayOfWeek(kotlinx.datetime.DayOfWeek.MONDAY)).isEqualTo(DayOfWeek.MONDAY)
        assertThat(DayOfWeek.fromKotlinDayOfWeek(kotlinx.datetime.DayOfWeek.TUESDAY)).isEqualTo(DayOfWeek.TUESDAY)
        assertThat(DayOfWeek.fromKotlinDayOfWeek(kotlinx.datetime.DayOfWeek.WEDNESDAY)).isEqualTo(DayOfWeek.WEDNESDAY)
        assertThat(DayOfWeek.fromKotlinDayOfWeek(kotlinx.datetime.DayOfWeek.THURSDAY)).isEqualTo(DayOfWeek.THURSDAY)
        assertThat(DayOfWeek.fromKotlinDayOfWeek(kotlinx.datetime.DayOfWeek.FRIDAY)).isEqualTo(DayOfWeek.FRIDAY)
        assertThat(DayOfWeek.fromKotlinDayOfWeek(kotlinx.datetime.DayOfWeek.SATURDAY)).isEqualTo(DayOfWeek.SATURDAY)
        assertThat(DayOfWeek.fromKotlinDayOfWeek(kotlinx.datetime.DayOfWeek.SUNDAY)).isEqualTo(DayOfWeek.SUNDAY)
    }

    @Test
    fun `all enum entries have correct int values`() {
        assertThat(DayOfWeek.MONDAY.value).isEqualTo(1)
        assertThat(DayOfWeek.SUNDAY.value).isEqualTo(7)
        assertThat(DayOfWeek.entries).hasSize(7)
    }
}
