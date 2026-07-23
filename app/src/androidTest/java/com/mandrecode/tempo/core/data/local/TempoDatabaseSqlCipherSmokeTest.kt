package com.mandrecode.tempo.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.CategoryEntity
import com.mandrecode.tempo.core.data.local.security.SqlCipherKdfIter
import kotlinx.coroutines.test.runTest
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.security.SecureRandom

/**
 * Exercises the same Room + SQLCipher wiring as [TempoDatabase.getDatabase] end-to-end, using an
 * isolated database name so it never touches the shared app database other instrumented tests
 * may rely on.
 */
@RunWith(AndroidJUnit4::class)
class TempoDatabaseSqlCipherSmokeTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dbName = "sqlcipher_smoke_test_db"
    private var database: TempoDatabase? = null

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun sqlCipherBackedRoomDatabase_opensWritesReadsAndCloses() =
        runTest {
            val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
            val db =
                Room
                    .databaseBuilder(context, TempoDatabase::class.java, dbName)
                    .openHelperFactory(
                        SupportOpenHelperFactory(
                            passphrase,
                            SqlCipherKdfIter.hookFor(SqlCipherKdfIter.CURRENT),
                            false,
                        ),
                    ).addMigrations(*TempoDatabase.MIGRATIONS)
                    .build()
            database = db

            val categoryId = db.categoryDao().insertCategory(CategoryEntity(name = "Smoke Test", sortOrder = 0))
            val categories = db.categoryDao().getAllCategoriesSync()

            assertThat(categories.any { it.id == categoryId }).isTrue()
            assertThat(db.isOpen).isTrue()

            db.close()
            database = null
            assertThat(db.isOpen).isFalse()
        }
}
