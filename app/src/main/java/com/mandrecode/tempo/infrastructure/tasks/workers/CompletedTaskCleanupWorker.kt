package com.mandrecode.tempo.infrastructure.tasks.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.usecase.DeleteExpiredCompletedTasksUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@HiltWorker
class CompletedTaskCleanupWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val preferences: CompletedTaskRetentionPreferences,
        private val deleteExpiredCompletedTasks: DeleteExpiredCompletedTasksUseCase,
        private val clock: Clock,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result =
            runCatching {
                if (preferences.isEnabled.value) {
                    deleteExpiredCompletedTasks(
                        now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        retentionDays = preferences.retentionDays.value,
                    )
                }
            }.fold(
                onSuccess = { Result.success() },
                onFailure = { failure ->
                    if (failure is CancellationException) throw failure
                    if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
                },
            )

        companion object {
            private const val MAX_ATTEMPTS = 3
        }
    }
