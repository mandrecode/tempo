package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveLiveActivityPreferencesImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : ActiveLiveActivityPreferences {
        private val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE,
            )

        override fun getActiveChainIds(): Set<Long> =
            prefs
                .getStringSet(KEY_ACTIVE_CHAIN_IDS, emptySet())
                .orEmpty()
                .mapNotNull { it.toLongOrNull() }
                .toSet()

        @Synchronized
        override fun addActiveChainId(chainId: Long) {
            val updated = getActiveChainIds() + chainId
            prefs.edit { putStringSet(KEY_ACTIVE_CHAIN_IDS, updated.map { it.toString() }.toSet()) }
        }

        @Synchronized
        override fun removeActiveChainId(chainId: Long) {
            val updated = getActiveChainIds() - chainId
            prefs.edit { putStringSet(KEY_ACTIVE_CHAIN_IDS, updated.map { it.toString() }.toSet()) }
        }

        companion object {
            private const val PREFS_NAME = "active_live_activity_prefs"
            private const val KEY_ACTIVE_CHAIN_IDS = "active_chain_ids"
        }
    }
