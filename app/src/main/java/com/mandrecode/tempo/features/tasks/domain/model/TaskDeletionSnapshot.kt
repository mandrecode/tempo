package com.mandrecode.tempo.features.tasks.domain.model

sealed interface TaskDeletionSnapshot {
    val tasks: List<Task>

    data class TaskTree(
        val rootTaskId: Long,
        override val tasks: List<Task>,
    ) : TaskDeletionSnapshot

    data class CompletedTasks(
        val categoryId: Long,
        override val tasks: List<Task>,
    ) : TaskDeletionSnapshot
}

data class CategoryDeletionSnapshot(
    val category: Category,
    val tasks: List<Task>,
    /** The category's sections (#61), restored before its tasks so their sectionIds resolve. */
    val sections: List<TaskSection> = emptyList(),
)
