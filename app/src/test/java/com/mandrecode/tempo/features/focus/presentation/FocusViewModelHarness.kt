package com.mandrecode.tempo.features.focus.presentation

import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import com.mandrecode.tempo.features.focus.domain.model.FocusAgenda
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
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
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Mocks, dispatcher and fixtures shared by the Focus view-model tests.
 *
 * Extracted when one test class outgrew what a single file should hold: the day's behaviour and
 * the timer's are two subjects, together only because they share a setup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class FocusViewModelHarness {
    private val testDispatcher = StandardTestDispatcher()
    protected val today = LocalDate(2026, 7, 29)
    protected val nowInstant = Instant.fromEpochMilliseconds(1_800_000_000_000)

    protected val getFocusAgenda = mockk<GetFocusAgendaUseCase>()
    protected val getFocusHistory = mockk<GetFocusHistoryUseCase>()
    protected val getFocusStreak = mockk<GetFocusStreakUseCase>(relaxed = true)
    protected val recordDailyActivity = mockk<RecordDailyActivityUseCase>(relaxed = true)
    protected val toggleTaskCompletion = mockk<ToggleTaskCompletionUseCase>(relaxed = true)
    protected val toggleHabitCompletion = mockk<ToggleHabitCompletionUseCase>(relaxed = true)
    protected val focusSessionUseCases = mockk<FocusSessionUseCases>(relaxed = true)
    protected val sessionFlow = MutableStateFlow<FocusSession?>(null)
    protected val lengthFlow = MutableStateFlow(25)
    protected val previewFlow = MutableStateFlow<Long?>(null)
    protected val focusSessionRepository =
        mockk<FocusSessionRepository> {
            every { activeSession } returns sessionFlow
            every { defaultLengthMinutes } returns lengthFlow
            every { previewTaskId } returns previewFlow
            every { breakLengthMinutes } returns MutableStateFlow(5)
            every { setPreviewTaskId(any()) } answers { previewFlow.value = firstArg() }
        }

    protected val clock =
        object : Clock {
            // Fixed so "today" is deterministic; the date is what the ViewModel reads.
            override fun now(): Instant = nowInstant
        }

    protected fun task(
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

    protected fun agendaOf(
        upNext: FocusAgendaItem.TaskEntry? = null,
        todayItems: List<FocusAgendaItem> = emptyList(),
        undated: Int = 0,
    ) = FocusAgenda(upNext = listOfNotNull(upNext), today = todayItems, undatedTaskCount = undated)

    protected fun stubDay(agenda: FocusAgenda = agendaOf()) {
        every { getFocusAgenda(any()) } returns flowOf(agenda)
        every { getFocusHistory(any(), any()) } returns
            flowOf(listOf(DailyFocusActivity(date = today, scheduledCount = 2, completedCount = 1)))
        coEvery { getFocusStreak(any()) } returns 14
    }

    protected fun createViewModel() =
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

    /**
     * The alarm receiver's ending, as the ViewModel sees it: a session whose time is spent, then
     * gone from the repository. Nothing else tells the app a session expired.
     */
    protected suspend fun TestScope.runSessionOut(
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

    protected fun focusHabit(id: Long) =
        Habit(
            id = id,
            title = "Habit $id",
            description = "",
            createdDate = LocalDateTime(today, LocalTime(9, 0)),
        )
}
