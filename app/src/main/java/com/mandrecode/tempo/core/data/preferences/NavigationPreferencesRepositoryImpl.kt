package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.domain.util.TabPreferencesPolicy
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

        private val enabledTabsFlow = MutableStateFlow(readEnabledTabs())
        private val defaultTabFlow = MutableStateFlow(readDefaultTab())

        override fun saveLastRoute(routeName: String) {
            prefs.edit { putString(KEY_LAST_ROUTE, routeName) }
        }

        override fun getLastRoute(): String? = prefs.getString(KEY_LAST_ROUTE, null)

        override fun enabledTabs(): Flow<Set<TempoTab>> = enabledTabsFlow.asStateFlow()

        override fun setTabEnabled(
            tab: TempoTab,
            enabled: Boolean,
        ) {
            writeEnabledTabs(TabPreferencesPolicy.withTabEnabled(enabledTabsFlow.value, tab, enabled))
        }

        override fun getDefaultTab(): Flow<TempoTab> = defaultTabFlow.asStateFlow()

        override fun setDefaultTab(tab: TempoTab) {
            prefs.edit { putString(KEY_DEFAULT_TAB, tab.preferenceValue) }
            defaultTabFlow.value = tab
        }

        override fun hasExplicitDefaultTab(): Boolean = prefs.contains(KEY_DEFAULT_TAB)

        private fun writeEnabledTabs(tabs: Set<TempoTab>) {
            val resolved = TabPreferencesPolicy.resolveEnabledTabs(tabs)
            prefs.edit {
                putStringSet(KEY_ENABLED_TABS, resolved.map { it.preferenceValue }.toSet())
            }
            enabledTabsFlow.value = resolved
        }

        /**
         * Reads the per-tab set, migrating from the legacy paired booleans on first read. The
         * legacy keys are deliberately left in place rather than removed: a downgrade to a build
         * that only understands two tabs then still finds the user's Routines/Tasks choices.
         */
        private fun readEnabledTabs(): Set<TempoTab> {
            val stored = prefs.getStringSet(KEY_ENABLED_TABS, null)
            if (stored != null) {
                return TabPreferencesPolicy.resolveEnabledTabs(
                    stored.mapNotNull { TempoTab.fromPreferenceValue(it) }.toSet(),
                )
            }

            val migrated =
                buildSet {
                    // Focus has no legacy key of its own, so it starts enabled for everyone.
                    add(TempoTab.FOCUS)
                    if (prefs.getBoolean(KEY_LEGACY_ROUTINES_TAB_ENABLED, true)) add(TempoTab.ROUTINES)
                    if (prefs.getBoolean(KEY_LEGACY_TASKS_TAB_ENABLED, true)) add(TempoTab.TASKS)
                }
            val resolved = TabPreferencesPolicy.resolveEnabledTabs(migrated)
            prefs.edit {
                putStringSet(KEY_ENABLED_TABS, resolved.map { it.preferenceValue }.toSet())
            }
            return resolved
        }

        private fun readDefaultTab(): TempoTab {
            val stored = prefs.getString(KEY_DEFAULT_TAB, null)
            return TempoTab.fromPreferenceValue(stored) ?: TempoTab.DEFAULT
        }

        companion object {
            private const val PREFS_NAME = "navigation_prefs"
            private const val KEY_LAST_ROUTE = "last_route"
            private const val KEY_ENABLED_TABS = "enabled_tabs"
            private const val KEY_DEFAULT_TAB = "default_tab"

            // Superseded by KEY_ENABLED_TABS; read once during migration, never written again.
            private const val KEY_LEGACY_ROUTINES_TAB_ENABLED = "routines_tab_enabled"
            private const val KEY_LEGACY_TASKS_TAB_ENABLED = "tasks_tab_enabled"
        }
    }
