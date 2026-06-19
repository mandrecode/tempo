package com.mandrecode.tempo.infrastructure.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NotificationChannelManagerTest {
    @Test
    fun `channel IDs are not empty`() {
        assertThat(NotificationChannelManager.TASK_REMINDER_CHANNEL_ID).isNotEmpty()
        assertThat(NotificationChannelManager.HABIT_REMINDER_CHANNEL_ID).isNotEmpty()
        assertThat(NotificationChannelManager.HABIT_CHAIN_LIVE_ACTIVITY_CHANNEL_ID).isNotEmpty()
    }

    @Test
    fun `channel IDs are unique`() {
        val ids =
            setOf(
                NotificationChannelManager.TASK_REMINDER_CHANNEL_ID,
                NotificationChannelManager.HABIT_REMINDER_CHANNEL_ID,
                NotificationChannelManager.HABIT_CHAIN_LIVE_ACTIVITY_CHANNEL_ID,
            )
        assertThat(ids).hasSize(3)
    }
}
