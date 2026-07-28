package com.mandrecode.tempo.core.domain.model

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.LocalDate
import org.junit.Test

class VacationPeriodTest {
    @Test
    fun `covers includes both bounds`() {
        val period = VacationPeriod(LocalDate(2026, 3, 10), LocalDate(2026, 3, 14))

        assertThat(period.covers(LocalDate(2026, 3, 10))).isTrue()
        assertThat(period.covers(LocalDate(2026, 3, 12))).isTrue()
        assertThat(period.covers(LocalDate(2026, 3, 14))).isTrue()
        assertThat(period.covers(LocalDate(2026, 3, 9))).isFalse()
        assertThat(period.covers(LocalDate(2026, 3, 15))).isFalse()
    }

    @Test
    fun `open-ended period covers every later date`() {
        val period = VacationPeriod(LocalDate(2026, 3, 10))

        assertThat(period.isOpenEnded).isTrue()
        assertThat(period.covers(LocalDate(2026, 3, 9))).isFalse()
        assertThat(period.covers(LocalDate(2026, 3, 10))).isTrue()
        assertThat(period.covers(LocalDate(2099, 1, 1))).isTrue()
    }

    @Test
    fun `normalize drops inverted ranges`() {
        val inverted = VacationPeriod(LocalDate(2026, 3, 10), LocalDate(2026, 3, 5))
        val valid = VacationPeriod(LocalDate(2026, 4, 1), LocalDate(2026, 4, 3))

        assertThat(VacationPeriod.normalize(listOf(inverted, valid))).containsExactly(valid)
    }

    @Test
    fun `normalize sorts by start date`() {
        val later = VacationPeriod(LocalDate(2026, 5, 1), LocalDate(2026, 5, 3))
        val earlier = VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 3))

        assertThat(VacationPeriod.normalize(listOf(later, earlier)))
            .containsExactly(earlier, later)
            .inOrder()
    }

    @Test
    fun `normalize merges overlapping ranges`() {
        val first = VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 10))
        val second = VacationPeriod(LocalDate(2026, 3, 5), LocalDate(2026, 3, 20))

        assertThat(VacationPeriod.normalize(listOf(first, second)))
            .containsExactly(VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 20)))
    }

    @Test
    fun `normalize merges adjacent ranges`() {
        val first = VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 10))
        val backToBack = VacationPeriod(LocalDate(2026, 3, 11), LocalDate(2026, 3, 14))

        assertThat(VacationPeriod.normalize(listOf(first, backToBack)))
            .containsExactly(VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 14)))
    }

    @Test
    fun `normalize keeps ranges separated by a tracked day apart`() {
        val first = VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 10))
        val second = VacationPeriod(LocalDate(2026, 3, 12), LocalDate(2026, 3, 14))

        assertThat(VacationPeriod.normalize(listOf(first, second)))
            .containsExactly(first, second)
            .inOrder()
    }

    @Test
    fun `normalize collapses duplicates`() {
        val period = VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 10))

        assertThat(VacationPeriod.normalize(listOf(period, period, period)))
            .containsExactly(period)
    }

    @Test
    fun `normalize keeps a contained range inside its enclosing range`() {
        val outer = VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 20))
        val inner = VacationPeriod(LocalDate(2026, 3, 5), LocalDate(2026, 3, 8))

        assertThat(VacationPeriod.normalize(listOf(outer, inner))).containsExactly(outer)
    }

    @Test
    fun `normalize lets an open-ended range absorb everything after it`() {
        val openEnded = VacationPeriod(LocalDate(2026, 3, 1))
        val later = VacationPeriod(LocalDate(2026, 6, 1), LocalDate(2026, 6, 5))
        val muchLater = VacationPeriod(LocalDate(2027, 1, 1))

        assertThat(VacationPeriod.normalize(listOf(openEnded, later, muchLater)))
            .containsExactly(openEnded)
    }

    @Test
    fun `normalize puts the only open-ended range last`() {
        val closed = VacationPeriod(LocalDate(2026, 1, 1), LocalDate(2026, 1, 5))
        val openEnded = VacationPeriod(LocalDate(2026, 3, 1))

        val result = VacationPeriod.normalize(listOf(openEnded, closed))

        assertThat(result).containsExactly(closed, openEnded).inOrder()
        assertThat(result.count { it.isOpenEnded }).isEqualTo(1)
    }

    @Test
    fun `normalize extends a closed range into an open-ended one when they touch`() {
        val closed = VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 10))
        val openEnded = VacationPeriod(LocalDate(2026, 3, 8))

        assertThat(VacationPeriod.normalize(listOf(closed, openEnded)))
            .containsExactly(VacationPeriod(LocalDate(2026, 3, 1)))
    }

    @Test
    fun `normalize of an empty list is empty`() {
        assertThat(VacationPeriod.normalize(emptyList())).isEmpty()
    }

    @Test
    fun `activeOn returns the covering period or null`() {
        val first = VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 10))
        val second = VacationPeriod(LocalDate(2026, 5, 1))
        val periods = listOf(first, second)

        assertThat(VacationPeriod.activeOn(periods, LocalDate(2026, 3, 4))).isEqualTo(first)
        assertThat(VacationPeriod.activeOn(periods, LocalDate(2026, 9, 9))).isEqualTo(second)
        assertThat(VacationPeriod.activeOn(periods, LocalDate(2026, 4, 4))).isNull()
    }
}
