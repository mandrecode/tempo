package com.mandrecode.tempo.core.data.local.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import net.zetetic.database.sqlcipher.SQLiteNotADatabaseException
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.SecureRandom
import net.zetetic.database.sqlcipher.SQLiteDatabase as CipherSQLiteDatabase

/**
 * Covers [DatabaseEncryptionMigrator]'s second responsibility — re-keying an already-encrypted
 * database from [SqlCipherKdfIter.LEGACY] up to [SqlCipherKdfIter.CURRENT] — delegated to
 * [KdfIterRekeyer]. Exercised through [DatabaseEncryptionMigrator.migrateIfNeeded], the same
 * single entry point production code calls, to match [DatabaseEncryptionMigratorTest]'s style for
 * the plaintext-migration side of the same class.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionMigratorKdfRekeyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val migrator = DatabaseEncryptionMigrator(context)
    private val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
    private val dbName = "kdf_rekey_test_db"

    private fun dbFile(): File = context.getDatabasePath(dbName)

    private fun rekeyNewFile() = File(dbFile().parentFile, "${dbFile().name}.kdf_rekey.new")

    private fun rekeyBackupFile() = File(dbFile().parentFile, "${dbFile().name}.kdf_rekey.bak")

    @Before
    fun setUp() = deleteArtifacts()

    @After
    fun tearDown() = deleteArtifacts()

    private fun deleteArtifacts() {
        dbFile().delete()
        rekeyNewFile().delete()
        rekeyBackupFile().delete()
        File(dbFile().parentFile, "${dbFile().name}-wal").delete()
        File(dbFile().parentFile, "${dbFile().name}-shm").delete()
        // DbKdfIterMarker isn't scoped per database name — clear it so another test class's
        // marker for a different dbName can't leak in here (see its own doc).
        DbKdfIterMarker.clear(context)
    }

    /** A genuine SQLCipher-encrypted file keyed at the legacy (pre-this-change) kdf_iter. */
    private fun seedLegacyKdfIterDatabase() {
        val db =
            CipherSQLiteDatabase.openDatabase(
                dbFile().absolutePath,
                passphrase,
                null,
                CipherSQLiteDatabase.OPEN_READWRITE or CipherSQLiteDatabase.CREATE_IF_NECESSARY,
                SqlCipherKdfIter.hookFor(SqlCipherKdfIter.LEGACY),
            )
        db.execSQL("CREATE TABLE t1(a TEXT, b TEXT)")
        db.execSQL("INSERT INTO t1(a, b) VALUES ('one for the money', 'two for the show')")
        db.close()
    }

    private fun seedCurrentKdfIterDatabase() {
        val db =
            CipherSQLiteDatabase.openDatabase(
                dbFile().absolutePath,
                passphrase,
                null,
                CipherSQLiteDatabase.OPEN_READWRITE or CipherSQLiteDatabase.CREATE_IF_NECESSARY,
                SqlCipherKdfIter.hookFor(SqlCipherKdfIter.CURRENT),
            )
        db.execSQL("CREATE TABLE t1(a TEXT, b TEXT)")
        db.execSQL("INSERT INTO t1(a, b) VALUES ('one for the money', 'two for the show')")
        db.close()
    }

    private fun isOpenableAt(kdfIter: Int): Boolean =
        try {
            val db =
                CipherSQLiteDatabase.openDatabase(
                    dbFile().absolutePath,
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
            false
        }

    private fun readRowAt(kdfIter: Int): Pair<String, String> {
        val db =
            CipherSQLiteDatabase.openDatabase(
                dbFile().absolutePath,
                passphrase,
                null,
                CipherSQLiteDatabase.OPEN_READONLY,
                SqlCipherKdfIter.hookFor(kdfIter),
            )
        try {
            val cursor = db.rawQuery("SELECT a, b FROM t1", null)
            try {
                assertThat(cursor.moveToFirst()).isTrue()
                return cursor.getString(0) to cursor.getString(1)
            } finally {
                cursor.close()
            }
        } finally {
            db.close()
        }
    }

    @Test
    fun migrateIfNeeded_reKeysLegacyKdfIterDatabase_preservingDataAndDroppingLegacyAccess() =
        runTest {
            seedLegacyKdfIterDatabase()
            assertThat(isOpenableAt(SqlCipherKdfIter.LEGACY)).isTrue()

            migrator.migrateIfNeeded(dbName, passphrase)

            assertThat(isOpenableAt(SqlCipherKdfIter.CURRENT)).isTrue()
            assertThat(isOpenableAt(SqlCipherKdfIter.LEGACY)).isFalse()
            val (a, b) = readRowAt(SqlCipherKdfIter.CURRENT)
            assertThat(a).isEqualTo("one for the money")
            assertThat(b).isEqualTo("two for the show")
        }

    @Test
    fun migrateIfNeeded_isIdempotent_secondCallLeavesDataIntact() =
        runTest {
            seedLegacyKdfIterDatabase()

            migrator.migrateIfNeeded(dbName, passphrase)
            migrator.migrateIfNeeded(dbName, passphrase)

            val (a, b) = readRowAt(SqlCipherKdfIter.CURRENT)
            assertThat(a).isEqualTo("one for the money")
            assertThat(b).isEqualTo("two for the show")
            assertThat(rekeyNewFile().exists()).isFalse()
            assertThat(rekeyBackupFile().exists()).isFalse()
        }

    @Test
    fun migrateIfNeeded_leavesAlreadyCurrentKdfIterDatabaseOpenable() =
        runTest {
            seedCurrentKdfIterDatabase()

            migrator.migrateIfNeeded(dbName, passphrase)

            val (a, b) = readRowAt(SqlCipherKdfIter.CURRENT)
            assertThat(a).isEqualTo("one for the money")
            assertThat(b).isEqualTo("two for the show")
        }

    @Test
    fun migrateIfNeeded_leavesFileUntouchedWhenPassphraseIsWrong() =
        runTest {
            seedLegacyKdfIterDatabase()
            val wrongPassphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }

            // Must not throw: neither kdf_iter probe opens this file with the wrong passphrase,
            // so this is a no-op that leaves the real failure to surface through the normal
            // TempoDatabase.getDatabase() open path instead.
            migrator.migrateIfNeeded(dbName, wrongPassphrase)

            assertThat(isOpenableAt(SqlCipherKdfIter.LEGACY)).isTrue()
            assertThat(rekeyNewFile().exists()).isFalse()
            assertThat(rekeyBackupFile().exists()).isFalse()
        }

    @Test
    fun migrateIfNeeded_discardsOrphanedRekeyNewFileFromAnInterruptedAttempt() =
        runTest {
            seedLegacyKdfIterDatabase()
            rekeyNewFile().writeText("garbage left behind by a killed process mid-export")

            migrator.migrateIfNeeded(dbName, passphrase)

            val (a, _) = readRowAt(SqlCipherKdfIter.CURRENT)
            assertThat(a).isEqualTo("one for the money")
            assertThat(rekeyNewFile().exists()).isFalse()
        }

    @Test
    fun migrateIfNeeded_recoversWhenInterruptedBetweenRekeySwapRenames() =
        runTest {
            seedLegacyKdfIterDatabase()
            migrator.migrateIfNeeded(dbName, passphrase)

            // Simulate a crash between the two renames of a future re-key attempt: the primary
            // path is empty, and a backup file holding the already-rekeyed database is left
            // behind — exactly SqlCipherFileSwap.swapInPlace's state right after the first
            // rename but before the second.
            assertThat(dbFile().renameTo(rekeyBackupFile())).isTrue()

            migrator.migrateIfNeeded(dbName, passphrase)

            assertThat(dbFile().exists()).isTrue()
            assertThat(rekeyBackupFile().exists()).isFalse()
            val (a, b) = readRowAt(SqlCipherKdfIter.CURRENT)
            assertThat(a).isEqualTo("one for the money")
            assertThat(b).isEqualTo("two for the show")
        }
}
