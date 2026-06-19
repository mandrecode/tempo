package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import jakarta.inject.Inject

class DeleteCompletedTasksUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) {
        suspend operator fun invoke(categoryId: Long) {
            taskRepository.deleteCompletedTasksByCategoryId(categoryId)
        }
    }
