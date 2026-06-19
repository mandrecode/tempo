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

            val baseSortOrder = tasks.minOfOrNull { it.sortOrder } ?: 0
            val tasksToUpdate = mutableListOf<Task>()
            reorderedTasks.forEachIndexed { index, task ->
                val newSortOrder = baseSortOrder + index
                if (task.sortOrder != newSortOrder) {
                    tasksToUpdate.add(task.copy(sortOrder = newSortOrder))
                }
            }
            if (tasksToUpdate.isNotEmpty()) {
                taskRepository.updateTasks(tasksToUpdate)
            }
        }
    }
