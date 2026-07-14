package com.mandrecode.tempo.core.data

internal class RestoreConflictException(
    recordDescription: String,
) : IllegalStateException("Restore conflict for $recordDescription")

internal suspend fun <T> insertOrVerifyRestoredEntity(
    existing: T?,
    snapshot: T,
    recordDescription: String,
    insert: suspend () -> Unit,
) {
    when {
        existing == null -> insert()
        existing != snapshot -> throw RestoreConflictException(recordDescription)
    }
}
