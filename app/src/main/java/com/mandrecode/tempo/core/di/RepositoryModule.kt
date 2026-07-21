package com.mandrecode.tempo.core.di

import com.mandrecode.tempo.core.data.preferences.AppPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.AppPreferencesRepositoryImpl
import com.mandrecode.tempo.core.data.preferences.CompletedTaskRetentionPreferencesImpl
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepositoryImpl
import com.mandrecode.tempo.core.data.preferences.OnboardingPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.OnboardingPreferencesRepositoryImpl
import com.mandrecode.tempo.core.data.preferences.TasksScreenPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.TasksScreenPreferencesRepositoryImpl
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepositoryImpl
import com.mandrecode.tempo.features.backup.data.repository.BackupRepositoryImpl
import com.mandrecode.tempo.features.backup.domain.repository.BackupRepository
import com.mandrecode.tempo.features.routines.data.repository.HabitChainRepositoryImpl
import com.mandrecode.tempo.features.routines.data.repository.HabitRepositoryImpl
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.tasks.data.repository.CategoryRepositoryImpl
import com.mandrecode.tempo.features.tasks.data.repository.TaskRepositoryImpl
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    abstract fun bindHabitChainRepository(impl: HabitChainRepositoryImpl): HabitChainRepository

    @Binds
    abstract fun bindThemePreferencesRepository(impl: ThemePreferencesRepositoryImpl): ThemePreferencesRepository

    @Binds
    abstract fun bindNavigationPreferencesRepository(impl: NavigationPreferencesRepositoryImpl): NavigationPreferencesRepository

    @Binds
    abstract fun bindOnboardingPreferencesRepository(impl: OnboardingPreferencesRepositoryImpl): OnboardingPreferencesRepository

    @Binds
    abstract fun bindAppPreferencesRepository(impl: AppPreferencesRepositoryImpl): AppPreferencesRepository

    @Binds
    abstract fun bindTasksScreenPreferencesRepository(impl: TasksScreenPreferencesRepositoryImpl): TasksScreenPreferencesRepository

    @Binds
    abstract fun bindCompletedTaskRetentionPreferences(impl: CompletedTaskRetentionPreferencesImpl): CompletedTaskRetentionPreferences

    @Binds
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
