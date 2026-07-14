package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.model.RestoreResult
import com.mandrecode.tempo.features.tasks.domain.model.CategoryDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import jakarta.inject.Inject

class RestoreDeletedCategoryUseCase
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
        private val taskReminderScheduler: TaskReminderScheduler,
    ) {
        suspend operator fun invoke(snapshot: CategoryDeletionSnapshot): RestoreResult {
            categoryRepository.restoreDeletedCategory(snapshot)
            snapshot.tasks.forEach(taskReminderScheduler::cancel)
            val results =
                snapshot.tasks
                    .filter { it.reminderDate != null }
                    .map(taskReminderScheduler::schedule)
            return RestoreResult(results)
        }
    }
