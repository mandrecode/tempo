package com.mandrecode.tempo.infrastructure.notifications

/**
 * Centralized contract for notification lifecycle synchronization.
 *
 * Every notification dismiss operation should go through this manager so that
 * notification tags, request-code generation, and cancellation logic remain
 * consistent regardless of where the state change originates (in-app
 * completion, notification action, alarm cancellation, etc.).
 */
interface NotificationSyncManager {
    fun dismissTaskNotification(taskId: Long)

    fun dismissHabitNotification(habitId: Long)

    fun dismissHabitChainNotification(chainId: Long)

    companion object {
        const val NOTIFICATION_TAG_HABIT = "habit"
        const val NOTIFICATION_TAG_CHAIN = "chain"
    }
}
