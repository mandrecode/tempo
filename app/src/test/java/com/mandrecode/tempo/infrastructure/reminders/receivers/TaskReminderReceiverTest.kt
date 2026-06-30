package com.mandrecode.tempo.infrastructure.reminders.receivers

import com.google.common.truth.Truth.assertThat
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

    private fun task(isCompleted: Boolean): Task =
        Task(
            id = 1L,
            title = "Task",
            description = "",
            isCompleted = isCompleted,
        )
}
