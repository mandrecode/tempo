package com.mandrecode.tempo.core.data.preferences

import com.mandrecode.tempo.core.domain.model.AppLanguage
import kotlinx.coroutines.flow.StateFlow

interface AppPreferencesRepository {
    val appLanguage: StateFlow<AppLanguage>

    fun saveAppLanguage(language: AppLanguage)

    fun getAppLanguage(): AppLanguage
}
