package com.mandrecode.tempo.infrastructure.notifications

import android.app.NotificationManager

class NotificationSyncManagerImpl(
    private val notificationManager: NotificationManager,
) : NotificationSyncManager {
    override fun dismissTaskNotification(taskId: Long) {
        notificationManager.cancel(RequestCodeGenerator.forTask(taskId))
    }

    override fun dismissHabitNotification(habitId: Long) {
        notificationManager.cancel(
            NotificationSyncManager.NOTIFICATION_TAG_HABIT,
            RequestCodeGenerator.forHabit(habitId),
        )
    }

    override fun dismissHabitChainNotification(chainId: Long) {
        notificationManager.cancel(
            NotificationSyncManager.NOTIFICATION_TAG_CHAIN,
            RequestCodeGenerator.forHabitChain(chainId),
        )
    }
}
