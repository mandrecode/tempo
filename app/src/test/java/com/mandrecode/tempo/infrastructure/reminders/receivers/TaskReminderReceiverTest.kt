package com.mandrecode.tempo.infrastructure.reminders.receivers

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.features.tasks.domain.model.Task
import org.junit.Test

class TaskReminderReceiverTest {
    @Test
    fun `shouldProcessTaskReminder returns false for missing task`() {
        val result = TaskReminderReceiver.shouldProcessTaskReminder(null)

        assertThat(result).isFalse()
    }

    @Test
    fun `shouldProcessTaskReminder returns false for completed task`() {
        val result = TaskReminderReceiver.shouldProcessTaskReminder(task(isCompleted = true))

        assertThat(result).isFalse()
    }

    @Test
    fun `shouldProcessTaskReminder returns true for incomplete task`() {
        val result = TaskReminderReceiver.shouldProcessTaskReminder(task(isCompleted = false))

        assertThat(result).isTrue()
    }

    @Test
    fun `a task with no focus time today is still notified about`() {
        val outcome =
            TaskReminderReceiver.reminderOutcome(task(isCompleted = false), hasFocusTimeToday = false)

        assertThat(outcome.notify).isTrue()
    }

    @Test
    fun `a task already worked on today is not notified about`() {
        val outcome =
            TaskReminderReceiver.reminderOutcome(task(isCompleted = false), hasFocusTimeToday = true)

        assertThat(outcome.notify).isFalse()
    }

    @Test
    fun `a periodic task still rolls over while its notification is withheld`() {
        val outcome = TaskReminderReceiver.reminderOutcome(periodicTask(), hasFocusTimeToday = true)

        assertThat(outcome.notify).isFalse()
        assertThat(outcome.rollOver).isTrue()
    }

    @Test
    fun `a one-off task has nothing to roll over`() {
        val outcome =
            TaskReminderReceiver.reminderOutcome(task(isCompleted = false), hasFocusTimeToday = false)

        assertThat(outcome.rollOver).isFalse()
    }

    @Test
    fun `a periodic subtask rolls over through its parent, not on its own`() {
        val outcome =
            TaskReminderReceiver.reminderOutcome(
                periodicTask().copy(parentTaskId = 2L),
                hasFocusTimeToday = false,
            )

        assertThat(outcome.rollOver).isFalse()
    }

    private fun task(isCompleted: Boolean): Task =
        Task(
            id = 1L,
            title = "Task",
            description = "",
            isCompleted = isCompleted,
        )

    private fun periodicTask(): Task = task(isCompleted = false).copy(periodicity = Periodicity.DAILY)
}
