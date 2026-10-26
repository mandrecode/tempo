package com.mandrecode.tempo.features.tasks.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val color: String? = null,
    val icon: String? = null,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    /**
     * Whether the list is split into sections (#61). Turning it off hides them without deleting
     * anything, so turning it back on restores every section and membership.
     */
    val sectionsEnabled: Boolean = false,
)
