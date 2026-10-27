package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
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

        override fun saveLastRoute(routeName: String) {
            prefs.edit { putString(KEY_LAST_ROUTE, routeName) }
        }

        override fun getLastRoute(): String? = prefs.getString(KEY_LAST_ROUTE, null)

        companion object {
            private const val PREFS_NAME = "navigation_prefs"
            private const val KEY_LAST_ROUTE = "last_route"
        }
    }
