package com.mandrecode.tempo.features.routines.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test

class RoutinesContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testDate = LocalDate(2024, 6, 15)
    private val createdDate = LocalDateTime(2024, 1, 1, 0, 0)

    /**
     * Builds a [RoutinesContract.UiState] with timeline items computed from
     * the supplied habits and chains, matching what the ViewModel would produce.
     */
    private fun buildUiState(
        habits: List<Habit> = emptyList(),
        habitChains: List<HabitChain> = emptyList(),
        selectedDate: LocalDate = testDate,
        isLoading: Boolean = false,
        expandedChainIds: kotlinx.collections.immutable.PersistentSet<Long> = persistentSetOf(),
    ): RoutinesContract.UiState {
        val (scheduled, unscheduled) =
            computeTimelineItems(habits, habitChains, selectedDate)
        return RoutinesContract.UiState(
            habits = habits.toPersistentList(),
            habitChains = habitChains.toPersistentList(),
            scheduledTimelineItems = scheduled,
            unscheduledTimelineItems = unscheduled,
            isLoading = isLoading,
            selectedDate = selectedDate,
            expandedChainIds = expandedChainIds,
        )
    }

    @Test
    fun showsLoadingIndicator_whenLoading() {
        composeTestRule.setContent {
            TempoTheme {
                RoutinesContent(
                    uiState = RoutinesContract.UiState(isLoading = true),
                    onEvent = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Loading habits…", substring = true).assertIsDisplayed()
    }

    @Test
    fun showsEmptyDayMessage_whenNoHabits() {
        composeTestRule.setContent {
            TempoTheme {
                RoutinesContent(
                    uiState = buildUiState(),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("No habits", substring = true).assertIsDisplayed()
    }

    @Test
    fun showsHabitCard_whenHabitsExist() {
        val habit =
            Habit(
                id = 1,
                title = "Morning Meditation",
                description = "10 minutes",
                createdDate = createdDate,
            )

        composeTestRule.setContent {
            TempoTheme {
                RoutinesContent(
                    uiState = buildUiState(habits = listOf(habit)),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Morning Meditation").assertIsDisplayed()
    }

    @Test
    fun showsAddHabitFab() {
        composeTestRule.setContent {
            TempoTheme {
                RoutinesContent(
                    uiState = RoutinesContract.UiState(isLoading = false),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithContentDescription("Add habit", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    // --- Chain habit rendering tests ---

    @Test
    fun showsChainCard_withTitle() {
        val chain =
            HabitChain(
                id = 10,
                title = "Morning Routine",
                habitIds = listOf(1L, 2L),
                createdDate = createdDate,
            )
        val habits =
            listOf(
                Habit(id = 1, title = "Brush Teeth", description = "", createdDate = createdDate),
                Habit(id = 2, title = "Stretch", description = "", createdDate = createdDate),
            )

        composeTestRule.setContent {
            TempoTheme {
                RoutinesContent(
                    uiState =
                        buildUiState(
                            habits = habits,
                            habitChains = listOf(chain),
                        ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Morning Routine").assertIsDisplayed()
    }

    @Test
    fun chainHabitsAreNotShownAsStandalone() {
        val chain =
            HabitChain(
                id = 10,
                title = "Morning Routine",
                habitIds = listOf(1L),
                createdDate = createdDate,
            )
        val chainHabit =
            Habit(id = 1, title = "Brush Teeth", description = "", createdDate = createdDate)
        val standaloneHabit =
            Habit(id = 2, title = "Read a Book", description = "", createdDate = createdDate)

        composeTestRule.setContent {
            TempoTheme {
                RoutinesContent(
                    uiState =
                        buildUiState(
                            habits = listOf(chainHabit, standaloneHabit),
                            habitChains = listOf(chain),
                        ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        // Chain card is shown
        composeTestRule.onNodeWithText("Morning Routine").assertIsDisplayed()
        // Standalone habit is shown as its own card
        composeTestRule.onNodeWithText("Read a Book").assertIsDisplayed()
    }

    @Test
    fun scheduledChainAppearsAboveUnscheduledHabit() {
        val scheduledChain =
            HabitChain(
                id = 10,
                title = "Scheduled Chain",
                habitIds = listOf(1L),
                periodicReminder = LocalDateTime(2024, 6, 15, 8, 0),
                createdDate = createdDate,
            )
        val unscheduledHabit =
            Habit(id = 2, title = "Unscheduled Habit", description = "", createdDate = createdDate)

        composeTestRule.setContent {
            TempoTheme {
                RoutinesContent(
                    uiState =
                        buildUiState(
                            habits =
                                listOf(
                                    Habit(id = 1, title = "Chain Habit", description = "", createdDate = createdDate),
                                    unscheduledHabit,
                                ),
                            habitChains = listOf(scheduledChain),
                        ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Scheduled Chain").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unscheduled Habit").assertIsDisplayed()

        val chainTop =
            composeTestRule
                .onNodeWithText("Scheduled Chain")
                .fetchSemanticsNode()
                .boundsInRoot.top
        val unscheduledTop =
            composeTestRule
                .onNodeWithText("Unscheduled Habit")
                .fetchSemanticsNode()
                .boundsInRoot.top
        assertThat(chainTop).isLessThan(unscheduledTop)
    }

    @Test
    fun noUnscheduledSeparator_whenAllItemsAreScheduled() {
        val scheduledHabit =
            Habit(
                id = 1,
                title = "Scheduled Habit",
                description = "",
                createdDate = createdDate,
                reminderDate = LocalDateTime(2024, 6, 15, 9, 0),
            )

        composeTestRule.setContent {
            TempoTheme {
                RoutinesContent(
                    uiState = buildUiState(habits = listOf(scheduledHabit)),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Scheduled Habit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unscheduled Habits").assertDoesNotExist()
    }

    @Test
    fun chainHabitsWithoutReminder_stayInsideChainAfterCompletionToggle() {
        // Simulates the race condition scenario: chain habits have no reminderDate
        // but should always render inside their chain card, never as standalone.
        val chain =
            HabitChain(
                id = 10,
                title = "Evening Routine",
                habitIds = listOf(1L, 2L),
                periodicReminder = LocalDateTime(2024, 6, 15, 20, 0),
                createdDate = createdDate,
            )
        // Child habits intentionally have NO reminderDate (they inherit scheduling from chain)
        val habit1 =
            Habit(id = 1, title = "Meditate", description = "", createdDate = createdDate)
        val habit2 =
            Habit(id = 2, title = "Journal", description = "", createdDate = createdDate)

        val uiState =
            mutableStateOf(
                buildUiState(
                    habits = listOf(habit1, habit2),
                    habitChains = listOf(chain),
                    expandedChainIds = persistentSetOf(10L),
                ),
            )

        composeTestRule.setContent {
            TempoTheme {
                RoutinesContent(
                    uiState = uiState.value,
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        // Chain card is visible
        composeTestRule.onNodeWithText("Evening Routine").assertIsDisplayed()
        // Child habits are visible inside the expanded chain
        composeTestRule.onNodeWithText("Meditate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Journal").assertIsDisplayed()
        // No unscheduled separator — the only items are scheduled chain habits
        composeTestRule.onNodeWithText("Unscheduled Habits").assertDoesNotExist()

        // Now simulate a toggle: habit1 gets updated completionHistory (as if toggled)
        val updatedHabit1 =
            habit1.copy(
                completionHistory = testDate.toString(),
                isCompleted = true,
            )
        val updatedChain =
            chain.copy(completionHistory = "")

        uiState.value =
            buildUiState(
                habits = listOf(updatedHabit1, habit2),
                habitChains = listOf(updatedChain),
                expandedChainIds = persistentSetOf(10L),
            )

        composeTestRule.waitForIdle()
        // After toggle, chain card is still visible
        composeTestRule.onNodeWithText("Evening Routine").assertIsDisplayed()
        // Child habits still inside chain — NOT in an unscheduled section
        composeTestRule.onNodeWithText("Meditate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Journal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unscheduled Habits").assertDoesNotExist()
    }

    @Test
    fun chainHabitsStayInsideChain_whenOnlyHabitsFlowUpdates() {
        // Simulates the intermediate combine() state where habits updated but chains haven't yet
        val chain =
            HabitChain(
                id = 10,
                title = "Study Chain",
                habitIds = listOf(1L, 2L),
                periodicReminder = LocalDateTime(2024, 6, 15, 14, 0),
                createdDate = createdDate,
            )
        val habit1 = Habit(id = 1, title = "Read Chapter", description = "", createdDate = createdDate)
        val habit2 = Habit(id = 2, title = "Take Notes", description = "", createdDate = createdDate)

        val uiState =
            mutableStateOf(
                buildUiState(
                    habits = listOf(habit1, habit2),
                    habitChains = listOf(chain),
                    expandedChainIds = persistentSetOf(10L),
                ),
            )

        composeTestRule.setContent {
            TempoTheme {
                RoutinesContent(
                    uiState = uiState.value,
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Study Chain").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read Chapter").assertIsDisplayed()

        // Simulate intermediate state: habits updated but chains are OLD (stale).
        // Timeline items stay the same because they only depend on structural fields.
        val updatedHabit1 = habit1.copy(completionHistory = testDate.toString(), isCompleted = true)
        uiState.value =
            buildUiState(
                habits = listOf(updatedHabit1, habit2),
                habitChains = listOf(chain),
                expandedChainIds = persistentSetOf(10L),
            )

        composeTestRule.waitForIdle()
        // Chain card still shows — habit must NOT appear standalone
        composeTestRule.onNodeWithText("Study Chain").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read Chapter").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unscheduled Habits").assertDoesNotExist()
    }
}
