package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import jakarta.inject.Inject

class ReorderCategoriesUseCase
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
    ) {
        suspend operator fun invoke(
            fromIndex: Int,
            toIndex: Int,
            categories: List<Category>,
        ) {
            if (fromIndex !in categories.indices || toIndex !in categories.indices) return
            if (fromIndex == toIndex) return

            val reordered = categories.toMutableList()
            val moved = reordered.removeAt(fromIndex)
            reordered.add(toIndex, moved)

            val baseSortOrder = categories.minOfOrNull { it.sortOrder } ?: 0
            val toUpdate = mutableListOf<Category>()
            reordered.forEachIndexed { index, category ->
                val newSortOrder = baseSortOrder + index
                if (category.sortOrder != newSortOrder) {
                    toUpdate.add(category.copy(sortOrder = newSortOrder))
                }
            }
            if (toUpdate.isNotEmpty()) {
                categoryRepository.updateCategories(toUpdate)
            }
        }
    }
