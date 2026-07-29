package com.mandrecode.tempo.core.data.preferences

import com.mandrecode.tempo.core.domain.model.TempoTab
import kotlinx.coroutines.flow.Flow

interface NavigationPreferencesRepository {
    fun saveLastRoute(routeName: String)

    fun getLastRoute(): String?

    /** The tabs currently shown in the navigation bar and rail; never empty. */
    fun enabledTabs(): Flow<Set<TempoTab>>

    fun setTabEnabled(
        tab: TempoTab,
        enabled: Boolean,
    )

    fun getDefaultTab(): Flow<TempoTab>

    fun setDefaultTab(tab: TempoTab)

    /**
     * Whether the user has ever chosen a default tab, as opposed to falling through to
     * [TempoTab.DEFAULT]. Onboarding uses this to seed a new installation's start tab without
     * overwriting a choice an existing installation already made.
     */
    fun hasExplicitDefaultTab(): Boolean
}
