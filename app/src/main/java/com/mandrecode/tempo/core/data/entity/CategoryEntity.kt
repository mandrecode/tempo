package com.mandrecode.tempo.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String? = null,
    val icon: String? = null,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
)

val DEFAULT_INBOX_CATEGORY_ENTITY =
    CategoryEntity(id = -1, name = "Inbox", icon = "inbox", isDefault = true, sortOrder = -1)
