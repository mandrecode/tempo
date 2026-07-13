package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.scheduler.CompletedTaskCleanupScheduler
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ConfigureCompletedTaskRetentionUseCaseTest {
    private lateinit var preferences: CompletedTaskRetentionPreferences
    private lateinit var scheduler: CompletedTaskCleanupScheduler
    private lateinit var useCase: ConfigureCompletedTaskRetentionUseCase

    @Before
    fun setUp() {
        preferences = mockk(relaxed = true)
        scheduler = mockk(relaxed = true)
        useCase = ConfigureCompletedTaskRetentionUseCase(preferences, scheduler)
    }

    @Test
    fun `enabled policy persists clamped days and schedules cleanup`() {
        useCase(enabled = true, retentionDays = 500)

        verify { preferences.setRetentionDays(365) }
        verify { preferences.setEnabled(true) }
        verify { scheduler.schedule() }
        verify(exactly = 0) { scheduler.cancel() }
    }

    @Test
    fun `disabled policy persists values and cancels cleanup`() {
        useCase(enabled = false, retentionDays = 30)

        verify { preferences.setRetentionDays(30) }
        verify { preferences.setEnabled(false) }
        verify { scheduler.cancel() }
        verify(exactly = 0) { scheduler.schedule() }
    }
}
