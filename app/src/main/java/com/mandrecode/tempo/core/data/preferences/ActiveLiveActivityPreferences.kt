package com.mandrecode.tempo.core.data.preferences

import kotlinx.datetime.LocalDate

interface ActiveLiveActivityPreferences {
    /**
     * Chains with an active live activity, mapped to the scheduled date their progress belongs to.
     * Records that predate date-scoping carry no date and are dropped as stale.
     */
    fun getActiveChains(): Map<Long, LocalDate>

    fun getActiveChainIds(): Set<Long>

    fun setActiveChain(
        chainId: Long,
        date: LocalDate,
    )

    fun removeActiveChainId(chainId: Long)
}
