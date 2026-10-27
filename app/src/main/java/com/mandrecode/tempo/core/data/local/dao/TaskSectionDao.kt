package com.mandrecode.tempo.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mandrecode.tempo.core.data.entity.TaskSectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskSectionDao {
    @Query("SELECT * FROM task_sections ORDER BY categoryId ASC, sortOrder ASC, id ASC")
    fun getAllSections(): Flow<List<TaskSectionEntity>>

    @Query("SELECT * FROM task_sections WHERE categoryId = :categoryId ORDER BY sortOrder ASC, id ASC")
    suspend fun getSectionsByCategorySync(categoryId: Long): List<TaskSectionEntity>

    @Query("SELECT * FROM task_sections WHERE id = :id")
    suspend fun getSectionById(id: Long): TaskSectionEntity?

    @Query("SELECT MAX(sortOrder) FROM task_sections WHERE categoryId = :categoryId")
    suspend fun getMaxSortOrder(categoryId: Long): Int?

    @Insert
    suspend fun insertSection(section: TaskSectionEntity): Long

    @Query("UPDATE task_sections SET name = :name WHERE id = :id")
    suspend fun renameSection(
        id: Long,
        name: String,
    )

    @Query("UPDATE task_sections SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(
        id: Long,
        sortOrder: Int,
    )

    @Delete
    suspend fun deleteSection(section: TaskSectionEntity)

    @Query("DELETE FROM task_sections WHERE categoryId = :categoryId")
    suspend fun deleteSectionsByCategory(categoryId: Long)

    /** Every section, for backups. */
    @Query("SELECT * FROM task_sections ORDER BY id ASC")
    suspend fun getAllSectionsSync(): List<TaskSectionEntity>

    @Query("DELETE FROM task_sections")
    suspend fun deleteAll()
}
