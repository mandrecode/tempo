package com.mandrecode.tempo.features.tasks.data.repository

import androidx.room.withTransaction
import com.mandrecode.tempo.core.data.entity.TaskSectionEntity
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.TaskDao
import com.mandrecode.tempo.core.data.local.dao.TaskSectionDao
import com.mandrecode.tempo.features.tasks.data.mapper.toDomain
import com.mandrecode.tempo.features.tasks.data.mapper.toEntity
import com.mandrecode.tempo.features.tasks.domain.model.SectionDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskSectionRepositoryImpl
    @Inject
    constructor(
        private val sectionDao: TaskSectionDao,
        private val taskDao: TaskDao,
        private val database: TempoDatabase,
    ) : TaskSectionRepository {
        override fun getAllSections(): Flow<List<TaskSection>> =
            sectionDao.getAllSections().map { sections -> sections.map(TaskSectionEntity::toDomain) }

        override suspend fun getSectionsByCategory(categoryId: Long): List<TaskSection> =
            sectionDao.getSectionsByCategorySync(categoryId).map(TaskSectionEntity::toDomain)

        override suspend fun getSectionById(id: Long): TaskSection? = sectionDao.getSectionById(id)?.toDomain()

        override suspend fun createSection(
            categoryId: Long,
            name: String,
            afterSectionId: Long?,
        ): Long =
            database.withTransaction {
                val nextSortOrder = (sectionDao.getMaxSortOrder(categoryId) ?: -1) + 1
                val id =
                    sectionDao.insertSection(
                        TaskSectionEntity(categoryId = categoryId, name = name, sortOrder = nextSortOrder),
                    )
                if (afterSectionId != null) {
                    val ordered = sectionDao.getSectionsByCategorySync(categoryId).map { it.id }.toMutableList()
                    val anchor = ordered.indexOf(afterSectionId)
                    if (anchor >= 0) {
                        ordered.remove(id)
                        ordered.add(anchor + 1, id)
                        ordered.forEachIndexed { index, sectionId -> sectionDao.updateSortOrder(sectionId, index) }
                    }
                }
                id
            }

        override suspend fun renameSection(
            id: Long,
            name: String,
        ) = sectionDao.renameSection(id, name)

        override suspend fun updateSortOrders(orderedIds: List<Long>) {
            database.withTransaction {
                orderedIds.forEachIndexed { index, id -> sectionDao.updateSortOrder(id, index) }
            }
        }

        override suspend fun deleteSectionWithSnapshot(id: Long): SectionDeletionSnapshot? =
            database.withTransaction {
                val section = sectionDao.getSectionById(id) ?: return@withTransaction null
                val taskIds = taskDao.getTopLevelTaskIdsInSection(id)
                taskDao.clearSection(id)
                sectionDao.deleteSection(section)
                SectionDeletionSnapshot(section = section.toDomain(), taskIds = taskIds)
            }

        override suspend fun restoreDeletedSection(snapshot: SectionDeletionSnapshot) {
            database.withTransaction {
                if (sectionDao.getSectionById(snapshot.section.id) == null) {
                    sectionDao.insertSection(snapshot.section.toEntity())
                }
                if (snapshot.taskIds.isNotEmpty()) {
                    // Tasks that left the category (or were deleted) in the meantime stay as they are.
                    taskDao.assignSection(snapshot.taskIds, snapshot.section.id, snapshot.section.categoryId)
                }
            }
        }

        override suspend fun moveTaskToSection(
            taskId: Long,
            sectionId: Long?,
            index: Int,
        ): Boolean =
            database.withTransaction {
                val task = taskDao.getTaskById(taskId)
                if (task == null || task.parentTaskId != null) return@withTransaction false
                if (sectionId != null && sectionDao.getSectionById(sectionId)?.categoryId != task.categoryId) {
                    return@withTransaction false
                }
                val others =
                    taskDao
                        .getActiveTopLevelTasksInSection(task.categoryId, sectionId)
                        .filterNot { it.id == taskId }
                val ordered = others.toMutableList().apply { add(index.coerceIn(0, others.size), task) }
                val base = ordered.minOfOrNull { it.sortOrder } ?: 0
                ordered.forEachIndexed { position, entity ->
                    val newSortOrder = base + position
                    if (entity.id == taskId || entity.sortOrder != newSortOrder) {
                        taskDao.updateTaskSectionAndSortOrder(entity.id, sectionId, newSortOrder)
                    }
                }
                true
            }
    }
