package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
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

    private val taskRepository = mockk<TaskRepository>(relaxed = true)
    private val activityRepository = mockk<DailyFocusActivityRepository>(relaxed = true)
    private val useCase =
        RecordDailyActivityUseCase(
            taskRepository = taskRepository,
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

    private suspend fun record(tasks: List<Task> = emptyList()) {
        every { taskRepository.getAllTasks() } returns flowOf(tasks)
        useCase(today)
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
    fun `a subtask standing on its own day is counted, because the day shows it`() =
        runTest {
            // The parent has no date, so nothing on the agenda is holding these two — they are
            // rows in their own right, and the hero has to agree with the list underneath it.
            record(
                tasks =
                    listOf(
                        task(1, null),
                        task(2, today, parentTaskId = 1),
                        task(3, today, parentTaskId = 1, isCompleted = true),
                    ),
            )

            coVerify { activityRepository.recordCounts(today, scheduledCount = 2, completedCount = 1) }
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

    @Test
    fun `recounting task progress is idempotent and does not write past days or minutes`() =
        runTest {
            val tasks = listOf(task(1, today, isCompleted = true), task(2, today))

            record(tasks)
            record(tasks)

            coVerify(exactly = 2) { activityRepository.recordCounts(today, 2, 1) }
            coVerify(exactly = 0) { activityRepository.recordCounts(neq(today), any(), any()) }
            coVerify(exactly = 0) { activityRepository.addFocusMinutes(any(), any()) }
            coVerify(exactly = 0) { activityRepository.replaceAll(any()) }
        }
}
