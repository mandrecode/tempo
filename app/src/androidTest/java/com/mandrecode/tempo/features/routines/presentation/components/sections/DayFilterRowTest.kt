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
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
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

        composeTestRule.onNodeWithContentDescription(string(R.string.yesterday), substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(string(R.string.today), substring = true).assertIsOn()
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

        composeTestRule.onNodeWithContentDescription(string(R.string.tomorrow), substring = true).performClick()

        assertThat(selected).isEqualTo(today.plus(DatePeriod(days = 1)))
        composeTestRule.onNodeWithContentDescription(string(R.string.tomorrow), substring = true).assertIsOn()
        composeTestRule.onNodeWithContentDescription(string(R.string.today), substring = true).assertIsOff()
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

        val segment = composeTestRule.onNodeWithContentDescription(string(R.string.today), substring = true).getUnclippedBoundsInRoot()
        val date =
            composeTestRule
                .onNodeWithText(today.day.toString(), useUnmergedTree = true)
                .getUnclippedBoundsInRoot()
        assertThat(date.bottom.value).isAtMost(segment.bottom.value)
        assertThat(date.top.value).isAtLeast(segment.top.value)
    }

    /**
     * "Sunday 1" in whatever language the device runs: built from the app's own strings, the way
     * the segment builds its announcement, so the test does not depend on an English locale.
     */
    private fun description(date: LocalDate): String {
        val weekday =
            when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> R.string.monday
                DayOfWeek.TUESDAY -> R.string.tuesday
                DayOfWeek.WEDNESDAY -> R.string.wednesday
                DayOfWeek.THURSDAY -> R.string.thursday
                DayOfWeek.FRIDAY -> R.string.friday
                DayOfWeek.SATURDAY -> R.string.saturday
                DayOfWeek.SUNDAY -> R.string.sunday
            }
        return context.getString(R.string.day_filter_day_description, string(weekday), date.day)
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun string(res: Int): String = context.getString(res)
}
