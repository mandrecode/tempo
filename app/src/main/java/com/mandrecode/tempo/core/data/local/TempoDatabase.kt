package com.mandrecode.tempo.core.data.local

import android.content.ContentValues
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.entity.CategoryEntity
import com.mandrecode.tempo.core.data.entity.Converters
import com.mandrecode.tempo.core.data.entity.HabitChainEntity
import com.mandrecode.tempo.core.data.entity.HabitChainMemberEntity
import com.mandrecode.tempo.core.data.entity.HabitEntity
import com.mandrecode.tempo.core.data.entity.TaskEntity
import com.mandrecode.tempo.core.data.local.dao.CategoryDao
import com.mandrecode.tempo.core.data.local.dao.HabitChainDao
import com.mandrecode.tempo.core.data.local.dao.HabitChainMemberDao
import com.mandrecode.tempo.core.data.local.dao.HabitDao
import com.mandrecode.tempo.core.data.local.dao.TaskDao
import com.mandrecode.tempo.core.data.local.security.DatabaseEncryptionMigrator
import com.mandrecode.tempo.core.data.local.security.DbKdfIterMarker
import com.mandrecode.tempo.core.data.local.security.DbPassphraseProvider
import com.mandrecode.tempo.core.data.local.security.SqlCipherKdfIter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        TaskEntity::class,
        CategoryEntity::class,
        HabitEntity::class,
        HabitChainEntity::class,
        HabitChainMemberEntity::class,
    ],
    version = 9,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class TempoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun categoryDao(): CategoryDao

    abstract fun habitDao(): HabitDao

    abstract fun habitChainDao(): HabitChainDao

    abstract fun habitChainMemberDao(): HabitChainMemberDao

    companion object {
        const val DATABASE_NAME = "tempo_database"

        @Volatile
        @Suppress("ktlint:standard:property-naming")
        private var INSTANCE: TempoDatabase? = null
        private val instanceMutex = Mutex()

        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN completedAt TEXT")
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE categories ADD COLUMN color TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE categories ADD COLUMN icon TEXT DEFAULT NULL")
                    db.execSQL(
                        "ALTER TABLE categories ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0",
                    )
                    db.execSQL(
                        "ALTER TABLE categories ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0",
                    )
                }
            }

        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO categories (id, name, color, icon, isDefault, sortOrder)
                        VALUES (-1, 'Inbox', NULL, 'inbox', 1, -1)
                        """.trimIndent(),
                    )
                }
            }

        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "UPDATE categories SET icon = 'inbox' WHERE id = -1 AND icon IS NULL",
                    )
                }
            }

        val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE tasks ADD COLUMN periodicityInterval INTEGER NOT NULL DEFAULT 1",
                    )
                    db.execSQL(
                        "ALTER TABLE tasks ADD COLUMN repeatDays TEXT DEFAULT NULL",
                    )
                    db.execSQL(
                        "ALTER TABLE tasks ADD COLUMN monthDayOption TEXT DEFAULT NULL",
                    )
                }
            }

        /**
         * Replaces the unused `isInverted` (INTEGER) column with `habitType` (TEXT).
         * Requires a table rebuild because SQLite does not support ALTER COLUMN.
         * Maps `isInverted = 1` → "QUIT", everything else → "BUILD".
         */
        val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // habit_chain_members has a FK on habits(id); drop+recreate must run with
                    // FKs disabled to avoid violations during the table swap.
                    db.execSQL("PRAGMA foreign_keys=OFF")
                    db.beginTransaction()
                    try {
                        // Defensively clear any leftover habits_new table from a previously
                        // interrupted migration run. Without this, CREATE TABLE could silently
                        // no-op (with IF NOT EXISTS) or fail loudly mid-transaction; either
                        // way the subsequent INSERT could corrupt or duplicate rows.
                        db.execSQL("DROP TABLE IF EXISTS habits_new")
                        db.execSQL(
                            """
                            CREATE TABLE habits_new (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                title TEXT NOT NULL,
                                description TEXT NOT NULL,
                                icon TEXT,
                                colorKey TEXT,
                                reminderDate TEXT,
                                isCompleted INTEGER NOT NULL,
                                habitType TEXT NOT NULL,
                                createdDate TEXT NOT NULL,
                                completionHistory TEXT NOT NULL,
                                repeatDays TEXT
                            )
                            """.trimIndent(),
                        )
                        db.execSQL(
                            """
                            INSERT INTO habits_new (id, title, description, icon, colorKey,
                                reminderDate, isCompleted, habitType, createdDate,
                                completionHistory, repeatDays)
                            SELECT id, title, description, icon, colorKey,
                                reminderDate, isCompleted,
                                CASE WHEN isInverted = 1 THEN 'QUIT' ELSE 'BUILD' END,
                                createdDate, completionHistory, repeatDays
                            FROM habits
                            """.trimIndent(),
                        )
                        // Quit habits cannot belong to chains. Read the post-migration habit
                        // type from `habits_new` (the source of truth at this point) rather
                        // than the about-to-be-dropped legacy `habits.isInverted` column —
                        // this stays correct regardless of statement ordering and documents
                        // the rule in domain terms.
                        db.execSQL(
                            """
                            DELETE FROM habit_chain_members
                            WHERE habitId IN (SELECT id FROM habits_new WHERE habitType = 'QUIT')
                            """.trimIndent(),
                        )
                        db.execSQL(
                            """
                            DELETE FROM habit_chains
                            WHERE id NOT IN (SELECT DISTINCT chainId FROM habit_chain_members)
                            """.trimIndent(),
                        )
                        db.execSQL("DROP TABLE habits")
                        db.execSQL("ALTER TABLE habits_new RENAME TO habits")
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                        db.execSQL("PRAGMA foreign_keys=ON")
                    }
                }
            }

        /**
         * Adds the `nextInstanceId` column to `tasks`. When a periodic task is completed,
         * the archived row's `nextInstanceId` is set to the spawned next-occurrence id so
         * the toggle use case can roll the spawn back if the user unchecks the archived task.
         */
        val MIGRATION_7_8 =
            object : Migration(7, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN nextInstanceId INTEGER DEFAULT NULL")
                }
            }

        /**
         * Adds indices on `tasks.categoryId` and `tasks.parentTaskId`. `TaskDao` filters on
         * both columns (subtask lookups, sort-order queries, category-scoped deletes), so the
         * indices replace full table scans. Index names match Room's generated names so the
         * migrated schema stays identity-equal to the exported `9.json`. Indices-only change:
         * no foreign keys are added (FK cascade is deferred to a separate considered change).
         */
        val MIGRATION_8_9 =
            object : Migration(8, 9) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_tasks_categoryId` ON `tasks` (`categoryId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_tasks_parentTaskId` ON `tasks` (`parentTaskId`)",
                    )
                }
            }

        val MIGRATIONS =
            arrayOf(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
            )

        suspend fun getDatabase(
            context: Context,
            passphraseProvider: DbPassphraseProvider,
            migrator: DatabaseEncryptionMigrator,
        ): TempoDatabase =
            INSTANCE ?: instanceMutex.withLock {
                INSTANCE ?: run {
                    val appContext = context.applicationContext
                    val passphrase = passphraseProvider.getOrCreatePassphrase()
                    // Guarantees that by the time Room opens this file below, it is already
                    // keyed at SqlCipherKdfIter.CURRENT — either because it didn't exist yet
                    // (fresh install, created below at CURRENT via the SupportOpenHelperFactory
                    // hook) or because it's been re-keyed from SqlCipherKdfIter.LEGACY. Room can
                    // therefore always open with CURRENT, unconditionally, below.
                    migrator.migrateIfNeeded(DATABASE_NAME, passphrase)
                    val instance =
                        Room
                            .databaseBuilder(
                                appContext,
                                TempoDatabase::class.java,
                                DATABASE_NAME,
                            ).openHelperFactory(
                                SupportOpenHelperFactory(
                                    passphrase,
                                    SqlCipherKdfIter.hookFor(SqlCipherKdfIter.CURRENT),
                                    false,
                                ),
                            ).addMigrations(*MIGRATIONS)
                            .addCallback(inboxCallback(appContext))
                            .fallbackToDestructiveMigration(false)
                            .build()
                    INSTANCE = instance
                    instance
                }
            }

        /**
         * Seeds the default Inbox category on fresh installs. After creation, the
         * category name is user-owned data and must not be rewritten on database open.
         *
         * `onCreate` only fires when Room itself creates the underlying file (i.e. a genuine
         * fresh install with no prior database) — the migrator's export/re-key paths always
         * hand Room an already-populated file, which Room treats as existing (`onOpen`, not
         * `onCreate`). So this is also the one place a freshly-created database's
         * [SqlCipherKdfIter] marker needs to be recorded: [DatabaseEncryptionMigrator] only ever
         * sees this file on its *next* launch, once it already exists.
         */
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal fun inboxCallback(context: Context) =
            object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    DbKdfIterMarker.write(context, SqlCipherKdfIter.CURRENT)
                    val localizedName = context.getString(R.string.category_inbox)
                    val values =
                        ContentValues().apply {
                            put("id", -1L)
                            put("name", localizedName)
                            put("icon", "inbox")
                            put("isDefault", 1)
                            put("sortOrder", -1)
                        }
                    db.insert("categories", android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE, values)
                }
            }
    }
}
