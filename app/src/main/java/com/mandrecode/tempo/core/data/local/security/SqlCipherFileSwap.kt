package com.mandrecode.tempo.core.data.local.security

import java.io.File
import net.zetetic.database.sqlcipher.SQLiteDatabase as CipherSQLiteDatabase

/**
 * Crash-safe swap-in-place mechanics shared by [DatabaseEncryptionMigrator] and [KdfIterRekeyer]:
 * rename the original aside, rename the replacement into place, then clean up the backup and its
 * stale WAL/SHM sidecars (a rename doesn't carry those along — they stay under the *original*
 * name on disk, so that's the name they must be cleaned up by, not `backup.name`).
 */
internal object SqlCipherFileSwap {
    fun swapInPlace(
        original: File,
        replacement: File,
        backup: File,
    ) {
        backup.delete()
        check(original.renameTo(backup)) { "Failed to move aside the original database" }
        check(replacement.renameTo(original)) { "Failed to move the new database into place" }
        deleteBackupAndSidecars(original, backup)
    }

    fun deleteBackupAndSidecars(
        original: File,
        backup: File,
    ) {
        backup.delete()
        File(backup.parentFile, "${original.name}-wal").delete()
        File(backup.parentFile, "${original.name}-shm").delete()
    }

    fun verifyEncrypted(
        file: File,
        passphrase: ByteArray,
        kdfIter: Int,
    ) {
        val db =
            CipherSQLiteDatabase.openDatabase(
                file.absolutePath,
                passphrase,
                null,
                CipherSQLiteDatabase.OPEN_READONLY,
                SqlCipherKdfIter.hookFor(kdfIter),
            )
        try {
            val cursor = db.rawQuery("SELECT count(*) FROM sqlite_master", null)
            try {
                check(cursor.moveToFirst()) { "Encrypted database verification query returned no rows" }
            } finally {
                cursor.close()
            }
        } finally {
            db.close()
        }
    }
}
