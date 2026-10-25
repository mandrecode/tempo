package com.mandrecode.tempo.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mandrecode.tempo.core.data.entity.TaskAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskAttachmentDao {
    @Query("SELECT * FROM task_attachments WHERE taskId = :taskId ORDER BY id ASC")
    fun observeForTask(taskId: Long): Flow<List<TaskAttachmentEntity>>

    @Query("SELECT * FROM task_attachments WHERE taskId = :taskId ORDER BY id ASC")
    suspend fun getForTask(taskId: Long): List<TaskAttachmentEntity>

    @Insert
    suspend fun insertAll(attachments: List<TaskAttachmentEntity>)

    @Query("DELETE FROM task_attachments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM task_attachments WHERE storedName = :storedName")
    suspend fun countByStoredName(storedName: String): Int

    @Query("SELECT DISTINCT storedName FROM task_attachments")
    suspend fun getAllStoredNames(): List<String>

    @Query("DELETE FROM task_attachments")
    suspend fun deleteAll()
}
