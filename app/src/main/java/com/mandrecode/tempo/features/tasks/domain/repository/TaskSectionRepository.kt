package com.mandrecode.tempo.features.tasks.domain.repository

import com.mandrecode.tempo.features.tasks.domain.model.SectionDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import kotlinx.coroutines.flow.Flow

interface TaskSectionRepository {
    fun getAllSections(): Flow<List<TaskSection>>

    suspend fun getSectionsByCategory(categoryId: Long): List<TaskSection>

    suspend fun getSectionById(id: Long): TaskSection?

    /**
     * Inserts [name] into [categoryId] and returns its id: last, or right below [afterSectionId]
     * when that is a section of the category, all in one transaction.
     */
    suspend fun createSection(
        categoryId: Long,
        name: String,
        afterSectionId: Long? = null,
    ): Long

    suspend fun renameSection(
        id: Long,
        name: String,
    )

    /** Rewrites every listed section's position to its index, in one transaction. */
    suspend fun updateSortOrders(orderedIds: List<Long>)

    /** Unsections the section's tasks and deletes it, in one transaction; null when it is gone. */
    suspend fun deleteSectionWithSnapshot(id: Long): SectionDeletionSnapshot?

    /** Re-inserts the section and re-adds the snapshot tasks still in its category. */
    suspend fun restoreDeletedSection(snapshot: SectionDeletionSnapshot)

    /**
     * Moves top-level task [taskId] into [sectionId] (null = unsectioned) at [index] among the
     * active tasks already there, renumbering them. Returns false when the task does not exist
     * or the section belongs to another category.
     */
    suspend fun moveTaskToSection(
        taskId: Long,
        sectionId: Long?,
        index: Int,
    ): Boolean
}
