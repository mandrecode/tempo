package com.mandrecode.tempo.features.backup.domain.repository

import com.mandrecode.tempo.features.backup.domain.model.ImportMode
import com.mandrecode.tempo.features.backup.domain.model.ImportOutcome

interface BackupRepository {
    /** Serializes the full local dataset and encrypts it with a key derived from [passphrase]. */
    suspend fun exportEncrypted(passphrase: CharArray): String

    /** True when [content] is this app's encrypted backup envelope rather than a legacy plaintext export. */
    fun isEncryptedBackup(content: String): Boolean

    /**
     * Applies a backup document to the local database. [passphrase] is required when
     * [content] is an encrypted envelope (see [isEncryptedBackup]) and ignored otherwise;
     * a wrong passphrase surfaces as [ImportOutcome.WrongPassphrase]. Decoding, decryption,
     * version check and payload validation all happen before any write; database mutations
     * run in a single transaction, so any non-[ImportOutcome.Success] result (or thrown
     * error) leaves local data untouched.
     */
    suspend fun importFromJson(
        content: String,
        mode: ImportMode,
        passphrase: CharArray? = null,
    ): ImportOutcome
}
