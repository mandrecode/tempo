package com.mandrecode.tempo.features.tasks.domain.repository

import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>

    suspend fun getTaskById(id: Long): Task?

    fun getSubtasks(parentId: Long): Flow<List<Task>>

    suspend fun getSubtasksSync(parentId: Long): List<Task>

    suspend fun getTasksWithReminders(): List<Task>

    suspend fun getMaxSortOrder(categoryId: Long): Int

    suspend fun getMaxSubtaskSortOrder(parentId: Long): Int

    suspend fun clearAllReminders()

    suspend fun insertTask(task: Task): Long

    suspend fun insertTasks(tasks: List<Task>): List<Long>

    suspend fun updateTask(task: Task)

    suspend fun updateTasks(tasks: List<Task>)

    suspend fun deleteTask(task: Task)

    suspend fun toggleTaskCompletion(
        id: Long,
        isCompleted: Boolean,
        completedAt: LocalDateTime?,
    )

    suspend fun updateSubtasksCompletion(
        parentId: Long,
        isCompleted: Boolean,
        completedAt: LocalDateTime?,
    )

    suspend fun completeIncompleteSubtasks(
        parentId: Long,
        completedAt: LocalDateTime,
    )

    suspend fun updateTaskReminderDate(
        taskId: Long,
        reminderDate: LocalDateTime?,
    )

    suspend fun updateTaskSortOrder(
        taskId: Long,
        sortOrder: Int,
    )

    suspend fun deleteTasksByCategoryId(id: Long)

    suspend fun deleteCompletedTasksByCategoryId(categoryId: Long)

    suspend fun deleteCompletedTasksAtOrBefore(cutoff: LocalDateTime)

    /**
     * Updates the `nextInstanceId` link on an archived periodic task pointing at its
     * spawned next-occurrence id. Set to `null` to clear a stale link.
     */
    suspend fun updateTaskNextInstanceId(
        taskId: Long,
        nextInstanceId: Long?,
    )

    /** Deletes a task and all of its subtasks. */
    suspend fun deleteTaskWithSubtasks(taskId: Long)

    /**
     * Runs the given block inside a single Room transaction. Repository implementations
     * back this with `RoomDatabase.withTransaction`. Side-effects with external systems
     * (e.g. reminder scheduler) MUST run after this returns to avoid holding the DB lock.
     */
    suspend fun <R> runInTransaction(block: suspend () -> R): R
}
