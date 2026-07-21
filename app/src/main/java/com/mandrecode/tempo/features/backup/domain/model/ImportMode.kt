package com.mandrecode.tempo.features.backup.domain.model

/**
 * How an imported backup is applied to the local database.
 *
 * - [REPLACE] wipes current data and restores the file exactly (backup/recovery path).
 * - [MERGE] adds the file's contents to existing data, skipping exact duplicates
 *   and reporting conflicts.
 */
enum class ImportMode {
    REPLACE,
    MERGE,
}
