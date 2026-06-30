package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.MonthDayOption
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test

class RollOverduePeriodicTaskUseCaseTest {
    private lateinit var useCase: RollOverduePeriodicTaskUseCase
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskReminderScheduler: TaskReminderScheduler

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        taskReminderScheduler = mockk(relaxed = true)

        coEvery { taskRepository.runInTransaction<Any?>(any()) } coAnswers {
            val block = firstArg<suspend () -> Any?>()
            block()
        }
        coEvery { taskRepository.insertTask(any()) } returns 10L

        useCase = RollOverduePeriodicTaskUseCase(taskRepository, taskReminderScheduler)
    }

    @Test
    fun `given overdue periodic task when invoked then preserves original and creates next instance`() =
        runTest {
            val now = LocalDateTime(2026, 4, 25, 10, 0)
            val overdueTask = overdueTask()
            val insertedSlot = slot<Task>()
            val scheduledSlot = slot<Task>()
            val updatedOriginalSlot = slot<Task>()
            coEvery { taskRepository.getTaskById(1L) } returns overdueTask
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()
            coEvery { taskRepository.insertTask(capture(insertedSlot)) } returns 10L
            coEvery { taskReminderScheduler.schedule(capture(scheduledSlot)) } answers {
                ScheduleResult.Success(scheduledSlot.captured.reminderDate!!)
            }

            val result = useCase(overdueTask, now)

            assertThat(result).isInstanceOf(RollOverduePeriodicTaskUseCase.Result.CreatedNextInstance::class.java)
            coVerify(exactly = 0) { taskRepository.updateTaskReminderDate(overdueTask.id, any()) }
            coVerify { taskRepository.updateTaskNextInstanceId(1L, 10L) }
            coVerify { taskRepository.updateTask(capture(updatedOriginalSlot)) }
            coVerify(exactly = 0) { taskReminderScheduler.cancel(overdueTask.copy(nextInstanceId = 10L)) }
            assertRolledOverOriginal(
                updated = updatedOriginalSlot.captured,
                original = overdueTask,
                expectedNextInstanceId = 10L,
            )
            assertThat(insertedSlot.captured.id).isEqualTo(0L)
            assertThat(insertedSlot.captured.isCompleted).isFalse()
            assertThat(insertedSlot.captured.periodicity).isEqualTo(Periodicity.DAILY)
            assertThat(insertedSlot.captured.reminderDate).isGreaterThan(now)
            assertThat(scheduledSlot.captured.id).isEqualTo(10L)
            assertThat(scheduledSlot.captured.reminderDate).isEqualTo(insertedSlot.captured.reminderDate)
        }

    @Test
    fun `given existing linked next instance when invoked then reuses it`() =
        runTest {
            val now = LocalDateTime(2026, 4, 25, 10, 0)
            val overdueTask = overdueTask(nextInstanceId = 10L)
            val nextInstance =
                overdueTask.copy(
                    id = 10L,
                    reminderDate = LocalDateTime(2026, 4, 26, 9, 0),
                    nextInstanceId = null,
                )
            val scheduledSlot = slot<Task>()
            val updatedOriginalSlot = slot<Task>()
            coEvery { taskRepository.getTaskById(1L) } returns overdueTask
            coEvery { taskRepository.getTaskById(10L) } returns nextInstance
            coEvery { taskReminderScheduler.schedule(capture(scheduledSlot)) } answers {
                ScheduleResult.Success(scheduledSlot.captured.reminderDate!!)
            }

            val result = useCase(overdueTask, now)

            assertThat(result).isInstanceOf(RollOverduePeriodicTaskUseCase.Result.ReusedNextInstance::class.java)
            coVerify(exactly = 0) { taskRepository.insertTask(any()) }
            coVerify(exactly = 0) { taskRepository.updateTaskNextInstanceId(1L, any()) }
            coVerify { taskRepository.updateTask(capture(updatedOriginalSlot)) }
            assertRolledOverOriginal(
                updated = updatedOriginalSlot.captured,
                original = overdueTask,
                expectedNextInstanceId = 10L,
            )
            assertThat(scheduledSlot.captured).isEqualTo(nextInstance)
        }

    @Test
    fun `given stale linked next instance when invoked then clears link and creates replacement`() =
        runTest {
            val now = LocalDateTime(2026, 4, 25, 10, 0)
            val overdueTask = overdueTask(nextInstanceId = 99L)
            val updatedOriginalSlot = slot<Task>()
            coEvery { taskRepository.getTaskById(1L) } returns overdueTask
            coEvery { taskRepository.getTaskById(99L) } returns null
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()

            val result = useCase(overdueTask, now)

            assertThat(result).isInstanceOf(RollOverduePeriodicTaskUseCase.Result.CreatedNextInstance::class.java)
            coVerify { taskRepository.updateTaskNextInstanceId(1L, null) }
            coVerify { taskRepository.updateTaskNextInstanceId(1L, 10L) }
            coVerify { taskRepository.updateTask(capture(updatedOriginalSlot)) }
            assertRolledOverOriginal(
                updated = updatedOriginalSlot.captured,
                original = overdueTask,
                expectedNextInstanceId = 10L,
            )
            coVerify { taskRepository.insertTask(any()) }
        }

    @Test
    fun `given completed linked next instance when invoked then clears link and creates replacement`() =
        runTest {
            val now = LocalDateTime(2026, 4, 25, 10, 0)
            val overdueTask = overdueTask(nextInstanceId = 99L)
            val updatedOriginalSlot = slot<Task>()
            val completedNextInstance =
                overdueTask.copy(
                    id = 99L,
                    isCompleted = true,
                    periodicity = null,
                    reminderDate = LocalDateTime(2026, 4, 26, 9, 0),
                    nextInstanceId = null,
                )
            coEvery { taskRepository.getTaskById(1L) } returns overdueTask
            coEvery { taskRepository.getTaskById(99L) } returns completedNextInstance
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()

            val result = useCase(overdueTask, now)

            assertThat(result).isInstanceOf(RollOverduePeriodicTaskUseCase.Result.CreatedNextInstance::class.java)
            coVerify { taskRepository.updateTaskNextInstanceId(1L, null) }
            coVerify { taskRepository.updateTaskNextInstanceId(1L, 10L) }
            coVerify { taskRepository.updateTask(capture(updatedOriginalSlot)) }
            assertRolledOverOriginal(
                updated = updatedOriginalSlot.captured,
                original = overdueTask,
                expectedNextInstanceId = 10L,
            )
            coVerify { taskRepository.insertTask(any()) }
            coVerify(exactly = 0) { taskReminderScheduler.schedule(completedNextInstance) }
        }

    @Test
    fun `given next instance insert fails when invoked then does not schedule invalid task`() =
        runTest {
            val now = LocalDateTime(2026, 4, 25, 10, 0)
            val overdueTask = overdueTask()
            coEvery { taskRepository.getTaskById(1L) } returns overdueTask
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()
            coEvery { taskRepository.insertTask(any()) } returns 0L

            val result = useCase(overdueTask, now)

            assertThat(result).isEqualTo(RollOverduePeriodicTaskUseCase.Result.FailedToCreate)
            coVerify(exactly = 0) { taskRepository.updateTaskNextInstanceId(any(), any()) }
            coVerify(exactly = 0) { taskRepository.updateTask(any()) }
            coVerify(exactly = 0) { taskReminderScheduler.schedule(any()) }
        }

    @Test
    fun `given future periodic task when invoked then returns not applicable`() =
        runTest {
            val now = LocalDateTime(2026, 4, 25, 10, 0)
            val task = overdueTask(reminderDate = LocalDateTime(2026, 4, 26, 9, 0))

            val result = useCase(task, now)

            assertThat(result).isEqualTo(RollOverduePeriodicTaskUseCase.Result.NotApplicable)
            coVerify(exactly = 0) { taskRepository.runInTransaction<Any?>(any()) }
            coVerify(exactly = 0) { taskRepository.updateTask(any()) }
        }

    @Test
    fun `given task becomes ineligible inside transaction when invoked then returns not applicable`() =
        runTest {
            val now = LocalDateTime(2026, 4, 25, 10, 0)
            val staleInput = overdueTask()
            val completedCurrentTask = staleInput.copy(isCompleted = true)
            coEvery { taskRepository.getTaskById(1L) } returns completedCurrentTask

            val result = useCase(staleInput, now)

            assertThat(result).isEqualTo(RollOverduePeriodicTaskUseCase.Result.NotApplicable)
            coVerify(exactly = 0) { taskRepository.insertTask(any()) }
            coVerify(exactly = 0) { taskRepository.updateTask(any()) }
            coVerify(exactly = 0) { taskReminderScheduler.schedule(any()) }
        }

    @Test
    fun `given completed overdue periodic task when invoked then returns not applicable without rollover`() =
        runTest {
            val now = LocalDateTime(2026, 4, 25, 10, 0)
            val completedTask = overdueTask().copy(isCompleted = true)

            val result = useCase(completedTask, now)

            assertThat(result).isEqualTo(RollOverduePeriodicTaskUseCase.Result.NotApplicable)
            coVerify(exactly = 0) { taskRepository.runInTransaction<Any?>(any()) }
            coVerify(exactly = 0) { taskRepository.insertTask(any()) }
            coVerify(exactly = 0) { taskRepository.updateTask(any()) }
            coVerify(exactly = 0) { taskReminderScheduler.schedule(any()) }
        }

    @Test
    fun `given overdue periodic task with subtasks when invoked then clones inactive subtasks under next instance`() =
        runTest {
            val now = LocalDateTime(2026, 4, 25, 10, 0)
            val overdueTask = overdueTask()
            val subtask =
                Task(
                    id = 2L,
                    title = "Subtask",
                    description = "",
                    isCompleted = true,
                    parentTaskId = 1L,
                    reminderDate = LocalDateTime(2026, 4, 24, 9, 0),
                    periodicity = Periodicity.DAILY,
                    sortOrder = 3,
                )
            val insertedSubtasksSlot = slot<List<Task>>()
            coEvery { taskRepository.getTaskById(1L) } returns overdueTask
            coEvery { taskRepository.getSubtasksSync(1L) } returns listOf(subtask)
            coEvery { taskRepository.getMaxSubtaskSortOrder(10L) } returns 0
            coEvery { taskRepository.insertTasks(capture(insertedSubtasksSlot)) } returns listOf(11L)

            useCase(overdueTask, now)

            val clone = insertedSubtasksSlot.captured.single()
            assertThat(clone.id).isEqualTo(0L)
            assertThat(clone.parentTaskId).isEqualTo(10L)
            assertThat(clone.isCompleted).isFalse()
            assertThat(clone.completedAt).isNull()
            assertThat(clone.reminderDate).isNull()
            assertThat(clone.periodicity).isNull()
        }

    @Test
    fun `given overdue periodic task when next date is still past then inserted reminder is advanced into future`() =
        runTest {
            val now = LocalDateTime(2026, 4, 25, 10, 0)
            val overdueTask = overdueTask(reminderDate = LocalDateTime(2024, 1, 1, 9, 0))
            val insertedSlot = slot<Task>()
            coEvery { taskRepository.getTaskById(1L) } returns overdueTask
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()
            coEvery { taskRepository.insertTask(capture(insertedSlot)) } returns 10L

            useCase(overdueTask, now)

            assertThat(insertedSlot.captured.reminderDate).isGreaterThan(now)
        }

    private fun overdueTask(
        reminderDate: LocalDateTime = LocalDateTime(2026, 4, 24, 9, 0),
        nextInstanceId: Long? = null,
    ): Task =
        Task(
            id = 1L,
            title = "Daily task",
            description = "",
            isCompleted = false,
            reminderDate = reminderDate,
            periodicity = Periodicity.DAILY,
            periodicityInterval = 3,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            monthDayOption = MonthDayOption.LAST_DAY,
            nextInstanceId = nextInstanceId,
        )

    private fun assertRolledOverOriginal(
        updated: Task,
        original: Task,
        expectedNextInstanceId: Long,
    ) {
        assertThat(updated.id).isEqualTo(original.id)
        assertThat(updated.isCompleted).isFalse()
        assertThat(updated.reminderDate).isEqualTo(original.reminderDate)
        assertThat(updated.nextInstanceId).isEqualTo(expectedNextInstanceId)
        assertThat(updated.periodicity).isNull()
        assertThat(updated.periodicityInterval).isEqualTo(1)
        assertThat(updated.repeatDays).isNull()
        assertThat(updated.monthDayOption).isNull()
    }
}
