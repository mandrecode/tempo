package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.CategoryDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RestoreDeletedCategoryUseCaseTest {
    @Test
    fun `restores category snapshot without scheduling tasks that have no reminder`() =
        runTest {
            val repository = mockk<CategoryRepository>(relaxed = true)
            val scheduler = mockk<TaskReminderScheduler>(relaxed = true)
            val snapshot =
                CategoryDeletionSnapshot(
                    Category(id = 2, name = "Work"),
                    listOf(Task(id = 4, title = "Task", description = "", categoryId = 2)),
                )

            val result = RestoreDeletedCategoryUseCase(repository, scheduler)(snapshot)

            coVerify { repository.restoreDeletedCategory(snapshot) }
            io.mockk.verify(exactly = 0) { scheduler.schedule(any()) }
            assertThat(result.scheduleResults).isEmpty()
        }
}
