package com.mandrecode.tempo.features.tasks.domain.scheduler

interface CompletedTaskCleanupScheduler {
    fun schedule()

    fun cancel()
}
