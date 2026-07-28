package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test

class VacationModePreferencesTest {
    private var stored: String? = null
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        stored = null
        val editor =
            mockk<SharedPreferences.Editor>(relaxed = true) {
                every { putString(any(), any()) } answers {
                    stored = secondArg()
                    this@mockk
                }
            }
        sharedPreferences =
            mockk {
                every { getString(any(), any()) } answers { stored ?: secondArg() }
                every { edit() } returns editor
            }
    }

    @Test
    fun `starts empty when nothing is stored`() {
        assertThat(createPreferences().periods.value).isEmpty()
    }

    @Test
    fun `start records an open-ended period from today`() {
        val preferences = createPreferences()

        preferences.setPaused(TODAY, paused = true)

        assertThat(preferences.periods.value).containsExactly(VacationPeriod(TODAY))
        assertThat(stored).isEqualTo("2026-03-10..")
    }

    @Test
    fun `start while already paused is a no-op`() {
        val preferences = createPreferences()

        preferences.setPaused(TODAY, paused = true)
        preferences.setPaused(TODAY.plusDays(3), paused = true)

        assertThat(preferences.periods.value).containsExactly(VacationPeriod(TODAY))
    }

    @Test
    fun `stop closes the active period on the day before`() {
        val preferences = createPreferences()
        preferences.setPaused(TODAY, paused = true)

        preferences.setPaused(TODAY.plusDays(5), paused = false)

        assertThat(preferences.periods.value)
            .containsExactly(VacationPeriod(TODAY, TODAY.plusDays(4)))
    }

    @Test
    fun `starting and stopping on the same day leaves nothing stored`() {
        val preferences = createPreferences()
        preferences.setPaused(TODAY, paused = true)

        preferences.setPaused(TODAY, paused = false)

        assertThat(preferences.periods.value).isEmpty()
    }

    @Test
    fun `stop while not paused is a no-op`() {
        val preferences = createPreferences()
        preferences.setPaused(TODAY, paused = true)
        preferences.setPaused(TODAY.plusDays(2), paused = false)
        val afterFirstStop = preferences.periods.value

        preferences.setPaused(TODAY.plusDays(9), paused = false)

        assertThat(preferences.periods.value).isEqualTo(afterFirstStop)
    }

    @Test
    fun `setPlannedEnd closes the active period on the chosen date`() {
        val preferences = createPreferences()
        preferences.setPaused(TODAY, paused = true)

        preferences.setPlannedEnd(TODAY, TODAY.plusDays(7))

        assertThat(preferences.periods.value)
            .containsExactly(VacationPeriod(TODAY, TODAY.plusDays(7)))
        assertThat(stored).isEqualTo("2026-03-10..2026-03-17")
    }

    @Test
    fun `setPlannedEnd before the period start is ignored`() {
        val preferences = createPreferences()
        preferences.setPaused(TODAY, paused = true)

        preferences.setPlannedEnd(TODAY, TODAY.minusDays(1))

        assertThat(preferences.periods.value).containsExactly(VacationPeriod(TODAY))
    }

    @Test
    fun `clearing the planned end reopens the period`() {
        val preferences = createPreferences()
        preferences.setPaused(TODAY, paused = true)
        preferences.setPlannedEnd(TODAY, TODAY.plusDays(7))

        preferences.setPlannedEnd(TODAY, null)

        assertThat(preferences.periods.value).containsExactly(VacationPeriod(TODAY))
    }

    @Test
    fun `setPlannedEnd while not paused is a no-op`() {
        val preferences = createPreferences()

        preferences.setPlannedEnd(TODAY, TODAY.plusDays(3))

        assertThat(preferences.periods.value).isEmpty()
    }

    @Test
    fun `periods survive a round-trip through the stored string`() {
        val first = createPreferences()
        first.setPaused(TODAY, paused = true)
        first.setPlannedEnd(TODAY, TODAY.plusDays(4))
        first.setPaused(TODAY.plusDays(30), paused = true)

        val reopened = createPreferences()

        assertThat(reopened.periods.value)
            .containsExactly(
                VacationPeriod(TODAY, TODAY.plusDays(4)),
                VacationPeriod(TODAY.plusDays(30)),
            ).inOrder()
    }

    @Test
    fun `unparsable stored entries are dropped`() {
        stored = "not-a-date..2026-03-12;2026-04-01..2026-04-05;2026-05-01..oops;;garbage"

        assertThat(createPreferences().periods.value)
            .containsExactly(VacationPeriod(LocalDate(2026, 4, 1), LocalDate(2026, 4, 5)))
    }

    @Test
    fun `stored inverted range is dropped`() {
        stored = "2026-04-10..2026-04-01"

        assertThat(createPreferences().periods.value).isEmpty()
    }

    @Test
    fun `stored overlapping ranges are merged on read`() {
        stored = "2026-04-01..2026-04-10;2026-04-05..2026-04-20"

        assertThat(createPreferences().periods.value)
            .containsExactly(VacationPeriod(LocalDate(2026, 4, 1), LocalDate(2026, 4, 20)))
    }

    @Test
    fun `replaceAll normalizes what it is given`() {
        val preferences = createPreferences()

        preferences.replaceAll(
            listOf(
                VacationPeriod(LocalDate(2026, 4, 5), LocalDate(2026, 4, 20)),
                VacationPeriod(LocalDate(2026, 4, 1), LocalDate(2026, 4, 10)),
                VacationPeriod(LocalDate(2026, 1, 10), LocalDate(2026, 1, 1)),
            ),
        )

        assertThat(preferences.periods.value)
            .containsExactly(VacationPeriod(LocalDate(2026, 4, 1), LocalDate(2026, 4, 20)))
    }

    private fun createPreferences(): VacationModePreferencesImpl {
        val context =
            mockk<Context> {
                every { getSharedPreferences(any(), any()) } returns sharedPreferences
            }
        return VacationModePreferencesImpl(context)
    }

    private fun LocalDate.plusDays(days: Int): LocalDate = LocalDate.fromEpochDays(toEpochDays() + days)

    private fun LocalDate.minusDays(days: Int): LocalDate = plusDays(-days)

    private companion object {
        val TODAY = LocalDate(2026, 3, 10)
    }
}
