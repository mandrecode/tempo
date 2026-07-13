package com.mandrecode.tempo.infrastructure.tasks

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class CompletedTaskCleanupSchedulerImplTest {
    private val workManager = mockk<WorkManager>(relaxed = true)

    @Test
    fun `schedule replaces immediate work and updates periodic work`() {
        CompletedTaskCleanupSchedulerImpl(workManager).schedule()

        verify {
            workManager.enqueueUniqueWork(
                CompletedTaskCleanupSchedulerImpl.IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>(),
            )
            workManager.enqueueUniquePeriodicWork(
                CompletedTaskCleanupSchedulerImpl.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                any<PeriodicWorkRequest>(),
            )
        }
    }

    @Test
    fun `cancel removes immediate and periodic work`() {
        CompletedTaskCleanupSchedulerImpl(workManager).cancel()

        verify {
            workManager.cancelUniqueWork(CompletedTaskCleanupSchedulerImpl.IMMEDIATE_WORK_NAME)
            workManager.cancelUniqueWork(CompletedTaskCleanupSchedulerImpl.PERIODIC_WORK_NAME)
        }
    }
}
