package com.mandrecode.tempo.features.routines.presentation.components.sections

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
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
        // Five days ahead is the far end of the window, and it fits without scrolling. Matched by
        // weekday and date together: the date alone ("1") would also match "31" across a month end.
        composeTestRule
            .onNodeWithContentDescription(description(today.plus(DatePeriod(days = 5))), substring = true)
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

    /** At twice the font size the date still sits inside its segment rather than being clipped. */
    @Test
    fun largeText_growsTheSegmentsInsteadOfClippingThem() {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                TempoTheme { DayFilterRow(selectedDate = today, onSelectDate = {}) }
            }
        }

        val segment = composeTestRule.onNodeWithContentDescription("Today", substring = true).getUnclippedBoundsInRoot()
        val date =
            composeTestRule
                .onNodeWithText(today.day.toString(), useUnmergedTree = true)
                .getUnclippedBoundsInRoot()
        assertThat(date.bottom.value).isAtMost(segment.bottom.value)
        assertThat(date.top.value).isAtLeast(segment.top.value)
    }

    /** "Sunday 1": English weekday name and date, as the segment announces them. */
    private fun description(date: LocalDate): String {
        val weekday =
            date.dayOfWeek.name
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        return "$weekday ${date.day}"
    }
}
