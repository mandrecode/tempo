package com.mandrecode.tempo.features.tasks.presentation

import com.mandrecode.tempo.features.tasks.domain.model.CategoryDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.model.SectionDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.model.TaskDeletionSnapshot

internal sealed interface PendingTaskDeletion {
    data class Tasks(
        val snapshot: TaskDeletionSnapshot,
    ) : PendingTaskDeletion

    data class Category(
        val snapshot: CategoryDeletionSnapshot,
        val selectedCategoryIdBeforeDeletion: Long,
    ) : PendingTaskDeletion

    data class Section(
        val snapshot: SectionDeletionSnapshot,
        val wasCollapsed: Boolean,
    ) : PendingTaskDeletion
}
