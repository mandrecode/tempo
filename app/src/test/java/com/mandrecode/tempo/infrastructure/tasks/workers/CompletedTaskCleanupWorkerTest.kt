package com.mandrecode.tempo.infrastructure.tasks.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.usecase.DeleteExpiredCompletedTasksUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

class CompletedTaskCleanupWorkerTest {
    private val context = mockk<Context>(relaxed = true)
    private val workerParameters = mockk<WorkerParameters>(relaxed = true)
    private val preferences = mockk<CompletedTaskRetentionPreferences>(relaxed = true)
    private val deleteExpiredCompletedTasks = mockk<DeleteExpiredCompletedTasksUseCase>(relaxed = true)
    private val clock = mockk<Clock>()
    private lateinit var worker: CompletedTaskCleanupWorker

    @Before
    fun setUp() {
        worker =
            CompletedTaskCleanupWorker(
                context,
                workerParameters,
                preferences,
                deleteExpiredCompletedTasks,
                clock,
            )
    }

    @Test
    fun `disabled policy succeeds without deleting`() =
        runTest {
            every { preferences.isEnabled.value } returns false

            val result = worker.doWork()

            assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
            coVerify(exactly = 0) { deleteExpiredCompletedTasks(any(), any()) }
        }

    @Test
    fun `enabled policy deletes using current time and retention`() =
        runTest {
            val now = LocalDateTime(2026, 7, 13, 10, 30)
            every { preferences.isEnabled.value } returns true
            every { preferences.retentionDays.value } returns 45
            every { clock.now() } returns now.toInstant(TimeZone.currentSystemDefault())

            val result = worker.doWork()

            assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
            coVerify { deleteExpiredCompletedTasks(now, 45) }
        }

    @Test
    fun `failure retries before maximum attempts`() =
        runTest {
            every { preferences.isEnabled.value } returns true
            every { preferences.retentionDays.value } returns 30
            every { clock.now() } throws IllegalStateException("boom")
            every { workerParameters.runAttemptCount } returns 1

            val result = worker.doWork()

            assertThat(result).isInstanceOf(ListenableWorker.Result.Retry::class.java)
        }
}
