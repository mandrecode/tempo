package com.mandrecode.tempo.core.data.local.security

import android.content.Context
import androidx.annotation.VisibleForTesting

/**
 * Tracks which [SqlCipherKdfIter] value the *on-disk* database is currently keyed with, so
 * [DatabaseEncryptionMigrator] doesn't have to re-verify that on every single launch. This is a
 * performance hint, not a source of truth: an absent or stale marker never causes data loss or a
 * wrong key to be used — [DatabaseEncryptionMigrator] always falls back to actually opening the
 * file to confirm before trusting or updating it. Not secret, so a plain (unencrypted)
 * `SharedPreferences` file is fine — unlike [KeystoreDbPassphraseProvider]'s blob, kdf_iter isn't
 * sensitive on its own.
 *
 * Not scoped per database name/path — fine in production, where Tempo only ever has the one
 * `tempo_database` file, but instrumented tests exercising multiple differently-named database
 * files in the same process must call [clear] between cases to avoid one test's marker leaking
 * into another's.
 */
internal object DbKdfIterMarker {
    fun readOrNull(context: Context): Int? {
        val stored = prefs(context).getInt(KEY_KDF_ITER, NOT_SET)
        return stored.takeIf { it != NOT_SET }
    }

    fun write(
        context: Context,
        kdfIter: Int,
    ) {
        check(prefs(context).edit().putInt(KEY_KDF_ITER, kdfIter).commit()) {
            "Failed to persist the database kdf_iter marker"
        }
    }

    @VisibleForTesting
    fun clear(context: Context) {
        check(prefs(context).edit().clear().commit()) {
            "Failed to clear the database kdf_iter marker"
        }
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    private const val PREFS_FILE_NAME = "tempo_db_kdf_prefs"
    private const val KEY_KDF_ITER = "db_kdf_iter"
    private const val NOT_SET = -1
}
