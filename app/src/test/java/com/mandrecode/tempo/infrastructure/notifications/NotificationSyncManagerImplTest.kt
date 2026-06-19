package com.mandrecode.tempo.infrastructure.notifications

import android.app.NotificationManager
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class NotificationSyncManagerImplTest {
    private lateinit var notificationManager: NotificationManager
    private lateinit var syncManager: NotificationSyncManagerImpl

    @Before
    fun setup() {
        notificationManager = mockk(relaxed = true)
        syncManager = NotificationSyncManagerImpl(notificationManager)
    }

    @Test
    fun `dismissTaskNotification cancels with correct request code`() {
        val taskId = 42L

        syncManager.dismissTaskNotification(taskId)

        verify { notificationManager.cancel(RequestCodeGenerator.forTask(taskId)) }
    }

    @Test
    fun `dismissHabitNotification cancels with habit tag and correct request code`() {
        val habitId = 7L

        syncManager.dismissHabitNotification(habitId)

        verify {
            notificationManager.cancel(
                NotificationSyncManager.NOTIFICATION_TAG_HABIT,
                RequestCodeGenerator.forHabit(habitId),
            )
        }
    }

    @Test
    fun `dismissHabitChainNotification cancels with chain tag and correct request code`() {
        val chainId = 99L

        syncManager.dismissHabitChainNotification(chainId)

        verify {
            notificationManager.cancel(
                NotificationSyncManager.NOTIFICATION_TAG_CHAIN,
                RequestCodeGenerator.forHabitChain(chainId),
            )
        }
    }

    @Test
    fun `dismissing different entity types uses different request codes`() {
        val id = 1L

        syncManager.dismissTaskNotification(id)
        syncManager.dismissHabitNotification(id)
        syncManager.dismissHabitChainNotification(id)

        val taskCode = RequestCodeGenerator.forTask(id)
        val habitCode = RequestCodeGenerator.forHabit(id)
        val chainCode = RequestCodeGenerator.forHabitChain(id)

        assertThat(taskCode).isNotEqualTo(habitCode)
        assertThat(taskCode).isNotEqualTo(chainCode)
        assertThat(habitCode).isNotEqualTo(chainCode)

        verify { notificationManager.cancel(taskCode) }
        verify { notificationManager.cancel(NotificationSyncManager.NOTIFICATION_TAG_HABIT, habitCode) }
        verify { notificationManager.cancel(NotificationSyncManager.NOTIFICATION_TAG_CHAIN, chainCode) }
    }

    @Test
    fun `dismissing same entity type twice is safe`() {
        val habitId = 5L

        syncManager.dismissHabitNotification(habitId)
        syncManager.dismissHabitNotification(habitId)

        verify(exactly = 2) {
            notificationManager.cancel(
                NotificationSyncManager.NOTIFICATION_TAG_HABIT,
                RequestCodeGenerator.forHabit(habitId),
            )
        }
    }
}
