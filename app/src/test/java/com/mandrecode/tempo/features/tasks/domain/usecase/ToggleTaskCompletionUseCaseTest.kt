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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

/**
 * Core toggle behavior coverage (parent/subtask completion, reminders, periodic archiving).
 *
 * Rollover-link edge cases are intentionally isolated in
 * [ToggleTaskCompletionUseCaseRolloverTest] to keep intent-specific assertions separate.
 */
class ToggleTaskCompletionUseCaseTest {
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
        // and silently skip the entire periodic-completion / rollback flow.
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
            )
    }

    @Test
    fun `completing parent task sets completedAt`() =
        runTest {
            val task = Task(id = 1, title = "Test", description = "", isCompleted = false)

            val taskSlot = slot<Task>()
            coEvery { updateTaskUseCase.invoke(capture(taskSlot)) } returns
                UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped)
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(taskSlot.captured.isCompleted).isTrue()
            assertThat(taskSlot.captured.completedAt).isNotNull()
        }

    @Test
    fun `uncompleting parent task clears completedAt`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Test",
                    description = "",
                    isCompleted = true,
                    completedAt = LocalDateTime(2024, 6, 15, 10, 0),
                )

            val taskSlot = slot<Task>()
            coEvery { updateTaskUseCase.invoke(capture(taskSlot)) } returns
                UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped)
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(taskSlot.captured.isCompleted).isFalse()
            assertThat(taskSlot.captured.completedAt).isNull()
        }

    @Test
    fun `completing parent task sets completedAt on subtasks`() =
        runTest {
            val task = Task(id = 1, title = "Parent", description = "", isCompleted = false)
            val subtask = Task(id = 2, title = "Sub", description = "", parentTaskId = 1)

            coEvery { taskRepository.getSubtasksSync(1) } returns listOf(subtask)

            useCase(task)

            coVerify {
                taskRepository.updateSubtasksCompletion(1, true, withArg { assertThat(it).isNotNull() })
            }
        }

    @Test
    fun `uncompleting parent task clears completedAt on subtasks`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Parent",
                    description = "",
                    isCompleted = true,
                    completedAt = LocalDateTime(2024, 6, 15, 10, 0),
                )
            val subtask = Task(id = 2, title = "Sub", description = "", parentTaskId = 1)

            coEvery { taskRepository.getSubtasksSync(1) } returns listOf(subtask)

            useCase(task)

            coVerify {
                taskRepository.updateSubtasksCompletion(1, false, null)
            }
        }

    @Test
    fun `completing subtask sets completedAt`() =
        runTest {
            val subtask =
                Task(
                    id = 2,
                    title = "Sub",
                    description = "",
                    isCompleted = false,
                    parentTaskId = 1,
                )

            val taskSlot = slot<Task>()
            coEvery { updateTaskUseCase.invoke(capture(taskSlot)) } returns
                UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped)

            useCase(subtask)

            assertThat(taskSlot.captured.isCompleted).isTrue()
            assertThat(taskSlot.captured.completedAt).isNotNull()
        }

    @Test
    fun `completing parent task cancels subtask reminders`() =
        runTest {
            val task = Task(id = 1, title = "Parent", description = "", isCompleted = false)
            val subtaskWithReminder =
                Task(
                    id = 2,
                    title = "Sub with reminder",
                    description = "",
                    parentTaskId = 1,
                    reminderDate = LocalDateTime(2025, 6, 15, 10, 0),
                )
            val subtaskWithoutReminder =
                Task(
                    id = 3,
                    title = "Sub no reminder",
                    description = "",
                    parentTaskId = 1,
                )

            coEvery { taskRepository.getSubtasksSync(1) } returns
                listOf(subtaskWithReminder, subtaskWithoutReminder)

            useCase(task)

            coVerify { taskReminderScheduler.cancel(subtaskWithReminder) }
            coVerify(exactly = 0) { taskReminderScheduler.cancel(subtaskWithoutReminder) }
        }

    @Test
    fun `uncompleting parent task reschedules subtask reminders`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Parent",
                    description = "",
                    isCompleted = true,
                    completedAt = LocalDateTime(2024, 6, 15, 10, 0),
                )
            val subtaskWithReminder =
                Task(
                    id = 2,
                    title = "Sub with reminder",
                    description = "",
                    parentTaskId = 1,
                    reminderDate = LocalDateTime(2025, 6, 15, 10, 0),
                )

            coEvery { taskRepository.getSubtasksSync(1) } returns listOf(subtaskWithReminder)

            useCase(task)

            coVerify { taskReminderScheduler.schedule(subtaskWithReminder) }
            coVerify(exactly = 0) { taskReminderScheduler.cancel(subtaskWithReminder) }
        }

    @Test
    fun `completing periodic task cancels subtask reminders`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Periodic",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.DAILY,
                    reminderDate = LocalDateTime(2030, 6, 15, 10, 0),
                )
            val subtaskWithReminder =
                Task(
                    id = 2,
                    title = "Sub with reminder",
                    description = "",
                    parentTaskId = 1,
                    reminderDate = LocalDateTime(2025, 6, 15, 10, 0),
                )

            coEvery { taskRepository.getSubtasksSync(1) } returns listOf(subtaskWithReminder)

            useCase(task)

            coVerify { taskReminderScheduler.cancel(subtaskWithReminder) }
        }

    @Test
    fun `completing periodic task only stamps incomplete subtasks`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Periodic",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.DAILY,
                    reminderDate = LocalDateTime(2030, 6, 15, 10, 0),
                )

            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            coVerify {
                taskRepository.completeIncompleteSubtasks(1, withArg { assertThat(it).isNotNull() })
            }
            coVerify(exactly = 0) { taskRepository.updateSubtasksCompletion(1, true, any()) }
        }

    @Test
    fun `completing periodic task sets completedAt and clears on next instance`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Periodic",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.DAILY,
                    reminderDate = LocalDateTime(2030, 6, 15, 10, 0),
                )

            val updatedSlot = slot<Task>()
            val insertedSlot = slot<Task>()
            coEvery { taskRepository.updateTask(capture(updatedSlot)) } returns Unit
            coEvery { taskRepository.insertTask(capture(insertedSlot)) } returns 10L
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            // Archived task should have completedAt set
            assertThat(updatedSlot.captured.isCompleted).isTrue()
            assertThat(updatedSlot.captured.completedAt).isNotNull()

            // Next instance should be active and have completedAt cleared
            assertThat(insertedSlot.captured.completedAt).isNull()
            assertThat(insertedSlot.captured.isCompleted).isFalse()
        }

    @Test
    fun `completing periodic task creates next instance with correct reminderDate`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Periodic",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.DAILY,
                    reminderDate = LocalDateTime(2030, 6, 15, 10, 0),
                )

            val insertedSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(insertedSlot)) } returns 10L
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(insertedSlot.captured.reminderDate?.date)
                .isEqualTo(kotlinx.datetime.LocalDate(2030, 6, 16))
            assertThat(insertedSlot.captured.reminderDate?.hour).isEqualTo(10)
            assertThat(insertedSlot.captured.reminderDate?.minute).isEqualTo(0)
            assertThat(insertedSlot.captured.periodicity).isEqualTo(Periodicity.DAILY)
        }

    @Test
    fun `completing periodic task with stale reminder advances next instance reminder into future`() =
        runTest {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val task =
                Task(
                    id = 1,
                    title = "Stale periodic",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.DAILY,
                    reminderDate = LocalDateTime(2024, 1, 1, 10, 0),
                )

            val insertedSlot = slot<Task>()
            val scheduledSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(insertedSlot)) } returns 10L
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()
            coEvery { taskReminderScheduler.schedule(capture(scheduledSlot)) } answers {
                ScheduleResult.Success(scheduledSlot.captured.reminderDate!!)
            }

            useCase(task)

            assertThat(insertedSlot.captured.reminderDate).isGreaterThan(now)
            assertThat(insertedSlot.captured.reminderDate).isEqualTo(scheduledSlot.captured.reminderDate)
        }

    @Test
    fun `completing periodic task clears periodicity on completed instance`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Periodic",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.DAILY,
                    reminderDate = LocalDateTime(2030, 6, 15, 10, 0),
                )

            val updatedSlot = slot<Task>()
            coEvery { taskRepository.updateTask(capture(updatedSlot)) } returns Unit
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(updatedSlot.captured.periodicity).isNull()
        }

    @Test
    fun `completing periodic task with interval 3 creates next instance 3 days ahead`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Every 3 days",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.DAILY,
                    periodicityInterval = 3,
                    reminderDate = LocalDateTime(2030, 6, 15, 10, 0),
                )

            val insertedSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(insertedSlot)) } returns 10L
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(insertedSlot.captured.reminderDate?.date)
                .isEqualTo(kotlinx.datetime.LocalDate(2030, 6, 18))
            assertThat(insertedSlot.captured.periodicityInterval).isEqualTo(3)
        }

    @Test
    fun `completing periodic task strips all recurrence fields from completed instance`() =
        runTest {
            val days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
            val task =
                Task(
                    id = 1,
                    title = "Complex Periodic",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.WEEKLY,
                    periodicityInterval = 2,
                    repeatDays = days,
                    reminderDate = LocalDateTime(2024, 6, 10, 10, 0),
                )

            val updatedSlot = slot<Task>()
            coEvery { taskRepository.updateTask(capture(updatedSlot)) } returns Unit
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(updatedSlot.captured.periodicity).isNull()
            assertThat(updatedSlot.captured.periodicityInterval).isEqualTo(1)
            assertThat(updatedSlot.captured.repeatDays).isNull()
            assertThat(updatedSlot.captured.monthDayOption).isNull()
        }

    @Test
    fun `completing periodic task preserves recurrence fields on next instance`() =
        runTest {
            val days = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
            val task =
                Task(
                    id = 1,
                    title = "Weekly MWF",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.WEEKLY,
                    periodicityInterval = 2,
                    repeatDays = days,
                    reminderDate = LocalDateTime(2024, 6, 10, 10, 0),
                )

            val insertedSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(insertedSlot)) } returns 10L
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(insertedSlot.captured.periodicity).isEqualTo(Periodicity.WEEKLY)
            assertThat(insertedSlot.captured.periodicityInterval).isEqualTo(2)
            assertThat(insertedSlot.captured.repeatDays).isEqualTo(days)
        }

    @Test
    fun `completing monthly periodic with FIRST_DAY creates next on 1st`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Monthly first",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.MONTHLY,
                    monthDayOption = MonthDayOption.FIRST_DAY,
                    reminderDate = LocalDateTime(2030, 6, 1, 10, 0),
                )

            val insertedSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(insertedSlot)) } returns 10L
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(insertedSlot.captured.reminderDate?.date)
                .isEqualTo(kotlinx.datetime.LocalDate(2030, 7, 1))
            assertThat(insertedSlot.captured.monthDayOption).isEqualTo(MonthDayOption.FIRST_DAY)
        }

    @Test
    fun `completing monthly periodic with LAST_DAY creates next on last day`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Monthly last",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.MONTHLY,
                    monthDayOption = MonthDayOption.LAST_DAY,
                    reminderDate = LocalDateTime(2030, 6, 30, 10, 0),
                )

            val insertedSlot = slot<Task>()
            coEvery { taskRepository.insertTask(capture(insertedSlot)) } returns 10L
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            assertThat(insertedSlot.captured.reminderDate?.date)
                .isEqualTo(kotlinx.datetime.LocalDate(2030, 7, 31))
            assertThat(insertedSlot.captured.monthDayOption).isEqualTo(MonthDayOption.LAST_DAY)
        }

    // ── New tests: periodic completion link + rollback ─────────────────────────────────

    @Test
    fun `completing periodic task links archived task to spawned next instance`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Periodic",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.DAILY,
                    reminderDate = LocalDateTime(2024, 6, 15, 10, 0),
                )

            val archivedSlot = slot<Task>()
            coEvery { taskRepository.insertTask(any()) } returns 42L
            coEvery { taskRepository.getSubtasksSync(1) } returns emptyList()

            useCase(task)

            coVerify { taskRepository.updateTask(capture(archivedSlot)) }
            assertThat(archivedSlot.captured.nextInstanceId).isNull()
            coVerify { taskRepository.updateTaskNextInstanceId(1L, 42L) }
        }

    @Test
    fun `unchecking archived task with nextInstanceId rolls back the spawn`() =
        runTest {
            val archivedTask =
                Task(
                    id = 1,
                    title = "Archived",
                    description = "",
                    isCompleted = true,
                    completedAt = LocalDateTime(2024, 6, 15, 10, 0),
                    nextInstanceId = 42L,
                )
            val nextInstance =
                Task(
                    id = 42,
                    title = "Archived",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.DAILY,
                    periodicityInterval = 1,
                    reminderDate = LocalDateTime(2030, 6, 16, 10, 0),
                )

            coEvery { taskRepository.getTaskById(42L) } returns nextInstance
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()

            val result = useCase(archivedTask)

            assertThat(result).isInstanceOf(ToggleTaskCompletionUseCase.Result.PeriodicRolledBack::class.java)
            // Spawn deleted
            coVerify { taskRepository.deleteTaskWithSubtasks(42L) }
            // Archived task restored — recurrence copied from next instance
            val restoredSlot = slot<Task>()
            coVerify { taskRepository.updateTask(capture(restoredSlot)) }
            assertThat(restoredSlot.captured.id).isEqualTo(1L)
            assertThat(restoredSlot.captured.isCompleted).isFalse()
            assertThat(restoredSlot.captured.completedAt).isNull()
            assertThat(restoredSlot.captured.nextInstanceId).isNull()
            assertThat(restoredSlot.captured.periodicity).isEqualTo(Periodicity.DAILY)
            assertThat(restoredSlot.captured.reminderDate).isEqualTo(nextInstance.reminderDate)
            // New instance reminder cancelled
            coVerify { taskReminderScheduler.cancel(nextInstance) }
        }

    @Test
    fun `rollback with stale next instance reminder restores future reminder`() =
        runTest {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val archivedTask =
                Task(
                    id = 1,
                    title = "Archived",
                    description = "",
                    isCompleted = true,
                    completedAt = LocalDateTime(2024, 6, 15, 10, 0),
                    nextInstanceId = 42L,
                )
            val nextInstance =
                Task(
                    id = 42,
                    title = "Archived",
                    description = "",
                    isCompleted = false,
                    periodicity = Periodicity.DAILY,
                    reminderDate = LocalDateTime(2024, 1, 1, 10, 0),
                )

            val restoredSlot = slot<Task>()
            val scheduledSlot = slot<Task>()
            coEvery { taskRepository.getTaskById(42L) } returns nextInstance
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()
            coEvery { taskRepository.updateTask(capture(restoredSlot)) } returns Unit
            coEvery { taskReminderScheduler.schedule(capture(scheduledSlot)) } answers {
                ScheduleResult.Success(scheduledSlot.captured.reminderDate!!)
            }

            useCase(archivedTask)

            assertThat(restoredSlot.captured.reminderDate).isGreaterThan(now)
            assertThat(restoredSlot.captured.reminderDate).isEqualTo(scheduledSlot.captured.reminderDate)
        }

    @Test
    fun `unchecking archived task without nextInstanceId follows normal uncheck path`() =
        runTest {
            val archivedTask =
                Task(
                    id = 1,
                    title = "Plain completed",
                    description = "",
                    isCompleted = true,
                    completedAt = LocalDateTime(2024, 6, 15, 10, 0),
                    nextInstanceId = null,
                )
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()

            val result = useCase(archivedTask)

            assertThat(result).isInstanceOf(ToggleTaskCompletionUseCase.Result.ParentToggled::class.java)
            coVerify(exactly = 0) { taskRepository.deleteTaskWithSubtasks(any()) }
        }

    @Test
    fun `rollback restores subtasks completed by the toggle`() =
        runTest {
            val archivedAt = LocalDateTime(2024, 6, 15, 10, 0)
            val archivedTask =
                Task(
                    id = 1,
                    title = "Archived",
                    description = "",
                    isCompleted = true,
                    completedAt = archivedAt,
                    nextInstanceId = 42L,
                )
            val autoCompletedSubtask =
                Task(
                    id = 2,
                    title = "Auto-completed",
                    description = "",
                    parentTaskId = 1,
                    isCompleted = true,
                    completedAt = archivedAt, // marked at the same instant as the parent toggle
                )
            val previouslyCompletedSubtask =
                Task(
                    id = 3,
                    title = "Was already done",
                    description = "",
                    parentTaskId = 1,
                    isCompleted = true,
                    completedAt = LocalDateTime(2024, 6, 1, 9, 0),
                )
            val laterCompletedSubtask =
                Task(
                    id = 4,
                    title = "Completed later",
                    description = "",
                    parentTaskId = 1,
                    isCompleted = true,
                    completedAt = LocalDateTime(2024, 6, 15, 10, 1),
                )

            coEvery { taskRepository.getTaskById(42L) } returns
                Task(id = 42, title = "next", description = "", periodicity = Periodicity.DAILY)
            coEvery { taskRepository.getSubtasksSync(1L) } returns
                listOf(autoCompletedSubtask, previouslyCompletedSubtask, laterCompletedSubtask)

            useCase(archivedTask)

            // Auto-completed subtask should be restored to active state (one of the updateTask calls)
            coVerify {
                taskRepository.updateTask(
                    match { it.id == 2L && !it.isCompleted && it.completedAt == null },
                )
            }
            // Previously-completed subtask must NOT be touched
            coVerify(exactly = 0) {
                taskRepository.updateTask(match { it.id == 3L })
            }
            // Subtasks completed after the parent archive must also be preserved.
            coVerify(exactly = 0) {
                taskRepository.updateTask(match { it.id == 4L })
            }
        }

    @Test
    fun `re-completing an archived task clears stale nextInstanceId`() =
        runTest {
            // First uncheck without rollback (we simulate by passing a task with no nextInstanceId
            // even though it was originally archived). Then user completes it again — stale link
            // must be cleared on the way to "completed".
            val archivedTask =
                Task(
                    id = 1,
                    title = "Archived again",
                    description = "",
                    isCompleted = false,
                    completedAt = null,
                    nextInstanceId = 99L, // stale link
                )

            val updatedSlot = slot<Task>()
            coEvery { updateTaskUseCase.invoke(capture(updatedSlot)) } returns
                UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped)
            coEvery { taskRepository.getSubtasksSync(1L) } returns emptyList()

            useCase(archivedTask)

            assertThat(updatedSlot.captured.isCompleted).isTrue()
            assertThat(updatedSlot.captured.nextInstanceId).isNull()
        }
}
