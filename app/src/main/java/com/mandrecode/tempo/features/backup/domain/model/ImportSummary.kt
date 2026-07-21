package com.mandrecode.tempo.features.backup.domain.model

/**
 * Post-import report shown to the user: how many records were imported,
 * skipped as exact duplicates, or held back as conflicts, per entity kind.
 */
data class ImportSummary(
    val categories: ImportCounts = ImportCounts(),
    val tasks: ImportCounts = ImportCounts(),
    val habits: ImportCounts = ImportCounts(),
    val habitChains: ImportCounts = ImportCounts(),
    val conflicts: List<ImportConflict> = emptyList(),
) {
    val totalImported: Int
        get() = categories.imported + tasks.imported + habits.imported + habitChains.imported

    val totalSkipped: Int
        get() = categories.skipped + tasks.skipped + habits.skipped + habitChains.skipped

    val totalConflicts: Int
        get() = categories.conflicts + tasks.conflicts + habits.conflicts + habitChains.conflicts
}

data class ImportCounts(
    val imported: Int = 0,
    val skipped: Int = 0,
    val conflicts: Int = 0,
)

/**
 * A record that matched a local record by natural key but differed in content
 * (or depended on such a record) and was therefore not imported.
 */
data class ImportConflict(
    val kind: BackupEntityKind,
    val displayName: String,
    val reason: ConflictReason,
)

enum class BackupEntityKind {
    CATEGORY,
    TASK,
    HABIT,
    HABIT_CHAIN,
}

enum class ConflictReason {
    /** Natural key matched a local record whose content differs. */
    CONTENT_MISMATCH,

    /** The record depends on a conflicted record (e.g. subtask of a conflicted task). */
    PARENT_CONFLICTED,
}
