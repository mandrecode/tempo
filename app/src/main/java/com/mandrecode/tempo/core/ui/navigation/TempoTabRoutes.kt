package com.mandrecode.tempo.core.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.mandrecode.tempo.core.domain.model.TempoTab

/**
 * The single mapping between the persisted [TempoTab] identity and its navigation route. Every
 * other navigation site resolves through here, so a new tab needs one entry in [TempoTab] and one
 * branch in each of these two functions rather than edits spread across the navigation package.
 */
internal val TempoTab.route: NavKey
    get() =
        when (this) {
            TempoTab.FOCUS -> FocusRoute
            TempoTab.ROUTINES -> RoutinesRoute
            TempoTab.TASKS -> TasksRoute
        }

internal fun NavKey.toTempoTabOrNull(): TempoTab? =
    when (this) {
        FocusRoute -> TempoTab.FOCUS
        RoutinesRoute -> TempoTab.ROUTINES
        TasksRoute -> TempoTab.TASKS
        else -> null
    }
