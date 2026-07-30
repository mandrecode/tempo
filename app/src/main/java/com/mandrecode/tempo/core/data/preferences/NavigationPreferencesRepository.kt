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
}
