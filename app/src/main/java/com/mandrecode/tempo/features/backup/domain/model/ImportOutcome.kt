package com.mandrecode.tempo.features.backup.domain.model

/**
 * Terminal result of an import attempt. Every non-[Success] outcome guarantees
 * the local database was left untouched.
 */
sealed interface ImportOutcome {
    data class Success(
        val summary: ImportSummary,
    ) : ImportOutcome

    /** The file's schema version is newer than this app understands. */
    data class UnsupportedVersion(
        val fileVersion: Int,
        val maxSupported: Int,
    ) : ImportOutcome

    /** The file is not valid JSON or does not match the expected structure. */
    data object CorruptFile : ImportOutcome

    /** The file is an encrypted backup and the supplied passphrase could not decrypt it. */
    data object WrongPassphrase : ImportOutcome

    /** The payload parsed but is internally inconsistent. */
    data class ValidationFailed(
        val issues: List<ValidationIssue>,
    ) : ImportOutcome
}

/** A single payload-integrity problem found during import validation. */
data class ValidationIssue(
    val kind: ValidationIssueKind,
    /** Human-oriented identifier of the offending record (title or id). */
    val detail: String,
)

enum class ValidationIssueKind {
    DUPLICATE_ID,
    UNKNOWN_CATEGORY_REFERENCE,
    UNKNOWN_PARENT_TASK_REFERENCE,
    UNKNOWN_CHAIN_REFERENCE,
    UNKNOWN_HABIT_REFERENCE,
}
