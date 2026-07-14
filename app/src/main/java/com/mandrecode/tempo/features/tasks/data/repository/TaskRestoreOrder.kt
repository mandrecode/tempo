package com.mandrecode.tempo.features.tasks.data.repository

import com.mandrecode.tempo.features.tasks.domain.model.Task

internal fun List<Task>.sortedParentFirst(): List<Task> {
    val tasksById = associateBy(Task::id)
    val depthsById = mutableMapOf<Long, Int>()

    fun depthOf(
        task: Task,
        visiting: MutableSet<Long>,
    ): Int {
        depthsById[task.id]?.let { return it }
        check(visiting.add(task.id)) { "Task snapshot contains a parent cycle at task ${task.id}" }

        val depth =
            task.parentTaskId
                ?.let(tasksById::get)
                ?.let { parent -> depthOf(parent, visiting) + 1 }
                ?: 0

        visiting.remove(task.id)
        depthsById[task.id] = depth
        return depth
    }

    return sortedBy { task -> depthOf(task, mutableSetOf()) }
}
