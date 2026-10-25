package com.mandrecode.tempo.core.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * No foreign key to `tasks`: a deleted task keeps its attachment rows so that undoing the
 * deletion restores them too. Rows left behind are pruned at startup (see
 * TaskAttachmentRepository.pruneOrphans).
 */
@Entity(
    tableName = "task_attachments",
    indices = [Index("taskId"), Index("storedName")],
)
data class TaskAttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val displayName: String,
    val mimeType: String?,
    val sizeBytes: Long,
    val storedName: String,
)
