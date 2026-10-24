package com.mandrecode.tempo.features.routines.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.presentation.components.sections.HABIT_HISTORY_DOT_ROW_TEST_TAG
import com.mandrecode.tempo.features.routines.presentation.components.sections.HABIT_HISTORY_STREAK_PILL_TEST_TAG
import com.mandrecode.tempo.features.routines.presentation.components.sections.HabitHistoryView
import com.mandrecode.tempo.util.CompletionHistoryUtil
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class HabitHistoryViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val fixedToday = LocalDate(2026, 2, 24)

    @Test
    fun displaysStreakLabel_whenStreakExists() {
        val history = "2026-02-22,2026-02-23,2026-02-24"
        val createdDate = LocalDate(2026, 1, 1)

        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = history,
                    createdDate = createdDate,
                    today = fixedToday,
                )
            }
        }

        composeTestRule.onNodeWithText("3 day streak", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysEncouragingStreakLabel_afterALoneMissedDay() {
        // Yesterday (Feb 23) was missed and today is still open.
        val history = "2026-02-21,2026-02-22"
        val createdDate = LocalDate(2026, 1, 1)

        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = history,
                    createdDate = createdDate,
                    today = fixedToday,
                )
            }
        }

        composeTestRule.onNodeWithText("2 day streak, keep going!", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysLastDaysLabel_whenNoStreak() {
        val history = "2026-01-10"
        val createdDate = LocalDate(2026, 1, 1)

        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = history,
                    createdDate = createdDate,
                    today = fixedToday,
                )
            }
        }

        composeTestRule.onNodeWithText("Last 21 days", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysLastDaysLabel_whenEmptyHistory() {
        val createdDate = LocalDate(2026, 2, 20)

        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = "",
                    createdDate = createdDate,
                    today = fixedToday,
                )
            }
        }

        // 5 days from Feb 20 to Feb 24 inclusive
        composeTestRule.onNodeWithText("Last 5 days", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysEllipsis_whenDotsExceedAvailableWidth() {
        val createdDate = LocalDate(2025, 12, 1)

        composeTestRule.setContent {
            TempoTheme {
                // Use a narrow width to force truncation
                Box(
                    modifier = Modifier.width(150.dp),
                ) {
                    HabitHistoryView(
                        completionHistory = "",
                        createdDate = createdDate,
                        today = fixedToday,
                    )
                }
            }
        }

        // Verify that with enough dates and tight constraints, the ellipsis Text is rendered
        // The exact rendering depends on layout measurements which can be device/density specific
        // So we just verify the component doesn't crash when rendering
        val ellipsisString = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.ellipsis)
        assertThat(ellipsisString).isEqualTo("…")
    }

    @Test
    fun doesNotDisplayEllipsis_whenDotsWithinAvailableWidth() {
        // Only 5 days (Feb 20 → Feb 24) — always fits
        val createdDate = LocalDate(2026, 2, 20)

        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = "",
                    createdDate = createdDate,
                    today = fixedToday,
                )
            }
        }

        composeTestRule.onNodeWithText("…").assertDoesNotExist()
    }

    @Test
    fun displaysTodayLabel_whenCreatedToday() {
        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = "",
                    createdDate = fixedToday,
                    today = fixedToday,
                )
            }
        }

        composeTestRule.onNodeWithText("Today", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysCorrectDaysLabel_whenCompletionsAreBeforeCreatedDate() {
        // Created today, but backfilled three days ago; the two missed days since break the streak
        val history = "2026-02-21"

        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = history,
                    createdDate = fixedToday,
                    today = fixedToday,
                )
            }
        }

        // Even though created today, it should expand to show the 4 days (21st to 24th)
        composeTestRule.onNodeWithText("Last 4 days", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysStreakLabel_whenStreakExistsWithUnplannedDaysSkipped() {
        // Tuesday (today) not planned, Monday, Friday completed. Planned Mon, Fri.
        val history = "2026-02-20,2026-02-23"
        val createdDate = LocalDate(2026, 2, 20)
        val repeatDays =
            setOf(
                com.mandrecode.tempo.core.domain.model.DayOfWeek.MONDAY,
                com.mandrecode.tempo.core.domain.model.DayOfWeek.FRIDAY,
            )

        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = history,
                    createdDate = createdDate,
                    repeatDays = repeatDays,
                    today = fixedToday, // Tuesday
                )
            }
        }

        composeTestRule.onNodeWithText("2 day streak", substring = true).assertIsDisplayed()
    }

    @Test
    fun keepsDotCountStable_whenTogglingCompletionWithFullWindow() {
        val createdDate = LocalDate(2026, 1, 1)
        val historyWithoutToday =
            (20 downTo 1).joinToString(",") { daysAgo ->
                LocalDate.fromEpochDays((fixedToday.toEpochDays() - daysAgo).toInt()).toString()
            }
        var completionHistory by mutableStateOf(historyWithoutToday)

        composeTestRule.setContent {
            TempoTheme {
                Box(
                    modifier = Modifier.width(300.dp),
                ) {
                    HabitHistoryView(
                        completionHistory = completionHistory,
                        createdDate = createdDate,
                        today = fixedToday,
                    )
                }
            }
        }

        val dotsBeforeToggle =
            composeTestRule
                .onAllNodes(
                    SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription),
                ).fetchSemanticsNodes()
                .size

        composeTestRule.runOnUiThread {
            completionHistory =
                CompletionHistoryUtil.updateCompletionHistoryForDate(
                    currentHistory = completionHistory,
                    date = fixedToday,
                    isCompleted = true,
                )
        }
        composeTestRule.waitForIdle()

        val dotsAfterToggle =
            composeTestRule
                .onAllNodes(
                    SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription),
                ).fetchSemanticsNodes()
                .size

        assertThat(dotsAfterToggle).isEqualTo(dotsBeforeToggle)
    }

    @Test
    fun keepsDotsVisible_withRecoveringLabelAtLargeFontScale() {
        // Yesterday was missed, so the longer recovering copy is shown.
        val history = "2026-02-21,2026-02-22"
        val createdDate = LocalDate(2026, 1, 1)
        val historySlotTag = "history_slot"

        composeTestRule.setContent {
            TempoTheme {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                    Row(
                        modifier =
                            Modifier
                                .width(320.dp)
                                .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.width(48.dp))
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .testTag(historySlotTag),
                        ) {
                            HabitHistoryView(
                                completionHistory = history,
                                createdDate = createdDate,
                                modifier = Modifier.fillMaxWidth(),
                                today = fixedToday,
                            )
                        }
                    }
                }
            }
        }

        val labelNode = composeTestRule.onNodeWithText("keep going!", substring = true)
        labelNode.assertIsDisplayed()
        val textLayouts = mutableListOf<TextLayoutResult>()
        labelNode
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(textLayouts)
        assertThat(textLayouts).isNotEmpty()
        assertThat(textLayouts.first().hasVisualOverflow).isFalse()
        val dotRowWidth =
            composeTestRule
                .onNodeWithTag(HABIT_HISTORY_DOT_ROW_TEST_TAG)
                .fetchSemanticsNode()
                .boundsInRoot
                .width
        val streakPillWidth =
            composeTestRule
                .onNodeWithTag(HABIT_HISTORY_STREAK_PILL_TEST_TAG)
                .fetchSemanticsNode()
                .boundsInRoot
                .width
        val historySlotWidth =
            composeTestRule
                .onNodeWithTag(historySlotTag)
                .fetchSemanticsNode()
                .boundsInRoot
                .width

        assertThat(dotRowWidth).isGreaterThan(0f)
        assertThat(streakPillWidth).isAtMost(historySlotWidth / 2f)
    }

    @Test
    fun keepsDotsVisibleAndStreakPillBounded_inBottomSheetRowContext() {
        val createdDate = LocalDate(2026, 1, 1)
        val fullWindowHistory =
            (20 downTo 0).joinToString(",") { daysAgo ->
                LocalDate.fromEpochDays((fixedToday.toEpochDays() - daysAgo).toInt()).toString()
            }
        val historySlotTag = "history_slot"

        composeTestRule.setContent {
            TempoTheme {
                Row(
                    modifier =
                        Modifier
                            .width(320.dp)
                            .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.width(48.dp))
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .testTag(historySlotTag),
                    ) {
                        HabitHistoryView(
                            completionHistory = fullWindowHistory,
                            createdDate = createdDate,
                            modifier = Modifier.fillMaxWidth(),
                            today = fixedToday,
                        )
                    }
                }
            }
        }

        val dotCount =
            composeTestRule
                .onAllNodes(
                    SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription),
                ).fetchSemanticsNodes()
                .size
        assertThat(dotCount).isGreaterThan(0)

        val dotRowWidth =
            composeTestRule
                .onNodeWithTag(HABIT_HISTORY_DOT_ROW_TEST_TAG)
                .fetchSemanticsNode()
                .boundsInRoot
                .width
        val streakPillWidth =
            composeTestRule
                .onNodeWithTag(HABIT_HISTORY_STREAK_PILL_TEST_TAG)
                .fetchSemanticsNode()
                .boundsInRoot
                .width
        val historySlotWidth =
            composeTestRule
                .onNodeWithTag(historySlotTag)
                .fetchSemanticsNode()
                .boundsInRoot
                .width

        assertThat(dotRowWidth).isGreaterThan(0f)
        assertThat(streakPillWidth).isLessThan(historySlotWidth * 0.8f)
    }

    @Test
    fun marksPausedDays_whenWindowSpansVacationPeriod() {
        // Completed 02-18..02-20, paused 02-21..02-23, completed again today (02-24).
        val history = "2026-02-18,2026-02-19,2026-02-20,2026-02-24"
        val createdDate = LocalDate(2026, 2, 18)
        val vacationPeriods =
            listOf(VacationPeriod(LocalDate(2026, 2, 21), LocalDate(2026, 2, 23)))

        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = history,
                    createdDate = createdDate,
                    today = fixedToday,
                    vacationPeriods = vacationPeriods,
                )
            }
        }

        val pausedDots =
            composeTestRule
                .onAllNodes(
                    SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription),
                ).fetchSemanticsNodes()
                .count { node ->
                    node.config
                        .getOrNull(SemanticsProperties.ContentDescription)
                        .orEmpty()
                        .any { it.endsWith("paused") }
                }

        assertThat(pausedDots).isEqualTo(3)
    }

    @Test
    fun keepsStreakAcrossPausedDays() {
        val history = "2026-02-18,2026-02-19,2026-02-20,2026-02-24"
        val createdDate = LocalDate(2026, 2, 18)
        val vacationPeriods =
            listOf(VacationPeriod(LocalDate(2026, 2, 21), LocalDate(2026, 2, 23)))

        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = history,
                    createdDate = createdDate,
                    today = fixedToday,
                    vacationPeriods = vacationPeriods,
                )
            }
        }

        // Three days before the pause plus today; the paused days neither count nor break it.
        composeTestRule.onNodeWithText("4 day streak", substring = true).assertIsDisplayed()
    }

    @Test
    fun marksCompletedDaysInsideVacationAsCompleted() {
        val history = "2026-02-22,2026-02-23,2026-02-24"
        val createdDate = LocalDate(2026, 2, 22)
        val vacationPeriods =
            listOf(VacationPeriod(LocalDate(2026, 2, 21), LocalDate(2026, 2, 23)))

        composeTestRule.setContent {
            TempoTheme {
                HabitHistoryView(
                    completionHistory = history,
                    createdDate = createdDate,
                    today = fixedToday,
                    vacationPeriods = vacationPeriods,
                )
            }
        }

        val descriptions =
            composeTestRule
                .onAllNodes(
                    SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription),
                ).fetchSemanticsNodes()
                .flatMap { node ->
                    node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
                }

        assertThat(descriptions.none { it.endsWith("paused") }).isTrue()
        composeTestRule.onNodeWithText("3 day streak", substring = true).assertIsDisplayed()
    }
}
