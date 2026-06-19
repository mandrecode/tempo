package com.mandrecode.tempo.infrastructure.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mandrecode.tempo.infrastructure.reminders.workers.RescheduleRemindersWorker
import java.util.concurrent.TimeUnit

object ReminderRefreshScheduler {
    private const val WORK_NAME = "RescheduleRemindersWorker"
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
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest,
            )
        enqueuedInProcess = true
    }
}
