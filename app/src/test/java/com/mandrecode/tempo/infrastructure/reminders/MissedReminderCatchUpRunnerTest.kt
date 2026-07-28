package com.mandrecode.tempo.infrastructure.reminders

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.MissedReminderPreferences
import com.mandrecode.tempo.features.tasks.domain.scheduler.MissedReminderScheduler
import com.mandrecode.tempo.features.tasks.domain.usecase.GetOverdueIncompleteTasksUseCase
import com.mandrecode.tempo.infrastructure.notifications.TaskReminderNotifier
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

class MissedReminderCatchUpRunnerTest {
    private val enabled = MutableStateFlow(true)
    private val getOverdueIncompleteTasks: GetOverdueIncompleteTasksUseCase = mockk()
    private val notifier: TaskReminderNotifier = mockk(relaxed = true)
    private val scheduler: MissedReminderScheduler = mockk(relaxed = true)

    private val now = LocalDateTime(2026, 7, 28, 9, 0)

    private val preferences =
        object : MissedReminderPreferences {
            override val isEnabled = enabled
            override val catchUpTime = MutableStateFlow(LocalTime(hour = 9, minute = 0))

            override fun setEnabled(enabled: Boolean) = Unit

            override fun setCatchUpTime(time: LocalTime) = Unit
        }

    @Test
    fun `notifies every overdue task exactly once`() =
        runTest {
            val first = task(1L)
            val second = task(2L)
            coEvery { getOverdueIncompleteTasks(now) } returns listOf(first, second)

            runner().run()

            verify(exactly = 1) { notifier.notify(first) }
            verify(exactly = 1) { notifier.notify(second) }
        }

    @Test
    fun `posts nothing when no task is overdue`() =
        runTest {
            coEvery { getOverdueIncompleteTasks(now) } returns emptyList()

            runner().run()

            verify(exactly = 0) { notifier.notify(any()) }
        }

    @Test
    fun `skips the sweep entirely when the catch-up is disabled`() =
        runTest {
            enabled.value = false

            runner().run()

            coVerify(exactly = 0) { getOverdueIncompleteTasks(any()) }
            verify(exactly = 0) { notifier.notify(any()) }
        }

    @Test
    fun `arms the next catch-up after a sweep`() =
        runTest {
            coEvery { getOverdueIncompleteTasks(now) } returns listOf(task(1L))

            runner().run()

            verify(exactly = 1) { scheduler.sync() }
        }

    @Test
    fun `arms the next catch-up even when disabled so a re-enable is not missed`() =
        runTest {
            enabled.value = false

            runner().run()

            verify(exactly = 1) { scheduler.sync() }
        }

    @Test
    fun `still arms the next catch-up when notifying throws`() =
        runTest {
            coEvery { getOverdueIncompleteTasks(now) } returns listOf(task(1L))
            every { notifier.notify(any()) } throws IllegalStateException("notification failed")

            val result = runCatching { runner().run() }

            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)

            verify(exactly = 1) { scheduler.sync() }
        }

    @Test
    fun `still arms the next catch-up when the query throws`() =
        runTest {
            coEvery { getOverdueIncompleteTasks(now) } throws IllegalStateException("query failed")

            val result = runCatching { runner().run() }

            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)

            verify(exactly = 1) { scheduler.sync() }
        }

    private fun runner(): MissedReminderCatchUpRunner =
        MissedReminderCatchUpRunner(
            preferences = preferences,
            getOverdueIncompleteTasks = getOverdueIncompleteTasks,
            taskReminderNotifier = notifier,
            missedReminderScheduler = scheduler,
            clock = fixedClock(now),
        )

    private fun fixedClock(at: LocalDateTime): Clock =
        object : Clock {
            override fun now(): Instant = at.toInstant(TimeZone.currentSystemDefault())
        }

    private fun task(id: Long): Task =
        Task(
            id = id,
            title = "Task $id",
            description = "",
            reminderDate = LocalDateTime(2026, 7, 27, 18, 0),
        )
}
