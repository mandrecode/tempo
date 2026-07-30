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
import com.mandrecode.tempo.features.routines.domain.model.Habit
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
import kotlinx.coroutines.test.TestScope
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
import kotlin.time.Duration.Companion.minutes
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
    private val previewFlow = MutableStateFlow<Long?>(null)
    private val focusSessionRepository =
        mockk<FocusSessionRepository> {
            every { activeSession } returns sessionFlow
            every { defaultLengthMinutes } returns lengthFlow
            every { previewTaskId } returns previewFlow
            every { breakLengthMinutes } returns MutableStateFlow(5)
            every { setPreviewTaskId(any()) } answers { previewFlow.value = firstArg() }
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
        upNext: FocusAgendaItem.TaskEntry? = null,
        todayItems: List<FocusAgendaItem> = emptyList(),
        undated: Int = 0,
    ) = FocusAgenda(upNext = listOfNotNull(upNext), today = todayItems, undatedTaskCount = undated)

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
    fun `starting a session uses the up next task`() =
        runTest {
            val entry = FocusAgendaItem.TaskEntry(task(9, "Report"))
            stubDay(agendaOf(upNext = entry, todayItems = listOf(entry)))

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.StartSession())
            advanceUntilIdle()

            coVerify { focusSessionUseCases.start(taskId = 9, taskTitle = "Report") }
        }

    @Test
    fun `previewing up next opens the session screen without starting a session`() =
        runTest {
            val entry = FocusAgendaItem.TaskEntry(task(9, "Report"))
            stubDay(agendaOf(upNext = entry, todayItems = listOf(entry)))

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.PreviewUpNext(9))
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(FocusContract.UiEffect.OpenSessionScreen)
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 0) { focusSessionUseCases.start(any(), any(), any()) }
            // The screen resolves its subject from the preview when nothing is running.
            assertThat(viewModel.uiState.value.sessionEntry).isEqualTo(entry)
        }

    @Test
    fun `leaving the session screen clears the preview`() =
        runTest {
            val entry = FocusAgendaItem.TaskEntry(task(9, "Report"))
            stubDay(agendaOf(upNext = entry, todayItems = listOf(entry)))

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.PreviewUpNext(9))
            viewModel.onEvent(FocusContract.UiEvent.ClearSessionPreview)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.sessionEntry).isNull()
        }

    @Test
    fun `a day of habits alone leaves the row with nothing to preview`() =
        runTest {
            val habitEntry =
                FocusAgendaItem.HabitEntry(
                    habit = focusHabit(4),
                    isCompleted = false,
                )
            stubDay(agendaOf(todayItems = listOf(habitEntry)))

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.upNext).isEmpty()
            assertThat(viewModel.uiState.value.sessionEntry).isNull()
        }

    @Test
    fun `starting a session takes the user into it`() =
        runTest {
            val entry = FocusAgendaItem.TaskEntry(task(9, "Report"))
            stubDay(agendaOf(upNext = entry, todayItems = listOf(entry)))

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.StartSession())
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(FocusContract.UiEffect.OpenSessionScreen)
                cancelAndIgnoreRemainingEvents()
            }
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

    /**
     * The alarm receiver's ending, as the ViewModel sees it: a session whose time is spent, then
     * gone from the repository. Nothing else tells the app a session expired.
     */
    private suspend fun TestScope.runSessionOut(
        taskId: Long = 1,
        title: String = "Report",
        isBreak: Boolean = false,
    ) {
        sessionFlow.value =
            FocusSession.start(
                taskId = taskId,
                taskTitle = title,
                now = nowInstant - 30.minutes,
                isBreak = isBreak,
            )
        advanceUntilIdle()
        sessionFlow.value = null
        advanceUntilIdle()
    }

    @Test
    fun `another session also takes the user into it`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()
            runSessionOut(taskId = 11)

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.StartAnotherSession)
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(FocusContract.UiEffect.OpenSessionScreen)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a day of habits alone never reaches the session use case`() =
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

            viewModel.onEvent(FocusContract.UiEvent.StartSession())
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
    fun `a session that runs out raises the completion sheet`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            runSessionOut()

            val finished = viewModel.uiState.value.finishedSession
            assertThat(finished?.taskTitle).isEqualTo("Report")
            // The planned length, not the elapsed time: an expired session ran its full course.
            assertThat(finished?.minutes).isEqualTo(25)
            assertThat(finished?.wasBreak).isFalse()
        }

    @Test
    fun `stopping raises no sheet, since the user already decided`() =
        runTest {
            stubDay()
            val session = FocusSession.start(1, "Report", nowInstant)
            sessionFlow.value = session
            coEvery { focusSessionUseCases.end() } returns session

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.StopSession)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.finishedSession).isNull()
        }

    @Test
    fun `a session cleared before its time is up raises no sheet`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            sessionFlow.value = FocusSession.start(1, "Report", nowInstant)
            advanceUntilIdle()
            sessionFlow.value = null
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.finishedSession).isNull()
        }

    @Test
    fun `a break that runs out is reported as a break`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()

            runSessionOut(isBreak = true)

            assertThat(
                viewModel.uiState.value.finishedSession
                    ?.wasBreak,
            ).isTrue()
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

    private fun focusHabit(id: Long) =
        Habit(
            id = id,
            title = "Habit $id",
            description = "",
            createdDate = LocalDateTime(today, LocalTime(9, 0)),
        )

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
    fun `taking a break starts a real break countdown`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()
            runSessionOut()

            viewModel.onEvent(FocusContract.UiEvent.TakeBreak)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.finishedSession).isNull()
            coVerify { focusSessionUseCases.start(taskId = any(), taskTitle = any(), isBreak = true) }
        }

    @Test
    fun `the completion sheet can finish the task once the session has already ended`() =
        runTest {
            val open = task(9, "Report")
            stubDay(agendaOf(todayItems = listOf(FocusAgendaItem.TaskEntry(open))))
            val viewModel = createViewModel()
            advanceUntilIdle()

            // The sheet offers this after expiry, when there is no running session left to read
            // the task from — it has to fall back to the one the last session ran on.
            runSessionOut(taskId = 9)
            assertThat(viewModel.uiState.value.finishedSession).isNotNull()

            viewModel.onEvent(FocusContract.UiEvent.CompleteSessionTask)
            advanceUntilIdle()

            coVerify { toggleTaskCompletion(open) }
            assertThat(viewModel.uiState.value.finishedSession).isNull()
        }

    @Test
    fun `finishing from the sheet is a no-op when the task is already done`() =
        runTest {
            val done = task(9, "Report", isCompleted = true)
            stubDay(agendaOf(todayItems = listOf(FocusAgendaItem.TaskEntry(done))))
            val viewModel = createViewModel()
            advanceUntilIdle()
            runSessionOut(taskId = 9)

            viewModel.onEvent(FocusContract.UiEvent.CompleteSessionTask)
            advanceUntilIdle()

            coVerify(exactly = 0) { toggleTaskCompletion(any()) }
            // The sheet still closes: the choice was made either way.
            assertThat(viewModel.uiState.value.finishedSession).isNull()
        }

    @Test
    fun `marking the session's task done completes it and ends the session`() =
        runTest {
            val open = task(4, "Report")
            stubDay(agendaOf(upNext = FocusAgendaItem.TaskEntry(open)))
            val session = FocusSession.start(4, "Report", nowInstant)
            sessionFlow.value = session
            coEvery { focusSessionUseCases.end() } returns session

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.CompleteSessionTask)
            advanceUntilIdle()

            coVerify { toggleTaskCompletion(open) }
            coVerify { focusSessionUseCases.end() }
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
    fun `another session restarts on the task the last one ran`() =
        runTest {
            stubDay()
            val viewModel = createViewModel()
            advanceUntilIdle()
            runSessionOut(taskId = 11)

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
