package com.mandrecode.tempo.core.di

import android.content.Context
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.CategoryDao
import com.mandrecode.tempo.core.data.local.dao.HabitChainDao
import com.mandrecode.tempo.core.data.local.dao.HabitChainMemberDao
import com.mandrecode.tempo.core.data.local.dao.HabitDao
import com.mandrecode.tempo.core.data.local.dao.TaskDao
import com.mandrecode.tempo.core.data.local.security.DatabaseEncryptionMigrator
import com.mandrecode.tempo.core.data.local.security.DbPassphraseProvider
import com.mandrecode.tempo.core.data.local.security.KeystoreDbPassphraseProvider
import com.mandrecode.tempo.util.DataMode
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDataMode(): DataMode = DataMode.REAL

    @Provides
    @Singleton
    fun provideDbPassphraseProvider(impl: KeystoreDbPassphraseProvider): DbPassphraseProvider = impl

    /**
     * Hilt's `@Provides` can't be `suspend`, so resolving this singleton blocks its calling
     * thread on Keystore I/O and a potential one-time migration. [com.mandrecode.tempo.TempoApp]
     * mitigates this by resolving the same Hilt-memoized singleton on a background coroutine
     * as early as app startup, so this provider is very likely already resolved (or close to
     * it) by the time anything on the main thread — e.g. the first ViewModel — needs it.
     */
    @Provides
    @Singleton
    fun provideTempoDatabase(
        @ApplicationContext context: Context,
        passphraseProvider: DbPassphraseProvider,
        migrator: DatabaseEncryptionMigrator,
    ): TempoDatabase =
        runBlocking(Dispatchers.IO) {
            TempoDatabase.getDatabase(context, passphraseProvider, migrator)
        }

    @Provides
    @Singleton
    fun provideTaskDao(database: TempoDatabase): TaskDao = database.taskDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: TempoDatabase): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideHabitDao(database: TempoDatabase): HabitDao = database.habitDao()

    @Provides
    @Singleton
    fun provideHabitChainDao(database: TempoDatabase): HabitChainDao = database.habitChainDao()

    @Provides
    @Singleton
    fun provideHabitChainMemberDao(database: TempoDatabase): HabitChainMemberDao = database.habitChainMemberDao()
}
