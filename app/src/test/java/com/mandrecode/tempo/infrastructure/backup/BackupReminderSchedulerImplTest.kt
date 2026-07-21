package com.mandrecode.tempo.infrastructure.backup

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test

class BackupReminderSchedulerImplTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var habitRepository: HabitRepository
    private lateinit var habitChainRepository: HabitChainRepository
    private lateinit var taskReminderScheduler: TaskReminderScheduler
    private lateinit var habitReminderScheduler: HabitReminderScheduler
    private lateinit var workManager: WorkManager
    private lateinit var scheduler: BackupReminderSchedulerImpl

    @Before
    fun setUp() {
        taskRepository = mockk(relaxed = true)
        habitRepository = mockk(relaxed = true)
        habitChainRepository = mockk(relaxed = true)
        taskReminderScheduler = mockk(relaxed = true)
        habitReminderScheduler = mockk(relaxed = true)
        workManager = mockk(relaxed = true)
        scheduler =
            BackupReminderSchedulerImpl(
                taskRepository,
                habitRepository,
                habitChainRepository,
                taskReminderScheduler,
                habitReminderScheduler,
                workManager,
            )
    }

    @Test
    fun `cancelAllReminders cancels every task habit and chain with a reminder`() =
        runTest {
            val reminder = LocalDateTime(2099, 1, 1, 9, 0)
            val task = Task(id = 1, title = "T", description = "", reminderDate = reminder)
            val habit =
                Habit(
                    id = 2,
                    title = "H",
                    description = "",
                    reminderDate = reminder,
                    createdDate = reminder,
                )
            val chain = HabitChain(id = 3, title = "C", periodicReminder = reminder, createdDate = reminder)
            coEvery { taskRepository.getTasksWithReminders() } returns listOf(task)
            coEvery { habitRepository.getHabitsWithReminders() } returns listOf(habit)
            coEvery { habitChainRepository.getHabitChainsWithReminders() } returns listOf(chain)

            scheduler.cancelAllReminders()

            verify { taskReminderScheduler.cancel(task) }
            coVerify { habitReminderScheduler.cancelHabit(habit) }
            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
        }

    @Test
    fun `rescheduleAllReminders enqueues the reschedule worker as unique replace work`() {
        val name = slot<String>()
        val policy = slot<ExistingWorkPolicy>()
        scheduler.rescheduleAllReminders()

        verify {
            workManager.enqueueUniqueWork(capture(name), capture(policy), any<OneTimeWorkRequest>())
        }
        assertThat(name.captured).isEqualTo(BackupReminderSchedulerImpl.WORK_NAME)
        assertThat(policy.captured).isEqualTo(ExistingWorkPolicy.REPLACE)
    }
}
