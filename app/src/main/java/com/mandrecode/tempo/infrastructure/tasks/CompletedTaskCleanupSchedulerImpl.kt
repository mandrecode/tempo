package com.mandrecode.tempo.infrastructure.tasks

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mandrecode.tempo.features.tasks.domain.scheduler.CompletedTaskCleanupScheduler
import com.mandrecode.tempo.infrastructure.tasks.workers.CompletedTaskCleanupWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompletedTaskCleanupSchedulerImpl
    @Inject
    constructor(
        private val workManager: WorkManager,
    ) : CompletedTaskCleanupScheduler {
        override fun schedule() {
            workManager.enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<CompletedTaskCleanupWorker>().build(),
            )
            workManager.enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<CompletedTaskCleanupWorker>(
                    REPEAT_INTERVAL_HOURS,
                    TimeUnit.HOURS,
                ).build(),
            )
        }

        override fun cancel() {
            workManager.cancelUniqueWork(IMMEDIATE_WORK_NAME)
            workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
        }

        companion object {
            internal const val IMMEDIATE_WORK_NAME = "completed-task-cleanup-immediate"
            internal const val PERIODIC_WORK_NAME = "completed-task-cleanup-periodic"
            private const val REPEAT_INTERVAL_HOURS = 24L
        }
    }
