package com.mandrecode.tempo.core.data.preferences

import kotlinx.coroutines.flow.StateFlow

interface OnboardingPreferencesRepository {
    val isCompleted: StateFlow<Boolean>

    fun setCompleted()
}
