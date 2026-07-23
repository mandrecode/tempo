package com.mandrecode.tempo.core.data.local.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Signals when the app-startup encrypted-database warm-up (`TempoApp.onCreate`'s background
 * resolution of `TempoDatabase`) has concluded, whether it succeeded or failed. `MainActivity`
 * uses this to keep the system splash screen on-screen for the (bounded) duration of that
 * warm-up, so the SQLCipher key-derivation cost is hidden behind a screen users already expect
 * to sit on briefly, instead of dismissing immediately and then surfacing the in-app loading
 * indicator once a screen's data hasn't arrived yet.
 */
@Singleton
class DatabaseWarmupSignal
    @Inject
    constructor() {
        private val _isReady = MutableStateFlow(false)
        val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

        fun markReady() {
            _isReady.value = true
        }
    }
