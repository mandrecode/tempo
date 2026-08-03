package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import jakarta.inject.Inject

class ReorderTasksUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) {
        suspend operator fun invoke(
            fromIndex: Int,
            toIndex: Int,
            tasks: List<Task>,
        ) {
            val reorderedTasks = tasks.toMutableList()
            val movedTask = reorderedTasks.removeAt(fromIndex)
            reorderedTasks.add(toIndex, movedTask)

            val sortOrders = sortOrdersToAssign(tasks)
            val tasksToUpdate =
                reorderedTasks.mapIndexedNotNull { index, task ->
                    val newSortOrder = sortOrders[index]
                    task.copy(sortOrder = newSortOrder).takeIf { task.sortOrder != newSortOrder }
                }
            if (tasksToUpdate.isNotEmpty()) {
                taskRepository.updateTasks(tasksToUpdate)
            }
        }

        /**
         * The sort orders to hand out position by position.
         *
         * Redistributing the values these tasks already hold keeps the write confined to them:
         * [tasks] is often a subset of its category (a run of tied tasks, or one parent's
         * subtasks), and renumbering it from its minimum would mint values that collide with the
         * tasks in between and scramble their manual order. Values that aren't distinct can't
         * express an order, so those fall back to renumbering.
         */
        private fun sortOrdersToAssign(tasks: List<Task>): List<Int> {
            val existing = tasks.map { it.sortOrder }.sorted()
            if (existing.distinct().size == existing.size) return existing
            val base = existing.firstOrNull() ?: 0
            return List(tasks.size) { index -> base + index }
        }
    }
