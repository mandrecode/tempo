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

        // Read once, together, so the migration below cannot depend on which flow happens to be
        // declared first — it writes both keys, and both flows must see the post-migration values.
        private val initialState = migrateIfNeeded()
        private val enabledTabsFlow = MutableStateFlow(initialState.enabledTabs)
        private val defaultTabFlow = MutableStateFlow(initialState.defaultTab)

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

        private fun writeEnabledTabs(tabs: Set<TempoTab>) {
            val resolved = TabPreferencesPolicy.resolveEnabledTabs(tabs)
            prefs.edit {
                putStringSet(KEY_ENABLED_TABS, resolved.map { it.preferenceValue }.toSet())
            }
            enabledTabsFlow.value = resolved
        }

        private data class StoredState(
            val enabledTabs: Set<TempoTab>,
            val defaultTab: TempoTab,
        )

        /**
         * Reads the stored navigation preferences, migrating from the legacy paired booleans the
         * first time this build runs.
         *
         * The same one-time pass moves the default tab to Focus, for existing installations as
         * well as new ones. That deliberately overwrites a tab some users chose on purpose, which
         * is why the what's-new sheet announces the change and points at the Settings entry that
         * reverses it. Keyed on [KEY_ENABLED_TABS] being absent, so it runs exactly once and never
         * re-applies after the user picks a different tab.
         *
         * The legacy keys are deliberately left in place rather than removed: a downgrade to a
         * build that only understands two tabs then still finds the user's Routines/Tasks choices.
         */
        private fun migrateIfNeeded(): StoredState {
            val stored = prefs.getStringSet(KEY_ENABLED_TABS, null)
            if (stored != null) {
                return StoredState(
                    enabledTabs =
                        TabPreferencesPolicy.resolveEnabledTabs(
                            stored.mapNotNull { TempoTab.fromPreferenceValue(it) }.toSet(),
                        ),
                    defaultTab = readStoredDefaultTab(),
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
                putString(KEY_DEFAULT_TAB, TempoTab.DEFAULT.preferenceValue)
            }
            return StoredState(
                enabledTabs = resolved,
                defaultTab = TabPreferencesPolicy.resolveDefaultTab(TempoTab.DEFAULT, resolved),
            )
        }

        private fun readStoredDefaultTab(): TempoTab {
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
