package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class DeleteExpiredCompletedTasksUseCaseTest {
    private val repository = mockk<TaskRepository>(relaxed = true)
    private val useCase = DeleteExpiredCompletedTasksUseCase(repository)

    @Test
    fun `deletes tasks at local cutoff`() =
        runTest {
            useCase(
                now = LocalDateTime(2026, 7, 13, 10, 30),
                retentionDays = 30,
            )

            coVerify { repository.deleteCompletedTasksAtOrBefore(LocalDateTime(2026, 6, 13, 10, 30)) }
        }

    @Test
    fun `clamps retention before calculating cutoff`() =
        runTest {
            useCase(
                now = LocalDateTime(2026, 7, 13, 10, 30),
                retentionDays = 0,
            )

            coVerify { repository.deleteCompletedTasksAtOrBefore(LocalDateTime(2026, 7, 12, 10, 30)) }
        }
}
