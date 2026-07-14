package com.mandrecode.tempo.core.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

internal fun NavHostController.topLevelPopUpToId(): Int {
    val startDestinationId = graph.findStartDestination().id
    return if (runCatching { getBackStackEntry(startDestinationId) }.isSuccess) {
        startDestinationId
    } else {
        graph.id
    }
}
