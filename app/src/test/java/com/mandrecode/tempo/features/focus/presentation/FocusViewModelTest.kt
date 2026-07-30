package com.mandrecode.tempo.features.focus.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.domain.model.FocusHeadlineBand
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.routines.domain.model.Habit
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import org.junit.Test

/** The day Focus shows: agenda, counts, streak, and opening items from it. */
@OptIn(ExperimentalCoroutinesApi::class)
class FocusViewModelTest : FocusViewModelHarness() {
    @Test
    fun `opening the screen recounts the day`() =
        runTest {
            stubDay()

            createViewModel()
            advanceUntilIdle()

            coVerify { recordDailyActivity(any()) }
        }

    @Test
    fun `state carries the agenda, streak and counts`() =
        runTest {
            val entry = FocusAgendaItem.TaskEntry(task(1))
            stubDay(agendaOf(upNext = entry, todayItems = listOf(entry), undated = 3))

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.isLoading).isFalse()
                assertThat(state.streakDays).isEqualTo(14)
                assertThat(state.upNext).containsExactly(entry)
                assertThat(state.undatedTaskCount).isEqualTo(3)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `the headline band follows the counts`() =
        runTest {
            val items = (1L..4L).map { FocusAgendaItem.TaskEntry(task(it, isCompleted = it <= 3)) }
            stubDay(agendaOf(todayItems = items))

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.headlineBand).isEqualTo(FocusHeadlineBand.NEARLY_THERE)
        }

    @Test
    fun `a day of habits alone starts nothing and goes nowhere`() =
        runTest {
            val habitEntry =
                FocusAgendaItem.HabitEntry(
                    habit =
                        com.mandrecode.tempo.features.routines.domain.model
                            .Habit(
                                id = 1,
                                title = "Water",
                                description = "",
                                createdDate = LocalDateTime(today, LocalTime(0, 0)),
                            ),
                    isCompleted = false,
                )
            stubDay(agendaOf(todayItems = listOf(habitEntry)))

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.StartSession())
                advanceUntilIdle()

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `editing a task opens its editor in Focus, without leaving the tab`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.EditTask(task(2)))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.taskEditor)
                .isEqualTo(FocusContract.TaskEditorTarget.Existing(task(2)))

            viewModel.onEvent(FocusContract.UiEvent.DismissEditor)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.taskEditor).isNull()
        }

    @Test
    fun `adding a subtask opens the editor on the parent rather than on a task`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.AddSubtask(7))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.taskEditor)
                .isEqualTo(FocusContract.TaskEditorTarget.NewSubtask(7))
        }

    @Test
    fun `the undated footer opens the Tasks tab`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.UndatedTasksClicked)
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(FocusContract.UiEffect.OpenTasksTab)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggling a habit delegates with today's date`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.ToggleHabitCompletion(habitId = 3, isCompleted = true))
            advanceUntilIdle()

            coVerify { toggleHabitCompletion(3, true, any()) }
        }

    @Test
    fun `editing a habit opens its editor in Focus, and only one editor is ever open`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.EditTask(task(2)))
            viewModel.onEvent(FocusContract.UiEvent.EditHabit(focusHabit(5)))
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.editingHabit
                    ?.id,
            ).isEqualTo(5)
            assertThat(viewModel.uiState.value.taskEditor).isNull()
        }

    @Test
    fun `expanding a chain toggles it on and off again`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.ToggleChainExpanded(2))
            assertThat(viewModel.uiState.value.expandedChainIds).contains(2L)

            viewModel.onEvent(FocusContract.UiEvent.ToggleChainExpanded(2))
            assertThat(viewModel.uiState.value.expandedChainIds).doesNotContain(2L)
        }

    @Test
    fun `completing a chain completes each of its habits`() =
        runTest {
            val habits =
                listOf(1L, 2L).map {
                    com.mandrecode.tempo.features.routines.domain.model
                        .Habit(
                            id = it,
                            title = "Habit $it",
                            description = "",
                            createdDate = LocalDateTime(today, LocalTime(0, 0)),
                        )
                }
            val chainEntry =
                FocusAgendaItem.ChainEntry(
                    chain =
                        com.mandrecode.tempo.features.routines.domain.model
                            .HabitChain(
                                id = 7,
                                title = "Morning",
                                createdDate = LocalDateTime(today, LocalTime(0, 0)),
                            ),
                    habits = habits,
                    isCompleted = false,
                )
            stubDay(agendaOf(todayItems = listOf(chainEntry)))

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.ToggleChainCompletion(chainId = 7, isCompleted = true))
            advanceUntilIdle()

            coVerify { toggleHabitCompletion(1, true, any()) }
            coVerify { toggleHabitCompletion(2, true, any()) }
        }

    @Test
    fun `completing an unknown chain does nothing`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.ToggleChainCompletion(chainId = 404, isCompleted = true))
            advanceUntilIdle()

            coVerify(exactly = 0) { toggleHabitCompletion(any(), any(), any()) }
        }

    @Test
    fun `marking done is a no-op when the task is already complete`() =
        runTest {
            val done = task(4, "Report", isCompleted = true)
            stubDay(agendaOf(upNext = FocusAgendaItem.TaskEntry(done)))
            sessionFlow.value = FocusSession.start(4, "Report", nowInstant)

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.CompleteSessionTask)
            advanceUntilIdle()

            coVerify(exactly = 0) { toggleTaskCompletion(any()) }
        }

    @Test
    fun `a day inside a vacation period says so`() =
        runTest {
            stubDay()
            vacationPeriods.value = listOf(VacationPeriod(start = today.minus(2, DateTimeUnit.DAY)))

            val viewModel = createViewModel()
            advanceUntilIdle()

            // Drives the palm on the title, the same badge Routines carries while paused.
            assertThat(viewModel.uiState.value.isVacationModeActive).isTrue()
        }

    @Test
    fun `a day outside every vacation period does not`() =
        runTest {
            stubDay()
            vacationPeriods.value =
                listOf(
                    VacationPeriod(
                        start = today.minus(9, DateTimeUnit.DAY),
                        endInclusive = today.minus(4, DateTimeUnit.DAY),
                    ),
                )

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.isVacationModeActive).isFalse()
        }
}
