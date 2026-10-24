package com.mandrecode.tempo.infrastructure.focus

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.infrastructure.reminders.receivers.TaskReminderReceiver
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class StartFocusSessionReceiverTest {
    @Test
    fun `taskIdFrom returns the task id carried by the reminder action`() {
        assertThat(StartFocusSessionReceiver.taskIdFrom(intentWithTaskId(7L))).isEqualTo(7L)
    }

    @Test
    fun `taskIdFrom returns null when the intent carries no task id`() {
        assertThat(StartFocusSessionReceiver.taskIdFrom(intentWithTaskId(0L))).isNull()
    }

    private fun intentWithTaskId(taskId: Long): Intent =
        mockk {
            every { getLongExtra(TaskReminderReceiver.EXTRA_TASK_ID, 0L) } returns taskId
        }
}
