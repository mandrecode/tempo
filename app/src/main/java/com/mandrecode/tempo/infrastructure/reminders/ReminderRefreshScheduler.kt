package com.mandrecode.tempo.infrastructure.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mandrecode.tempo.infrastructure.reminders.workers.RescheduleRemindersWorker
import java.util.concurrent.TimeUnit

object ReminderRefreshScheduler {
    private const val PERIODIC_WORK_NAME = "RescheduleRemindersWorker"
    private const val IMMEDIATE_WORK_NAME = "ImmediateRescheduleRemindersWorker"
    private var enqueuedInProcess = false

    @Synchronized
    fun enqueuePeriodicRefresh(context: Context) {
        if (enqueuedInProcess) return

        val workRequest =
            PeriodicWorkRequestBuilder<RescheduleRemindersWorker>(
                24,
                TimeUnit.HOURS,
            ).build()

        WorkManager
            .getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest,
            )
        enqueuedInProcess = true
    }

    /**
     * Re-arms reminders immediately rather than waiting for the next periodic refresh.
     * Covers reopening the app after a force-stop, which clears scheduled alarms and
     * prevents boot/background triggers from reaching the app until it is explicitly launched.
     */
    fun enqueueImmediateRefresh(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<RescheduleRemindersWorker>().build()

        WorkManager
            .getInstance(context.applicationContext)
            .enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                workRequest,
            )
    }
}
