package com.mandrecode.tempo.core.data.preferences

interface ActiveLiveActivityPreferences {
    fun getActiveChainIds(): Set<Long>

    fun addActiveChainId(chainId: Long)

    fun removeActiveChainId(chainId: Long)
}
