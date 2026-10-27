package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.core.domain.usecase.DailyActivityRecorder
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskAttachmentRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test

/** Completing, uncompleting and recurring a task never changes its section (#61). */
class ToggleTaskCompletionUseCaseSectionTest {
    private val dailyActivityRecorder = mockk<DailyActivityRecorder>(relaxed = true)
    private val taskAttachmentRepository = mockk<TaskAttachmentRepository>(relaxed = true)
    private lateinit var useCase: ToggleTaskCompletionUseCase
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskReminderScheduler: TaskReminderScheduler
    private lateinit var updateTaskUseCase: UpdateTaskUseCase

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        taskReminderScheduler = mockk(relaxed = true)
        updateTaskUseCase = mockk(relaxed = true)

        coEvery { updateTaskUseCase.invoke(any()) } returns
            UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped)

        // runInTransaction must execute its block; the relaxed mock would otherwise return null
        // and silently skip the entire periodic-completion / detachment flow.
        coEvery { taskRepository.runInTransaction<Any?>(any()) } coAnswers {
            val block = firstArg<suspend () -> Any?>()
            block()
        }
        coEvery { taskRepository.getTaskById(any()) } returns null
        coEvery { taskRepository.insertTask(any()) } returns 10L

        useCase =
            ToggleTaskCompletionUseCase(
                taskRepository,
                taskReminderScheduler,
                updateTaskUseCase,
                dailyActivityRecorder,
                taskAttachmentRepository,
            )
    }

    @Test
    fun `completing periodic task keeps the section on the next instance`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Water plants",
                    description = "",
                    periodicity = Periodicity.WEEKLY,
                    reminderDate = LocalDateTime(2030, 6, 15, 10, 0),
                    categoryId = 3,
                    sectionId = 8,
                )
            val insertedSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(insertedSlot)) } returns 10L
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(insertedSlot.captured.sectionId).isEqualTo(8L)
        }

    @Test
    fun `uncompleting a task leaves its section unchanged`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Buy litter",
                    description = "",
                    isCompleted = true,
                    completedAt = LocalDateTime(2024, 6, 15, 10, 0),
                    sectionId = 8,
                )
            val taskSlot = slot<Task>()
            coEvery { updateTaskUseCase.invoke(capture(taskSlot)) } returns
                UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped)
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(taskSlot.captured.sectionId).isEqualTo(8L)
        }
}
