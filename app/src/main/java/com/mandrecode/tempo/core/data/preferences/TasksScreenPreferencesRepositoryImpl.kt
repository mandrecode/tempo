package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksScreenPreferencesRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : TasksScreenPreferencesRepository {
        private val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE,
            )

        override fun getSortOption(categoryId: Long): SortOption {
            val value = prefs.getString(sortOptionKey(categoryId), SortOption.MANUAL.name)
            return try {
                SortOption.valueOf(value ?: SortOption.MANUAL.name)
            } catch (_: IllegalArgumentException) {
                SortOption.MANUAL
            }
        }

        override fun setSortOption(
            categoryId: Long,
            sortOption: SortOption,
        ) {
            prefs.edit { putString(sortOptionKey(categoryId), sortOption.name) }
        }

        override fun getShowCompletedTasks(): Boolean = prefs.getBoolean(KEY_SHOW_COMPLETED_TASKS, true)

        override fun setShowCompletedTasks(show: Boolean) {
            prefs.edit { putBoolean(KEY_SHOW_COMPLETED_TASKS, show) }
        }

        override fun getSelectedCategoryId(): Long = prefs.getLong(KEY_SELECTED_CATEGORY_ID, 0L)

        override fun setSelectedCategoryId(categoryId: Long) {
            prefs.edit { putLong(KEY_SELECTED_CATEGORY_ID, categoryId) }
        }

        companion object {
            private const val PREFS_NAME = "tasks_screen_prefs"
            private const val KEY_SHOW_COMPLETED_TASKS = "show_completed_tasks"
            private const val KEY_SORT_OPTION_PREFIX = "sort_option_"
            private const val KEY_SELECTED_CATEGORY_ID = "selected_category_id"

            private fun sortOptionKey(categoryId: Long): String = "$KEY_SORT_OPTION_PREFIX$categoryId"
        }
    }
