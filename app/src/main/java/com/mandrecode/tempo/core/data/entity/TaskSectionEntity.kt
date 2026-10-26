package com.mandrecode.tempo.core.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A divider inside one category (#61). Deliberately cheaper than a category: no icon, colour,
 * default flag or sharing identity, and nothing to complete. Tasks point at it through
 * [TaskEntity.sectionId]; there are no foreign keys, so the repository keeps the two consistent.
 */
@Entity(
    tableName = "task_sections",
    indices = [Index("categoryId")],
)
data class TaskSectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val sortOrder: Int = 0,
)
