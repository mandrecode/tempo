package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import jakarta.inject.Inject

/**
 * Moves the section at [fromIndex] to [toIndex] within one category's [sections] (#61) and
 * renumbers them all 0..n-1. Replaying the same order writes the same values.
 */
class ReorderTaskSectionsUseCase
    @Inject
    constructor(
        private val sectionRepository: TaskSectionRepository,
    ) {
        suspend operator fun invoke(
            fromIndex: Int,
            toIndex: Int,
            sections: List<TaskSection>,
        ) {
            if (fromIndex !in sections.indices || toIndex !in sections.indices || fromIndex == toIndex) return
            val reordered = sections.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
            sectionRepository.updateSortOrders(reordered.map(TaskSection::id))
        }
    }
