package com.mandrecode.tempo.features.focus.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import com.mandrecode.tempo.features.focus.domain.model.FocusAgenda
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.domain.model.FocusHeadlineBand
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import com.mandrecode.tempo.features.focus.domain.usecase.FocusSessionUseCases
import com.mandrecode.tempo.features.focus.domain.usecase.GetFocusAgendaUseCase
import com.mandrecode.tempo.features.focus.domain.usecase.GetFocusHistoryUseCase
import com.mandrecode.tempo.features.focus.domain.usecase.GetFocusStreakUseCase
import com.mandrecode.tempo.features.focus.domain.usecase.RecordDailyActivityUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.ToggleHabitCompletionUseCase
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class FocusViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val today = LocalDate(2026, 7, 29)
    private val nowInstant = Instant.fromEpochMilliseconds(1_800_000_000_000)

    private val getFocusAgenda = mockk<GetFocusAgendaUseCase>()
    private val getFocusHistory = mockk<GetFocusHistoryUseCase>()
    private val getFocusStreak = mockk<GetFocusStreakUseCase>(relaxed = true)
    private val recordDailyActivity = mockk<RecordDailyActivityUseCase>(relaxed = true)
    private val toggleTaskCompletion = mockk<ToggleTaskCompletionUseCase>(relaxed = true)
    private val toggleHabitCompletion = mockk<ToggleHabitCompletionUseCase>(relaxed = true)
    private val focusSessionUseCases = mockk<FocusSessionUseCases>(relaxed = true)
    private val sessionFlow = MutableStateFlow<FocusSession?>(null)
    private val lengthFlow = MutableStateFlow(25)
    private val focusSessionRepository =
        mockk<FocusSessionRepository> {
            every { activeSession } returns sessionFlow
            every { defaultLengthMinutes } returns lengthFlow
        }

    private val clock =
        object : Clock {
            // Fixed so "today" is deterministic; the date is what the ViewModel reads.
            override fun now(): Instant = nowInstant
        }

    private fun task(
        id: Long,
        title: String = "Task $id",
        isCompleted: Boolean = false,
    ) = Task(
        id = id,
        title = title,
        description = "",
        isCompleted = isCompleted,
        reminderDate = LocalDateTime(today, LocalTime(9, 0)),
    )

    private fun agendaOf(
        upNext: FocusAgendaItem? = null,
        todayItems: List<FocusAgendaItem> = emptyList(),
        undated: Int = 0,
    ) = FocusAgenda(upNext = upNext, today = todayItems, undatedTaskCount = undated)

    private fun stubDay(agenda: FocusAgenda = agendaOf()) {
        every { getFocusAgenda(any()) } returns flowOf(agenda)
        every { getFocusHistory(any(), any()) } returns
            flowOf(listOf(DailyFocusActivity(date = today, scheduledCount = 2, completedCount = 1)))
        coEvery { getFocusStreak(any()) } returns 14
    }

    private fun createViewModel() =
        FocusViewModel(
            getFocusAgenda = getFocusAgenda,
            getFocusHistory = getFocusHistory,
            getFocusStreak = getFocusStreak,
            recordDailyActivity = recordDailyActivity,
            toggleTaskCompletion = toggleTaskCompletion,
            focusSessionRepository = focusSessionRepository,
            focusSessionUseCases = focusSessionUseCases,
            toggleHabitCompletion = toggleHabitCompletion,
            clock = clock,
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
                assertThat(state.upNext).isEqualTo(entry)
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
    fun `starting a session uses the up next task`() =
        runTest {
            val entry = FocusAgendaItem.TaskEntry(task(9, "Report"))
            stubDay(agendaOf(upNext = entry, todayItems = listOf(entry)))

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.StartSession)
            advanceUntilIdle()

            coVerify { focusSessionUseCases.start(taskId = 9, taskTitle = "Report") }
        }

    @Test
    fun `a habit in up next cannot host a session`() =
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
            stubDay(agendaOf(upNext = habitEntry, todayItems = listOf(habitEntry)))

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.StartSession)
            advanceUntilIdle()

            coVerify(exactly = 0) { focusSessionUseCases.start(any(), any()) }
        }

    @Test
    fun `the running session is mirrored into state`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            sessionFlow.value = FocusSession.start(1, "Report", nowInstant)
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.session
                    ?.taskId,
            ).isEqualTo(1)
        }

    @Test
    fun `opening the session asks for its screen rather than flipping a flag`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.OpenSessionScreen)
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(FocusContract.UiEffect.OpenSessionScreen)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `stopping raises the completion sheet with the banked minutes`() =
        runTest {
            stubDay()
            val session = FocusSession.start(1, "Report", nowInstant)
            sessionFlow.value = session
            coEvery { focusSessionUseCases.end() } returns session

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.StopSession)
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.finishedSession
                    ?.taskTitle,
            ).isEqualTo("Report")
        }

    @Test
    fun `completing the session's own task ends the session`() =
        runTest {
            val open = task(4, "Report")
            stubDay(agendaOf(todayItems = listOf(FocusAgendaItem.TaskEntry(open))))
            val session = FocusSession.start(4, "Report", nowInstant)
            sessionFlow.value = session
            coEvery { focusSessionUseCases.end() } returns session

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.ToggleTaskCompletion(open))
            advanceUntilIdle()

            coVerify { focusSessionUseCases.end() }
        }

    @Test
    fun `completing an unrelated task leaves the session running`() =
        runTest {
            val other = task(8)
            stubDay(agendaOf(todayItems = listOf(FocusAgendaItem.TaskEntry(other))))
            sessionFlow.value = FocusSession.start(4, "Report", nowInstant)

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.ToggleTaskCompletion(other))
            advanceUntilIdle()

            coVerify(exactly = 0) { focusSessionUseCases.end() }
        }

    @Test
    fun `dismissing clears the completion sheet`() =
        runTest {
            stubDay()
            val session = FocusSession.start(1, "Report", nowInstant)
            sessionFlow.value = session
            coEvery { focusSessionUseCases.end() } returns session

            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(FocusContract.UiEvent.StopSession)
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.DismissFinishedSession)

            assertThat(viewModel.uiState.value.finishedSession).isNull()
        }

    @Test
    fun `editing a task asks the Tasks tab to open`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.EditTask(task(2)))
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(FocusContract.UiEffect.OpenTaskInTasksTab(2))
                cancelAndIgnoreRemainingEvents()
            }
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
    fun `pause and resume delegate to the session use cases`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.PauseSession)
            viewModel.onEvent(FocusContract.UiEvent.ResumeSession)

            coVerify { focusSessionUseCases.pause() }
            coVerify { focusSessionUseCases.resume() }
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
    fun `editing a habit asks the Routines tab to open`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.EditHabit(5))
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(FocusContract.UiEffect.OpenHabitInRoutinesTab(5))
                cancelAndIgnoreRemainingEvents()
            }
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
    fun `taking a break just clears the sheet`() =
        runTest {
            stubDay()
            val session = FocusSession.start(1, "Report", nowInstant)
            sessionFlow.value = session
            coEvery { focusSessionUseCases.end() } returns session

            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(FocusContract.UiEvent.StopSession)
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.TakeBreak)

            assertThat(viewModel.uiState.value.finishedSession).isNull()
        }

    @Test
    fun `another session restarts on the task the last one ran`() =
        runTest {
            stubDay()
            val session = FocusSession.start(11, "Report", nowInstant)
            sessionFlow.value = session
            coEvery { focusSessionUseCases.end() } returns session

            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(FocusContract.UiEvent.StopSession)
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.StartAnotherSession)
            advanceUntilIdle()

            coVerify { focusSessionUseCases.start(taskId = 11, taskTitle = "Report") }
        }

    @Test
    fun `the configured session length reaches state`() =
        runTest {
            stubDay()
            lengthFlow.value = 45

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.defaultSessionLengthMinutes).isEqualTo(45)
        }
}
