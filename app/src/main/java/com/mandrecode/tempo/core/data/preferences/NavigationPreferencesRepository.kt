package com.mandrecode.tempo.core.data.preferences

import kotlinx.coroutines.flow.Flow

interface NavigationPreferencesRepository {
    fun saveLastRoute(routeName: String)

    fun getLastRoute(): String?

    fun isRoutinesTabEnabled(): Flow<Boolean>

    fun isTasksTabEnabled(): Flow<Boolean>

    fun getDefaultTab(): Flow<String>

    fun setRoutinesTabEnabled(enabled: Boolean)

    fun setTasksTabEnabled(enabled: Boolean)

    fun setDefaultTab(tabName: String)

    companion object {
        const val DEFAULT_TAB_ROUTINES = "routines"
        const val DEFAULT_TAB_TASKS = "tasks"
    }
}
