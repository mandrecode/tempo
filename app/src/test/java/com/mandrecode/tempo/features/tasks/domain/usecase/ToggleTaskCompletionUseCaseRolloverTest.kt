package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
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

/**
 * Dedicated coverage for next-instance rollover linking and stale-link replacement.
 *
 * Non-rollover toggle behavior remains in [ToggleTaskCompletionUseCaseTest].
 */
class ToggleTaskCompletionUseCaseRolloverTest {
    private lateinit var useCase: ToggleTaskCompletionUseCase
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskReminderScheduler: TaskReminderScheduler
    private lateinit var updateTaskUseCase: UpdateTaskUseCase

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        taskReminderScheduler = mockk(relaxed = true)
        updateTaskUseCase = mockk(relaxed = true)

        coEvery { taskRepository.runInTransaction<Any?>(any()) } coAnswers {
            val block = firstArg<suspend () -> Any?>()
            block()
        }
        coEvery { taskRepository.getTaskById(any()) } returns null

        useCase =
            ToggleTaskCompletionUseCase(
                taskRepository,
                taskReminderScheduler,
                updateTaskUseCase,
            )
    }

    @Test
    fun `completing periodic task with existing nextInstanceId reuses spawned next instance`() =
        runTest {
            val task = periodicTask(nextInstanceId = 42L)
            val existingNextInstance =
                periodicTask(
                    id = 42L,
                    reminderDate = LocalDateTime(2024, 6, 16, 10, 0),
                    nextInstanceId = null,
                )
            val scheduledSlot = slot<Task>()
            coEvery { taskRepository.getTaskById(42L) } returns existingNextInstance
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()
            coEvery { taskReminderScheduler.schedule(capture(scheduledSlot)) } answers {
                ScheduleResult.Success(scheduledSlot.captured.reminderDate!!)
            }

            useCase(task)

            coVerify(exactly = 0) { taskRepository.insertTask(any()) }
            coVerify { taskRepository.updateTaskNextInstanceId(1L, 42L) }
            assertThat(scheduledSlot.captured).isEqualTo(existingNextInstance)
        }

    @Test
    fun `completing periodic task re-reads current nextInstanceId before inserting next instance`() =
        runTest {
            val staleInput = periodicTask(nextInstanceId = null)
            val currentTask = staleInput.copy(nextInstanceId = 42L)
            val existingNextInstance =
                periodicTask(
                    id = 42L,
                    reminderDate = LocalDateTime(2024, 6, 16, 10, 0),
                    nextInstanceId = null,
                )
            coEvery { taskRepository.getTaskById(1L) } returns currentTask
            coEvery { taskRepository.getTaskById(42L) } returns existingNextInstance
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()

            useCase(staleInput)

            coVerify(exactly = 0) { taskRepository.insertTask(any()) }
            coVerify { taskRepository.updateTaskNextInstanceId(1L, 42L) }
            coVerify { taskReminderScheduler.schedule(existingNextInstance) }
        }

    @Test
    fun `completing periodic task with stale nextInstanceId creates replacement next instance`() =
        runTest {
            val task = periodicTask(nextInstanceId = 99L)
            coEvery { taskRepository.getTaskById(99L) } returns null
            coEvery { taskRepository.insertTask(any()) } returns 42L
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()

            useCase(task)

            coVerify { taskRepository.updateTaskNextInstanceId(1L, null) }
            coVerify { taskRepository.updateTaskNextInstanceId(1L, 42L) }
            coVerify { taskRepository.insertTask(any()) }
        }

    @Test
    fun `completing periodic task with completed nextInstanceId creates replacement next instance`() =
        runTest {
            val task = periodicTask(nextInstanceId = 99L)
            val completedNextInstance =
                periodicTask(
                    id = 99L,
                    reminderDate = LocalDateTime(2024, 6, 16, 10, 0),
                    nextInstanceId = null,
                ).copy(
                    isCompleted = true,
                    periodicity = null,
                )
            coEvery { taskRepository.getTaskById(99L) } returns completedNextInstance
            coEvery { taskRepository.insertTask(any()) } returns 42L
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()

            useCase(task)

            coVerify { taskRepository.updateTaskNextInstanceId(1L, null) }
            coVerify { taskRepository.updateTaskNextInstanceId(1L, 42L) }
            coVerify { taskRepository.insertTask(any()) }
            coVerify(exactly = 0) { taskReminderScheduler.schedule(completedNextInstance) }
        }

    private fun periodicTask(
        id: Long = 1L,
        reminderDate: LocalDateTime = LocalDateTime(2024, 6, 15, 10, 0),
        nextInstanceId: Long? = null,
    ): Task =
        Task(
            id = id,
            title = "Periodic",
            description = "",
            isCompleted = false,
            periodicity = Periodicity.DAILY,
            reminderDate = reminderDate,
            nextInstanceId = nextInstanceId,
        )
}
