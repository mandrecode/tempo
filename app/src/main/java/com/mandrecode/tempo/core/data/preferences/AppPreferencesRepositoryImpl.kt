package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.mandrecode.tempo.core.domain.model.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : AppPreferencesRepository {
        private val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE,
            )

        private val _appLanguage = MutableStateFlow(getAppLanguage())
        override val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

        override fun saveAppLanguage(language: AppLanguage) {
            prefs.edit { putString(KEY_APP_LANGUAGE, language.name) }
            _appLanguage.value = language
        }

        override fun getAppLanguage(): AppLanguage {
            val languageString =
                prefs.getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.name)
                    ?: AppLanguage.SYSTEM.name
            return AppLanguage.fromString(languageString)
        }

        companion object {
            private const val PREFS_NAME = "app_prefs"
            private const val KEY_APP_LANGUAGE = "app_language"
        }
    }
