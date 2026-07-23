package com.mandrecode.tempo.infrastructure.reminders.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.mandrecode.tempo.infrastructure.reminders.workers.RescheduleRemindersWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class BootAndTimeReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (shouldRescheduleReminders(intent.action)) {
            val workRequest =
                OneTimeWorkRequestBuilder<RescheduleRemindersWorker>()
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS,
                    ).build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "BootAndTimeRescheduleRemindersWorker",
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )
        }
    }

    companion object {
        fun shouldRescheduleReminders(action: String?): Boolean =
            action == Intent.ACTION_BOOT_COMPLETED ||
                action == Intent.ACTION_TIMEZONE_CHANGED ||
                action == Intent.ACTION_TIME_CHANGED ||
                action == Intent.ACTION_MY_PACKAGE_REPLACED
    }
}
