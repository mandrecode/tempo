package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
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

    /**
     * Beside the icon column a 412 dp phone leaves the row about 311 dp. Seven days share it as
     * targets a finger can tell apart: every day at least 40 dp wide and 48 dp tall, and a tap in
     * the middle of each one lands on that day and no other.
     */
    @Test
    fun onACompactSheet_everyDayIsItsOwnTarget() {
        days = setOf(DayOfWeek.MONDAY)
        composeTestRule.setContent {
            TempoTheme {
                Box(modifier = Modifier.width(312.dp)) {
                    DayOfWeekSelector(
                        selectedDays = days,
                        onDaysChange = { days = it },
                        nullMeansEveryDay = false,
                    )
                }
            }
        }

        val others = listOf("Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        listOf("Monday", "Sunday").plus(others).forEach { name ->
            val bounds = composeTestRule.onNodeWithContentDescription(name).getUnclippedBoundsInRoot()
            assertThat(bounds.width.value).isAtLeast(40f)
            assertThat(bounds.height.value).isAtLeast(48f)
        }
        others.forEach { name -> composeTestRule.onNodeWithContentDescription(name).performClick() }

        assertThat(days).containsExactly(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
        )
        composeTestRule.onNodeWithContentDescription("Sunday").assertIsOff()
    }

    /**
     * A 360 dp phone leaves 264 dp beside the icon column: under 38 dp a day. The days keep their
     * full 48 dp instead and the row scrolls, so Sunday is still there to pick.
     */
    @Test
    fun onANarrowRow_daysKeepFullSizeAndScroll() {
        days = setOf(DayOfWeek.MONDAY)
        composeTestRule.setContent {
            TempoTheme {
                Box(modifier = Modifier.width(264.dp)) {
                    DayOfWeekSelector(
                        selectedDays = days,
                        onDaysChange = { days = it },
                        nullMeansEveryDay = false,
                    )
                }
            }
        }

        val monday = composeTestRule.onNodeWithContentDescription("Monday").getUnclippedBoundsInRoot()
        assertThat(monday.width.value).isAtLeast(48f)
        composeTestRule.onNodeWithContentDescription("Sunday").performScrollTo().performClick()

        assertThat(days).containsExactly(DayOfWeek.MONDAY, DayOfWeek.SUNDAY)
        composeTestRule.onNodeWithContentDescription("Sunday").assertIsOn()
    }
}
