package com.mandrecode.tempo

import android.app.Application
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mandrecode.tempo.core.data.local.TempoDatabase
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
@OptIn(ExperimentalComposeUiApi::class)
class TempoApp :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var tempoDatabaseLazy: Lazy<TempoDatabase>

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        ComposeUiFlags.isMediaQueryIntegrationEnabled = true
        // SQLCipher's native core must be loaded explicitly before any database access —
        // unlike the deprecated android-database-sqlcipher library, this one does not do it
        // automatically as part of opening a database.
        System.loadLibrary("sqlcipher")
        super.onCreate()
        // Warm up the encrypted database off the main thread as early as possible.
        // DatabaseModule.provideTempoDatabase() blocks its caller on Keystore I/O and a
        // potential one-time migration; Hilt's Lazy/Provider memoize on first resolution
        // (DoubleCheck), so triggering that resolution here means the first ViewModel that
        // needs the database moments later — on the main thread — finds it already built (or
        // waits only for whatever's left) instead of paying the full cost itself.
        applicationScope.launch { tempoDatabaseLazy.get() }
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()
}
