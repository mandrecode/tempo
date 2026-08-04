package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.UndatedTask
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * The loose ends: open, top-level work carrying no date at all.
 *
 * The same population `GetFocusAgendaUseCase` counts for the Focus footer offering to give them a
 * day, so the sheet that footer opens lists exactly what it promised. A step with no time of its
 * own is deliberately not one of them — it is part of whatever its parent is, not a task waiting
 * to be planned on its own.
 */
class GetUndatedTasksUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val categoryRepository: CategoryRepository,
    ) {
        operator fun invoke(): Flow<List<UndatedTask>> =
            withCategories { tasks ->
                tasks.filter(::isLooseEnd)
            }

        /**
         * The same rows, but held to [taskIds] rather than to being undated.
         *
         * Planning a task is what takes it out of [invoke]'s answer, so a sheet driven by that flow
         * would lose every row the moment it did its job. The sheet asks for the tasks it opened
         * with instead, and then gets to show what planning them did.
         */
        fun pinnedTo(taskIds: Set<Long>): Flow<List<UndatedTask>> =
            withCategories { tasks ->
                tasks.filter { it.id in taskIds }
            }

        private fun withCategories(select: (List<Task>) -> List<Task>): Flow<List<UndatedTask>> =
            combine(
                taskRepository.getAllTasks(),
                categoryRepository.getAllCategories(),
            ) { tasks, categories ->
                val categoriesById: Map<Long, Category> = categories.associateBy { it.id }
                val subtasksByParent: Map<Long?, List<Task>> =
                    tasks.filter { it.parentTaskId != null }.groupBy { it.parentTaskId }
                select(tasks)
                    .sortedBy { it.sortOrder }
                    .map { task ->
                        UndatedTask(
                            task = task,
                            category = categoriesById[task.categoryId],
                            subtasks = subtasksByParent[task.id].orEmpty().sortedBy { it.sortOrder },
                        )
                    }
            }

        private fun isLooseEnd(task: Task): Boolean =
            task.parentTaskId == null &&
                task.reminderDate == null &&
                !task.isCompleted
    }
