package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.LocalDate
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

        override fun getActiveChains(): Map<Long, LocalDate> =
            prefs
                .getStringSet(KEY_ACTIVE_CHAIN_IDS, emptySet())
                .orEmpty()
                .mapNotNull { it.toRecord() }
                .toMap()

        override fun getActiveChainIds(): Set<Long> = getActiveChains().keys

        override fun setActiveChain(
            chainId: Long,
            date: LocalDate,
        ) {
            persist(getActiveChains() + (chainId to date))
        }

        override fun removeActiveChainId(chainId: Long) {
            persist(getActiveChains() - chainId)
        }

        private fun persist(records: Map<Long, LocalDate>) {
            prefs.edit {
                putStringSet(
                    KEY_ACTIVE_CHAIN_IDS,
                    records.map { (chainId, date) -> "$chainId$SEPARATOR$date" }.toSet(),
                )
            }
        }

        /**
         * Parses a `<chainId>|<iso-date>` entry. Entries written before date-scoping have no
         * separator and no date, so they parse to null and are discarded — which also clears
         * records left stuck by the pre-fix resync loop.
         */
        private fun String.toRecord(): Pair<Long, LocalDate>? {
            val chainId = substringBefore(SEPARATOR, missingDelimiterValue = "").toLongOrNull()
            val date = runCatching { LocalDate.parse(substringAfter(SEPARATOR)) }.getOrNull()
            return if (chainId != null && date != null) chainId to date else null
        }

        companion object {
            private const val PREFS_NAME = "active_live_activity_prefs"
            private const val KEY_ACTIVE_CHAIN_IDS = "active_chain_ids"
            private const val SEPARATOR = "|"
        }
    }
