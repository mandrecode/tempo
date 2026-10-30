package com.mandrecode.tempo.core.ui.navigation

/**
 * Marks the entries of the three top-level tabs (Focus, Routines, Tasks) with their route, so
 * transitions can tell a tab switch from other navigation.
 */
internal const val TOP_LEVEL_ROUTE_METADATA = "tempo.top.level.route"

/**
 * Marks a top-level route's [androidx.navigation3.runtime.NavEntry] for identity checks that can't
 * rely on its `contentKey` (it defaults to `key.toString()`, and the raw
 * [androidx.navigation3.runtime.NavKey] instance isn't otherwise exposed).
 */
internal const val ONBOARDING_ROUTE_METADATA = "tempo.onboarding.route"
internal const val SETTINGS_ROUTE_METADATA = "tempo.settings.route"
