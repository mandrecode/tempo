package com.mandrecode.tempo.core.data.local.security

import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteNotADatabaseException
import java.io.File
import net.zetetic.database.sqlcipher.SQLiteDatabase as CipherSQLiteDatabase

/**
 * Re-keys an already-encrypted database still at [SqlCipherKdfIter.LEGACY] (every Tempo install
 * created before that constant was lowered) up to [SqlCipherKdfIter.CURRENT], exactly once.
 *
 * Split out of [DatabaseEncryptionMigrator] purely to stay under detekt's per-class function
 * limit — the two share the exact same crash-safety contract (see that class's doc: the original
 * file is never touched until a verified replacement exists at a temp path) and are always
 * driven from the same `DatabaseEncryptionMigrator.migrateIfNeeded` call, in the same order:
 * [recoverFromInterruption] before anything else touches the file, then [rekeyIfNeeded].
 *
 * Uses its own file suffixes (`.kdf_rekey.new` / `.kdf_rekey.bak`), distinct from
 * [DatabaseEncryptionMigrator]'s plaintext-migration ones, so the two recovery paths can never
 * collide with or be mistaken for each other — the two conversions are mutually exclusive per
 * call (`isPlaintextSqlite` routes to exactly one), but distinct names keep that guarantee
 * obvious by inspection, not just by logic.
 */
internal class KdfIterRekeyer(
    private val context: Context,
) {
    /**
     * An absent [DbKdfIterMarker] is always safe — falls through to actually probing the file at
     * [SqlCipherKdfIter.CURRENT] then [SqlCipherKdfIter.LEGACY]. A marker that already says
     * `CURRENT` is trusted outright with no re-verification, by design: see [DbKdfIterMarker]'s
     * own doc for why that's safe under normal operation and what "stale" would actually require.
     */
    fun rekeyIfNeeded(
        dbFile: File,
        passphrase: ByteArray,
    ) {
        if (DbKdfIterMarker.readOrNull(context) == SqlCipherKdfIter.CURRENT) return

        if (isOpenableAt(dbFile, passphrase, SqlCipherKdfIter.CURRENT)) {
            DbKdfIterMarker.write(context, SqlCipherKdfIter.CURRENT)
            return
        }

        rekeyFromLegacyIfOpenable(dbFile, passphrase)
    }

    private fun rekeyFromLegacyIfOpenable(
        dbFile: File,
        passphrase: ByteArray,
    ) {
        // Neither CURRENT nor LEGACY opens this file: not a kdf_iter mismatch this migrator
        // knows how to fix (wrong passphrase, corruption, etc.). Leave it alone and let
        // TempoDatabase.getDatabase()'s normal Room open surface the real failure, rather than
        // risk masking it behind a re-key attempt built on a wrong assumption.
        if (!isOpenableAt(dbFile, passphrase, SqlCipherKdfIter.LEGACY)) return

        val rekeyedFile = rekeyFileFor(dbFile)
        rekeyedFile.delete()
        exportRekeyed(dbFile, rekeyedFile, passphrase)
        SqlCipherFileSwap.verifyEncrypted(rekeyedFile, passphrase, SqlCipherKdfIter.CURRENT)
        SqlCipherFileSwap.swapInPlace(dbFile, rekeyedFile, rekeyBackupFileFor(dbFile))
        DbKdfIterMarker.write(context, SqlCipherKdfIter.CURRENT)
    }

    private fun isOpenableAt(
        file: File,
        passphrase: ByteArray,
        kdfIter: Int,
    ): Boolean =
        try {
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
                    cursor.moveToFirst()
                } finally {
                    cursor.close()
                }
            } finally {
                db.close()
            }
            true
        } catch (_: SQLiteNotADatabaseException) {
            // The specific, narrow signal for "this key/kdf_iter combination didn't decrypt this
            // file" (SQLITE_NOTADB) — anything else (disk I/O, a locked file) must keep
            // propagating rather than being misread as "must be the other kdf_iter."
            false
        }

    /**
     * Re-keys [source] (already encrypted, opened at [SqlCipherKdfIter.LEGACY]) into a new file
     * at [SqlCipherKdfIter.CURRENT] via the same `sqlcipher_export()` mechanism
     * [DatabaseEncryptionMigrator] uses for the plaintext migration — just starting from a keyed
     * connection instead of an unkeyed one.
     */
    private fun exportRekeyed(
        source: File,
        target: File,
        passphrase: ByteArray,
    ) {
        val sourceDb =
            CipherSQLiteDatabase.openDatabase(
                source.absolutePath,
                passphrase,
                null,
                // CREATE_IF_NECESSARY here too, for the same reason the plaintext migration's
                // export needs it on its (also already-existing) source file: ATTACHing a *new*
                // database inherits whether this connection may create files from how the main
                // connection itself was opened.
                CipherSQLiteDatabase.OPEN_READWRITE or CipherSQLiteDatabase.CREATE_IF_NECESSARY,
                SqlCipherKdfIter.hookFor(SqlCipherKdfIter.LEGACY),
            )
        try {
            sourceDb.execSQL(
                "ATTACH DATABASE ? AS rekeyed KEY ?;",
                arrayOf<Any>(target.absolutePath, passphrase),
            )
            sourceDb.execSQL("PRAGMA rekeyed.kdf_iter = ${SqlCipherKdfIter.CURRENT};")
            sourceDb.rawExecSQL("SELECT sqlcipher_export('rekeyed');")
            sourceDb.execSQL("DETACH DATABASE rekeyed;")
        } finally {
            sourceDb.close()
        }
    }

    /** Mirrors [DatabaseEncryptionMigrator]'s own recovery, for this class's temp/backup files. */
    fun recoverFromInterruption(dbFile: File) {
        val backupFile = rekeyBackupFileFor(dbFile)
        if (!dbFile.exists() && backupFile.exists()) {
            backupFile.renameTo(dbFile)
        } else if (backupFile.exists()) {
            SqlCipherFileSwap.deleteBackupAndSidecars(dbFile, backupFile)
        }
        rekeyFileFor(dbFile).delete()
    }

    private fun rekeyFileFor(dbFile: File) = File(dbFile.parentFile, "${dbFile.name}.kdf_rekey.new")

    private fun rekeyBackupFileFor(dbFile: File) = File(dbFile.parentFile, "${dbFile.name}.kdf_rekey.bak")
}
