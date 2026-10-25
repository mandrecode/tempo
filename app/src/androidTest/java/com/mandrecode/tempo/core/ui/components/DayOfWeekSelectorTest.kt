package com.mandrecode.tempo.core.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import org.junit.Rule
import org.junit.Test

class DayOfWeekSelectorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var days by mutableStateOf<Set<DayOfWeek>?>(null)

    private fun setSelector(nullMeansEveryDay: Boolean = true) {
        composeTestRule.setContent {
            TempoTheme {
                DayOfWeekSelector(
                    selectedDays = days,
                    onDaysChange = { days = it },
                    nullMeansEveryDay = nullMeansEveryDay,
                )
            }
        }
    }

    @Test
    fun everyDayHabit_showsEveryDayOn_andTurningOneOffKeepsTheRest() {
        setSelector()

        composeTestRule.onNodeWithContentDescription("Sunday").assertIsOn()
        composeTestRule.onNodeWithContentDescription("Sunday").performClick()

        composeTestRule.onNodeWithContentDescription("Sunday").assertIsOff()
        composeTestRule.onNodeWithContentDescription("Monday").assertIsOn()
        assertThat(days).isEqualTo(DayOfWeek.ALL_DAYS - DayOfWeek.SUNDAY)
    }

    @Test
    fun theLastDayOfAHabitStaysOn() {
        days = setOf(DayOfWeek.MONDAY)
        setSelector()

        composeTestRule.onNodeWithContentDescription("Monday").performClick()

        composeTestRule.onNodeWithContentDescription("Monday").assertIsOn()
        assertThat(days).containsExactly(DayOfWeek.MONDAY)
    }

    @Test
    fun weeklyTask_withNoDaysPicked_showsNoneOn() {
        setSelector(nullMeansEveryDay = false)

        composeTestRule.onNodeWithContentDescription("Monday").assertIsOff()
        composeTestRule.onNodeWithContentDescription("Wednesday").performClick()

        assertThat(days).containsExactly(DayOfWeek.WEDNESDAY)
    }
}
