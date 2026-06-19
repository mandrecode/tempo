package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationPreferencesRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : NavigationPreferencesRepository {
        private val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE,
            )

        private val routinesTabEnabledFlow = MutableStateFlow(getRoutinesTabEnabled())
        private val tasksTabEnabledFlow = MutableStateFlow(getTasksTabEnabled())
        private val _defaultTab = MutableStateFlow(getCurrentDefaultTab())

        override fun saveLastRoute(routeName: String) {
            prefs.edit { putString(KEY_LAST_ROUTE, routeName) }
        }

        override fun getLastRoute(): String? = prefs.getString(KEY_LAST_ROUTE, null)

        override fun isRoutinesTabEnabled(): Flow<Boolean> = routinesTabEnabledFlow.asStateFlow()

        override fun isTasksTabEnabled(): Flow<Boolean> = tasksTabEnabledFlow.asStateFlow()

        override fun getDefaultTab(): Flow<String> = _defaultTab.asStateFlow()

        override fun setRoutinesTabEnabled(enabled: Boolean) {
            prefs.edit { putBoolean(KEY_ROUTINES_TAB_ENABLED, enabled) }
            routinesTabEnabledFlow.value = enabled
        }

        override fun setTasksTabEnabled(enabled: Boolean) {
            prefs.edit { putBoolean(KEY_TASKS_TAB_ENABLED, enabled) }
            tasksTabEnabledFlow.value = enabled
        }

        override fun setDefaultTab(tabName: String) {
            prefs.edit { putString(KEY_DEFAULT_TAB, tabName) }
            _defaultTab.value = tabName
        }

        private fun getRoutinesTabEnabled(): Boolean = prefs.getBoolean(KEY_ROUTINES_TAB_ENABLED, true)

        private fun getTasksTabEnabled(): Boolean = prefs.getBoolean(KEY_TASKS_TAB_ENABLED, true)

        private fun getCurrentDefaultTab(): String =
            prefs.getString(KEY_DEFAULT_TAB, NavigationPreferencesRepository.DEFAULT_TAB_ROUTINES)
                ?: NavigationPreferencesRepository.DEFAULT_TAB_ROUTINES

        companion object {
            private const val PREFS_NAME = "navigation_prefs"
            private const val KEY_LAST_ROUTE = "last_route"
            private const val KEY_ROUTINES_TAB_ENABLED = "routines_tab_enabled"
            private const val KEY_TASKS_TAB_ENABLED = "tasks_tab_enabled"
            private const val KEY_DEFAULT_TAB = "default_tab"
        }
    }
