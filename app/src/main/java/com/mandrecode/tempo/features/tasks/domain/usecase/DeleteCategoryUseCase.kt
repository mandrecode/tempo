package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.CategoryDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import jakarta.inject.Inject

class DeleteCategoryUseCase
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
        private val taskReminderScheduler: TaskReminderScheduler,
    ) {
        sealed class Result {
            data class Success(
                val snapshot: CategoryDeletionSnapshot,
            ) : Result()

            data object LastCategory : Result()

            data object CannotDeleteDefault : Result()
        }

        suspend operator fun invoke(category: Category): Result {
            if (category.isDefault) {
                return Result.CannotDeleteDefault
            }
            if (categoryRepository.getCategoryCount() <= 1) {
                return Result.LastCategory
            }
            val snapshot = categoryRepository.deleteCategoryWithSnapshot(category)
            snapshot.tasks.forEach(taskReminderScheduler::cancel)
            return Result.Success(snapshot)
        }
    }
