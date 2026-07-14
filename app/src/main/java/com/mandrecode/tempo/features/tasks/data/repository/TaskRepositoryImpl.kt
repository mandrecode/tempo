package com.mandrecode.tempo.features.tasks.data.repository

import androidx.room.withTransaction
import com.mandrecode.tempo.core.data.insertOrVerifyRestoredEntity
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.TaskDao
import com.mandrecode.tempo.features.tasks.data.mapper.toDomain
import com.mandrecode.tempo.features.tasks.data.mapper.toEntity
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class TaskRepositoryImpl
    @Inject
    constructor(
        private val taskDao: TaskDao,
        private val database: TempoDatabase,
    ) : TaskRepository {
        override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks().map { it.toDomain() }

        override suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)?.toDomain()

        override fun getSubtasks(parentId: Long): Flow<List<Task>> = taskDao.getSubtasks(parentId).map { it.toDomain() }

        override suspend fun getSubtasksSync(parentId: Long): List<Task> = taskDao.getSubtasksSync(parentId).toDomain()

        override suspend fun getTasksWithReminders(): List<Task> = taskDao.getTasksWithReminders().toDomain()

        override suspend fun getMaxSortOrder(categoryId: Long): Int = taskDao.getMaxSortOrder(categoryId) ?: 0

        override suspend fun getMaxSubtaskSortOrder(parentId: Long): Int = taskDao.getMaxSubtaskSortOrder(parentId) ?: 0

        override suspend fun clearAllReminders() = taskDao.clearAllReminders()

        override suspend fun insertTask(task: Task): Long = taskDao.insertTask(task.toEntity())

        override suspend fun insertTasks(tasks: List<Task>): List<Long> = taskDao.insertTasks(tasks.toEntity())

        override suspend fun updateTask(task: Task) {
            val entity = task.toEntity()
            val oldTask = taskDao.getTaskById(entity.id)
            if (oldTask != null && oldTask.categoryId != entity.categoryId) {
                taskDao.updateSubtasksCategory(entity.id, entity.categoryId)
            }
            taskDao.updateTask(entity)
        }

        override suspend fun updateTasks(tasks: List<Task>) = taskDao.updateTasks(tasks.toEntity())

        override suspend fun deleteTask(task: Task) {
            taskDao.deleteTaskTrees(listOf(task.id))
        }

        override suspend fun deleteTaskWithSnapshot(taskId: Long): TaskDeletionSnapshot.TaskTree =
            database.withTransaction {
                val tasks = taskDao.getTaskTrees(listOf(taskId))
                require(tasks.any { it.id == taskId })
                taskDao.deleteTaskTrees(listOf(taskId))
                TaskDeletionSnapshot.TaskTree(
                    rootTaskId = taskId,
                    tasks = tasks.toDomain(),
                )
            }

        override suspend fun deleteCompletedTasksWithSnapshot(categoryId: Long): TaskDeletionSnapshot.CompletedTasks =
            database.withTransaction {
                val completedParentIds = taskDao.getCompletedTopLevelTaskIds(categoryId)
                val tasks =
                    if (completedParentIds.isEmpty()) {
                        emptyList()
                    } else {
                        taskDao.getTaskTrees(completedParentIds)
                    }
                if (tasks.isNotEmpty()) {
                    taskDao.deleteTaskTrees(completedParentIds)
                }
                TaskDeletionSnapshot.CompletedTasks(
                    categoryId = categoryId,
                    tasks = tasks.toDomain(),
                )
            }

        override suspend fun restoreDeletedTasks(snapshot: TaskDeletionSnapshot) {
            database.withTransaction {
                snapshot.tasks
                    .sortedParentFirst()
                    .forEach { task ->
                        val entity = task.toEntity()
                        insertOrVerifyRestoredEntity(
                            existing = taskDao.getTaskById(task.id),
                            snapshot = entity,
                            recordDescription = "task ${task.id}",
                        ) {
                            taskDao.insertTask(entity)
                        }
                    }
            }
        }

        override suspend fun toggleTaskCompletion(
            id: Long,
            isCompleted: Boolean,
            completedAt: LocalDateTime?,
        ) = taskDao.updateTaskCompletion(id, isCompleted, completedAt?.toString())

        override suspend fun updateSubtasksCompletion(
            parentId: Long,
            isCompleted: Boolean,
            completedAt: LocalDateTime?,
        ) = taskDao.updateSubtasksCompletion(parentId, isCompleted, completedAt?.toString())

        override suspend fun completeIncompleteSubtasks(
            parentId: Long,
            completedAt: LocalDateTime,
        ) = taskDao.completeIncompleteSubtasks(parentId, completedAt.toString())

        override suspend fun updateTaskReminderDate(
            taskId: Long,
            reminderDate: LocalDateTime?,
        ) = taskDao.updateTaskReminderDate(taskId, reminderDate?.toString())

        override suspend fun updateTaskSortOrder(
            taskId: Long,
            sortOrder: Int,
        ) = taskDao.updateTaskSortOrder(taskId, sortOrder)

        override suspend fun deleteTasksByCategoryId(id: Long) = taskDao.deleteTasksByCategoryId(id)

        override suspend fun deleteCompletedTasksByCategoryId(categoryId: Long) {
            val completedParentIds = taskDao.getCompletedTopLevelTaskIds(categoryId)
            if (completedParentIds.isNotEmpty()) {
                taskDao.deleteTaskTrees(completedParentIds)
            }
        }

        override suspend fun deleteCompletedTasksAtOrBefore(cutoff: LocalDateTime) =
            taskDao.deleteCompletedTaskTreesAtOrBefore(cutoff.toString())

        override suspend fun updateTaskNextInstanceId(
            taskId: Long,
            nextInstanceId: Long?,
        ) = taskDao.updateTaskNextInstanceId(taskId, nextInstanceId)

        override suspend fun deleteTaskWithSubtasks(taskId: Long) =
            database.withTransaction {
                taskDao.deleteTaskTrees(listOf(taskId))
            }

        override suspend fun <R> runInTransaction(block: suspend () -> R): R = database.withTransaction { block() }
    }
