package com.mandrecode.tempo.core.di

import android.content.Context
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.CategoryDao
import com.mandrecode.tempo.core.data.local.dao.HabitChainDao
import com.mandrecode.tempo.core.data.local.dao.HabitChainMemberDao
import com.mandrecode.tempo.core.data.local.dao.HabitDao
import com.mandrecode.tempo.core.data.local.dao.TaskDao
import com.mandrecode.tempo.util.DataMode
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDataMode(): DataMode = DataMode.REAL

    @Provides
    @Singleton
    fun provideTempoDatabase(
        @ApplicationContext context: Context,
    ): TempoDatabase = TempoDatabase.getDatabase(context)

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
