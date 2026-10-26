package com.mandrecode.tempo.core.data.entity

import androidx.room.ColumnInfo
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
    /**
     * Whether this category's list is split into sections (#61). Off by default: opt-in. The SQL
     * default matters as much as the Kotlin one: raw inserts that predate the column, such as the
     * fresh-install Inbox seed, must still succeed, and it keeps a new install matching 11 -> 12.
     */
    @ColumnInfo(defaultValue = "0")
    val sectionsEnabled: Boolean = false,
)

val DEFAULT_INBOX_CATEGORY_ENTITY =
    CategoryEntity(id = -1, name = "Inbox", icon = "inbox", isDefault = true, sortOrder = -1)
