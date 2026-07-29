package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Test
import kotlin.time.Clock

class RecordDailyActivityUseCaseTest {
    // A Wednesday.
    private val today = LocalDate(2026, 7, 29)

    private val taskRepository = mockk<TaskRepository>()
    private val habitRepository = mockk<HabitRepository>()
    private val activityRepository = mockk<DailyFocusActivityRepository>(relaxed = true)
    private val useCase =
        RecordDailyActivityUseCase(
            taskRepository = taskRepository,
            habitRepository = habitRepository,
            activityRepository = activityRepository,
            clock = Clock.System,
        )

    private fun task(
        id: Long,
        dueDate: LocalDate?,
        isCompleted: Boolean = false,
        parentTaskId: Long? = null,
    ) = Task(
        id = id,
        title = "Task $id",
        description = "",
        isCompleted = isCompleted,
        parentTaskId = parentTaskId,
        reminderDate = dueDate?.let { LocalDateTime(it, kotlinx.datetime.LocalTime(9, 0)) },
    )

    private fun habit(
        id: Long,
        repeatDays: Set<DayOfWeek>? = null,
        completionHistory: String = "",
    ) = Habit(
        id = id,
        title = "Habit $id",
        description = "",
        createdDate = LocalDateTime(today, kotlinx.datetime.LocalTime(0, 0)),
        repeatDays = repeatDays,
        completionHistory = completionHistory,
    )

    private suspend fun record(
        tasks: List<Task> = emptyList(),
        habits: List<Habit> = emptyList(),
    ) {
        every { taskRepository.getAllTasks() } returns flowOf(tasks)
        every { habitRepository.getAllHabits() } returns flowOf(habits)
        useCase(today)
    }

    @Test
    fun `counts today's tasks and habits`() =
        runTest {
            record(
                tasks = listOf(task(1, today), task(2, today, isCompleted = true)),
                habits = listOf(habit(1, completionHistory = today.toString()), habit(2)),
            )

            coVerify { activityRepository.recordCounts(today, scheduledCount = 4, completedCount = 2) }
        }

    @Test
    fun `future tasks are not counted`() =
        runTest {
            record(tasks = listOf(task(1, LocalDate(2026, 8, 5))))

            coVerify { activityRepository.recordCounts(today, scheduledCount = 0, completedCount = 0) }
        }

    @Test
    fun `undated tasks are not counted`() =
        runTest {
            record(tasks = listOf(task(1, dueDate = null)))

            coVerify { activityRepository.recordCounts(today, scheduledCount = 0, completedCount = 0) }
        }

    @Test
    fun `an open overdue task counts, matching what Focus shows`() =
        runTest {
            record(tasks = listOf(task(1, LocalDate(2026, 7, 27))))

            coVerify { activityRepository.recordCounts(today, scheduledCount = 1, completedCount = 0) }
        }

    @Test
    fun `a completed overdue task drops out rather than inflating both counts`() =
        runTest {
            record(tasks = listOf(task(1, LocalDate(2026, 7, 27), isCompleted = true)))

            coVerify { activityRepository.recordCounts(today, scheduledCount = 0, completedCount = 0) }
        }

    @Test
    fun `subtasks are counted through their parent, not separately`() =
        runTest {
            record(
                tasks =
                    listOf(
                        task(1, today),
                        task(2, today, parentTaskId = 1),
                        task(3, today, parentTaskId = 1),
                    ),
            )

            coVerify { activityRepository.recordCounts(today, scheduledCount = 1, completedCount = 0) }
        }

    @Test
    fun `a habit outside its repeat days is not scheduled today`() =
        runTest {
            // Today is a Wednesday.
            record(habits = listOf(habit(1, repeatDays = setOf(DayOfWeek.MONDAY))))

            coVerify { activityRepository.recordCounts(today, scheduledCount = 0, completedCount = 0) }
        }

    @Test
    fun `a habit on one of its repeat days is scheduled today`() =
        runTest {
            record(habits = listOf(habit(1, repeatDays = setOf(DayOfWeek.WEDNESDAY))))

            coVerify { activityRepository.recordCounts(today, scheduledCount = 1, completedCount = 0) }
        }

    @Test
    fun `a habit completed on another day does not count as completed today`() =
        runTest {
            record(habits = listOf(habit(1, completionHistory = "2026-07-28")))

            coVerify { activityRepository.recordCounts(today, scheduledCount = 1, completedCount = 0) }
        }

    @Test
    fun `an empty day is still recorded so the history has no holes`() =
        runTest {
            record()

            coVerify { activityRepository.recordCounts(today, scheduledCount = 0, completedCount = 0) }
        }

    @Test
    fun `only the given day is ever written, so past days cannot be recomputed away`() =
        runTest {
            // This is what makes the history survive a retention purge: yesterday's counts were
            // written while yesterday was current, and nothing here revisits them once the
            // completed tasks behind them are deleted.
            record(tasks = listOf(task(1, today, isCompleted = true)))

            coVerify(exactly = 1) { activityRepository.recordCounts(today, 1, 1) }
            coVerify(exactly = 0) { activityRepository.recordCounts(neq(today), any(), any()) }
        }
}
