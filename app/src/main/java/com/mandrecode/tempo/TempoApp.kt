package com.mandrecode.tempo

import android.app.Application
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
@OptIn(ExperimentalComposeUiApi::class)
class TempoApp :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        ComposeUiFlags.isMediaQueryIntegrationEnabled = true
        // SQLCipher's native core must be loaded explicitly before any database access —
        // unlike the deprecated android-database-sqlcipher library, this one does not do it
        // automatically as part of opening a database.
        System.loadLibrary("sqlcipher")
        super.onCreate()
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()
}
