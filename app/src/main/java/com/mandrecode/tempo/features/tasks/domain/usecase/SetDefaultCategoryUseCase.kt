package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import jakarta.inject.Inject

class SetDefaultCategoryUseCase
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
    ) {
        suspend operator fun invoke(categoryId: Long) {
            categoryRepository.setDefaultCategory(categoryId)
        }
    }
