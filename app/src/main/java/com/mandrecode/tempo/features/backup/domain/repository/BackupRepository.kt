package com.mandrecode.tempo.features.backup.domain.repository

import com.mandrecode.tempo.features.backup.domain.model.ImportMode
import com.mandrecode.tempo.features.backup.domain.model.ImportOutcome

interface BackupRepository {
    /** Serializes the full local dataset into a schema-versioned JSON document. */
    suspend fun exportToJson(): String

    /**
     * Applies a backup document to the local database. Decoding, version check
     * and payload validation all happen before any write; database mutations
     * run in a single transaction, so any non-[ImportOutcome.Success] result
     * (or thrown error) leaves local data untouched.
     */
    suspend fun importFromJson(
        json: String,
        mode: ImportMode,
    ): ImportOutcome
}
