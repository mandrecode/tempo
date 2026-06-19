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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

class CreateTaskUseCaseTest {
    private lateinit var useCase: CreateTaskUseCase
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskReminderScheduler: TaskReminderScheduler

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        taskReminderScheduler = mockk(relaxed = true)
        coEvery { taskRepository.getMaxSortOrder(any()) } returns 0
        coEvery { taskRepository.insertTask(any()) } returns 1L
        coEvery { taskReminderScheduler.schedule(any()) } returns ScheduleResult.Skipped

        useCase = CreateTaskUseCase(taskRepository, taskReminderScheduler)
    }

    @Test
    fun `creating task with future reminder and periodicity saves unchanged`() =
        runTest {
            val futureReminder = LocalDateTime(2099, 1, 1, 10, 0)
            val task = task(reminderDate = futureReminder, periodicity = Periodicity.DAILY)

            val taskSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(taskSlot)) } returns 1L

            val result = useCase(task)

            assertThat(taskSlot.captured.reminderDate).isEqualTo(futureReminder)
            assertThat((result as CreateTaskUseCase.Result.Success).reminderAdvanced).isFalse()
        }

    @Test
    fun `creating task with no reminder saves unchanged`() =
        runTest {
            val task = task(reminderDate = null, periodicity = Periodicity.WEEKLY)

            val taskSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(taskSlot)) } returns 1L

            useCase(task)

            assertThat(taskSlot.captured.reminderDate).isNull()
        }

    @Test
    fun `creating task with past reminder and no periodicity saves unchanged and sets flag`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)
            val task = task(reminderDate = pastReminder, periodicity = null)

            val taskSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(taskSlot)) } returns 1L

            val result = useCase(task)

            assertThat(taskSlot.captured.reminderDate).isEqualTo(pastReminder)
            assertThat((result as CreateTaskUseCase.Result.Success).pastReminderWithoutPeriodicity).isTrue()
            assertThat(result.reminderAdvanced).isFalse()
        }

    @Test
    fun `creating daily task with past reminder advances to future date`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)
            val task = task(reminderDate = pastReminder, periodicity = Periodicity.DAILY)

            val taskSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(taskSlot)) } returns 1L

            val result = useCase(task)

            val savedReminder = taskSlot.captured.reminderDate
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!! > now).isTrue()
            assertThat(savedReminder.hour).isEqualTo(pastReminder.hour)
            assertThat(savedReminder.minute).isEqualTo(pastReminder.minute)
            assertThat((result as CreateTaskUseCase.Result.Success).reminderAdvanced).isTrue()
        }

    @Test
    fun `creating weekly task with past reminder advances to future date`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)
            val task = task(reminderDate = pastReminder, periodicity = Periodicity.WEEKLY)

            val taskSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(taskSlot)) } returns 1L

            useCase(task)

            val savedReminder = taskSlot.captured.reminderDate
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!! > now).isTrue()
        }

    @Test
    fun `creating monthly task with past reminder advances to future date`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)
            val task = task(reminderDate = pastReminder, periodicity = Periodicity.MONTHLY)

            val taskSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(taskSlot)) } returns 1L

            useCase(task)

            val savedReminder = taskSlot.captured.reminderDate!!
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            assertThat(savedReminder > now).isTrue()
        }

    @Test
    fun `creating task with past reminder and periodicity schedules the alarm`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)
            val task = task(reminderDate = pastReminder, periodicity = Periodicity.DAILY)

            coEvery { taskReminderScheduler.schedule(any()) } returns
                ScheduleResult.Success(LocalDateTime(2099, 1, 2, 10, 0))

            useCase(task)

            coVerify { taskReminderScheduler.schedule(any()) }
        }

    @Test
    fun `creating task trims title and description before saving`() =
        runTest {
            val task = task(title = "  Padded Title  ", description = "  Padded Desc  ")

            val taskSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(taskSlot)) } returns 1L

            useCase(task)

            assertThat(taskSlot.captured.title).isEqualTo("Padded Title")
            assertThat(taskSlot.captured.description).isEqualTo("Padded Desc")
        }

    @Test
    fun `title validation returns error for empty title`() =
        runTest {
            val task = task(title = "")

            val result = useCase(task)

            assertThat(result).isInstanceOf(CreateTaskUseCase.Result.ValidationError::class.java)
            assertThat((result as CreateTaskUseCase.Result.ValidationError).type)
                .isEqualTo(CreateTaskUseCase.ValidationErrorType.TITLE_EMPTY)
        }

    @Test
    fun `creating subtask uses parent max sort order`() =
        runTest {
            coEvery { taskRepository.getMaxSubtaskSortOrder(10L) } returns 5

            val taskSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(taskSlot)) } returns 2L

            val subtask =
                Task(
                    title = "Subtask",
                    description = "",
                    parentTaskId = 10L,
                )

            val result = useCase(subtask)

            assertThat(result).isInstanceOf(CreateTaskUseCase.Result.Success::class.java)
            assertThat(taskSlot.captured.sortOrder).isEqualTo(6)
            coVerify(exactly = 0) { taskRepository.getMaxSortOrder(any()) }
            coVerify { taskRepository.getMaxSubtaskSortOrder(10L) }
        }

    private fun task(
        title: String = "Test Task",
        description: String = "",
        reminderDate: LocalDateTime? = null,
        periodicity: Periodicity? = null,
    ) = Task(
        title = title,
        description = description,
        reminderDate = reminderDate,
        periodicity = periodicity,
    )
}
