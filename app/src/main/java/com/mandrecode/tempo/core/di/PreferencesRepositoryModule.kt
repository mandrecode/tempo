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
import com.mandrecode.tempo.core.data.preferences.WhatsNewPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.WhatsNewPreferencesRepositoryImpl
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesRepositoryModule {
    @Binds
    abstract fun bindThemePreferencesRepository(impl: ThemePreferencesRepositoryImpl): ThemePreferencesRepository

    @Binds
    abstract fun bindNavigationPreferencesRepository(impl: NavigationPreferencesRepositoryImpl): NavigationPreferencesRepository

    @Binds
    abstract fun bindOnboardingPreferencesRepository(impl: OnboardingPreferencesRepositoryImpl): OnboardingPreferencesRepository

    @Binds
    abstract fun bindWhatsNewPreferencesRepository(impl: WhatsNewPreferencesRepositoryImpl): WhatsNewPreferencesRepository

    @Binds
    abstract fun bindAppPreferencesRepository(impl: AppPreferencesRepositoryImpl): AppPreferencesRepository

    @Binds
    abstract fun bindTasksScreenPreferencesRepository(impl: TasksScreenPreferencesRepositoryImpl): TasksScreenPreferencesRepository

    @Binds
    abstract fun bindCompletedTaskRetentionPreferences(impl: CompletedTaskRetentionPreferencesImpl): CompletedTaskRetentionPreferences
}
