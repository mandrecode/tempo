package com.mandrecode.tempo.features.tasks.domain.model

/**
 * A named divider inside one category (#61) — structure for a long list, not a list of its own.
 * Not completable, and carries no icon, colour or sharing identity.
 */
data class TaskSection(
    val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val sortOrder: Int = 0,
)

/**
 * What deleting a section took away, so undo can put it back: the section itself and the
 * top-level tasks that were in it (completed ones included).
 */
data class SectionDeletionSnapshot(
    val section: TaskSection,
    val taskIds: List<Long>,
)
