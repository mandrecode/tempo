package com.mandrecode.tempo.core.data.local.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore

/** Keystore is unavailable off-device, so this provider is only covered here, not in JVM tests. */
@RunWith(AndroidJUnit4::class)
class KeystoreDbPassphraseProviderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        context
            .getSharedPreferences(SECURE_PREFS_FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    @Test
    fun getOrCreatePassphrase_returnsSameValueAcrossCalls() =
        runTest {
            val provider = KeystoreDbPassphraseProvider(context)

            val first = provider.getOrCreatePassphrase()
            val second = provider.getOrCreatePassphrase()

            assertThat(second).isEqualTo(first)
        }

    @Test
    fun getOrCreatePassphrase_persistsAcrossNewProviderInstances() =
        runTest {
            val passphrase = KeystoreDbPassphraseProvider(context).getOrCreatePassphrase()

            val reloaded = KeystoreDbPassphraseProvider(context).getOrCreatePassphrase()

            assertThat(reloaded).isEqualTo(passphrase)
        }

    @Test
    fun getOrCreatePassphrase_generatesA32ByteKey() =
        runTest {
            val passphrase = KeystoreDbPassphraseProvider(context).getOrCreatePassphrase()

            assertThat(passphrase).hasLength(32)
        }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "tempo_db_passphrase_key"
        const val SECURE_PREFS_FILE_NAME = "tempo_secure_prefs"
    }
}
