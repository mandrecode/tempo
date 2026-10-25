package com.mandrecode.tempo.features.routines.presentation.components.sections

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock

class DayFilterRowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

    @Test
    fun showsTheWholeWindow_withTodaySelected() {
        var selected by mutableStateOf(today)
        composeTestRule.setContent {
            TempoTheme { DayFilterRow(selectedDate = selected, onSelectDate = { selected = it }) }
        }

        composeTestRule.onNodeWithContentDescription("Yesterday", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Today", substring = true).assertIsOn()
        // Five days ahead is the far end of the window, and it fits without scrolling.
        composeTestRule
            .onNodeWithContentDescription(today.plus(DatePeriod(days = 5)).day.toString(), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun tappingADay_selectsIt() {
        var selected: LocalDate by mutableStateOf(today)
        composeTestRule.setContent {
            TempoTheme { DayFilterRow(selectedDate = selected, onSelectDate = { selected = it }) }
        }

        composeTestRule.onNodeWithContentDescription("Tomorrow", substring = true).performClick()

        assertThat(selected).isEqualTo(today.plus(DatePeriod(days = 1)))
        composeTestRule.onNodeWithContentDescription("Tomorrow", substring = true).assertIsOn()
        composeTestRule.onNodeWithContentDescription("Today", substring = true).assertIsOff()
    }
}
