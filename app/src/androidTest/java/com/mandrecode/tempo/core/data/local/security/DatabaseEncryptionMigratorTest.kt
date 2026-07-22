package com.mandrecode.tempo.core.data.local.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.SecureRandom
import net.zetetic.database.sqlcipher.SQLiteDatabase as CipherSQLiteDatabase

@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionMigratorTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val migrator = DatabaseEncryptionMigrator(context)
    private val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
    private val dbName = "migration_test_db"

    private fun dbFile(): File = context.getDatabasePath(dbName)

    private fun newFile() = File(dbFile().parentFile, "${dbFile().name}.new")

    private fun backupFile() = File(dbFile().parentFile, "${dbFile().name}.plaintext.bak")

    @Before
    fun setUp() = deleteArtifacts()

    @After
    fun tearDown() = deleteArtifacts()

    private fun deleteArtifacts() {
        dbFile().delete()
        newFile().delete()
        backupFile().delete()
        File(dbFile().parentFile, "${dbFile().name}-wal").delete()
        File(dbFile().parentFile, "${dbFile().name}-shm").delete()
    }

    /** A genuine plaintext SQLite file, created via the plain Android framework SQLite API. */
    private fun seedPlaintextDatabase() {
        val db =
            android.database.sqlite.SQLiteDatabase
                .openOrCreateDatabase(dbFile(), null)
        db.execSQL("CREATE TABLE t1(a TEXT, b TEXT)")
        db.execSQL("INSERT INTO t1(a, b) VALUES ('one for the money', 'two for the show')")
        db.close()
    }

    private fun readRowFromEncrypted(): Pair<String, String> {
        val db =
            CipherSQLiteDatabase.openDatabase(
                dbFile().absolutePath,
                passphrase,
                null,
                CipherSQLiteDatabase.OPEN_READONLY,
                null,
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
    fun migrateIfNeeded_convertsPlaintextDatabaseToEncrypted_preservingData() =
        runTest {
            seedPlaintextDatabase()

            migrator.migrateIfNeeded(dbName, passphrase)

            val (a, b) = readRowFromEncrypted()
            assertThat(a).isEqualTo("one for the money")
            assertThat(b).isEqualTo("two for the show")
        }

    @Test
    fun migrateIfNeeded_removesStalePlaintextWalAndShmSidecars() =
        runTest {
            seedPlaintextDatabase()
            // A file rename doesn't rename its WAL/SHM sidecars — they stay under the
            // *original* name, not the `.plaintext.bak` name swapInPlace() renames the main
            // file to. Simulate leftover sidecars from the plaintext database explicitly.
            val staleWal = File(dbFile().parentFile, "${dbFile().name}-wal")
            val staleShm = File(dbFile().parentFile, "${dbFile().name}-shm")
            staleWal.writeText("stale plaintext WAL frames")
            staleShm.writeText("stale plaintext shared-memory file")

            migrator.migrateIfNeeded(dbName, passphrase)

            assertThat(staleWal.exists()).isFalse()
            assertThat(staleShm.exists()).isFalse()
        }

    @Test
    fun migrateIfNeeded_freshInstallWithNoFile_doesNothing() =
        runTest {
            migrator.migrateIfNeeded(dbName, passphrase)

            assertThat(dbFile().exists()).isFalse()
        }

    @Test
    fun migrateIfNeeded_isIdempotent_secondCallLeavesDataIntact() =
        runTest {
            seedPlaintextDatabase()
            migrator.migrateIfNeeded(dbName, passphrase)

            migrator.migrateIfNeeded(dbName, passphrase)

            val (a, b) = readRowFromEncrypted()
            assertThat(a).isEqualTo("one for the money")
            assertThat(b).isEqualTo("two for the show")
        }

    @Test
    fun migrateIfNeeded_discardsOrphanedNewFileFromAnInterruptedAttempt() =
        runTest {
            seedPlaintextDatabase()
            newFile().writeText("garbage left behind by a killed process mid-export")

            migrator.migrateIfNeeded(dbName, passphrase)

            val (a, _) = readRowFromEncrypted()
            assertThat(a).isEqualTo("one for the money")
            assertThat(newFile().exists()).isFalse()
        }

    @Test
    fun migrateIfNeeded_recoversWhenInterruptedBetweenSwapRenames() =
        runTest {
            seedPlaintextDatabase()
            migrator.migrateIfNeeded(dbName, passphrase)

            // Simulate a crash between the two renames of a future migration attempt: the
            // primary path is empty, and a backup file holding the (already-encrypted) database
            // is left behind.
            assertThat(dbFile().renameTo(backupFile())).isTrue()

            migrator.migrateIfNeeded(dbName, passphrase)

            assertThat(dbFile().exists()).isTrue()
            assertThat(backupFile().exists()).isFalse()
            val (a, b) = readRowFromEncrypted()
            assertThat(a).isEqualTo("one for the money")
            assertThat(b).isEqualTo("two for the show")
        }

    @Test
    fun migrateIfNeeded_cleansUpLeftoverPlaintextBackupAfterASuccessfulSwap() =
        runTest {
            seedPlaintextDatabase()
            migrator.migrateIfNeeded(dbName, passphrase)

            // Simulate a crash *after* both swap renames succeeded but *before* the leftover
            // plaintext backup (and its stale sidecars, still named after the primary db) were
            // deleted: recreate that backup file plus fake sidecars alongside the now-current
            // (already encrypted) primary db file.
            assertThat(dbFile().exists()).isTrue()
            backupFile().writeText("leftover plaintext copy of the user's data")
            val staleWal = File(dbFile().parentFile, "${dbFile().name}-wal")
            val staleShm = File(dbFile().parentFile, "${dbFile().name}-shm")
            staleWal.writeText("stale plaintext WAL frames")
            staleShm.writeText("stale plaintext shared-memory file")

            migrator.migrateIfNeeded(dbName, passphrase)

            assertThat(backupFile().exists()).isFalse()
            assertThat(staleWal.exists()).isFalse()
            assertThat(staleShm.exists()).isFalse()
            val (a, b) = readRowFromEncrypted()
            assertThat(a).isEqualTo("one for the money")
            assertThat(b).isEqualTo("two for the show")
        }
}
