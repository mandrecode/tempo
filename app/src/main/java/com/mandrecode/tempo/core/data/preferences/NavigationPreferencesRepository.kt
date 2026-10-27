package com.mandrecode.tempo.core.data.preferences

interface NavigationPreferencesRepository {
    fun saveLastRoute(routeName: String)

    fun getLastRoute(): String?
}
