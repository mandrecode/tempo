package com.mandrecode.tempo.core.data.local.security

import net.zetetic.database.sqlcipher.SQLiteConnection
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook

/**
 * The PBKDF2 iteration count ("kdf_iter") SQLCipher uses to stretch the database passphrase into
 * an encryption key. SQLCipher does not self-describe this value inside the file — whatever
 * count was used to key a database must be supplied identically on every later open, or the
 * derived key (and every read/write against it) is silently wrong. See
 * [DatabaseEncryptionMigrator] for how the on-disk database is brought to and kept at [CURRENT].
 *
 * The database passphrase (see [KeystoreDbPassphraseProvider]) is not a human password — it's 32
 * random bytes from a CSPRNG, already wrapped by a hardware-backed Android Keystore key. PBKDF2
 * stretching exists to slow down brute-forcing a *guessable* passphrase; it adds no meaningful
 * security margin against an already-uniform 256-bit random key, so paying SQLCipher 4's default
 * of 256,000 iterations (tuned for human passwords — see the "kdf_iter" pragma docs at
 * https://www.zetetic.net/sqlcipher/sqlcipher-api/#kdf_iter, which explicitly caveats that
 * guidance as being "if a passphrase is in use") on *every single cold start* is latency with no
 * corresponding benefit for this threat model. Measured directly on this repo's dev emulator
 * (net.zetetic:sqlcipher-android 4.17.0, x86_64), open time scales almost perfectly linearly with
 * kdf_iter: 256,000 ≈ 406ms, 64,000 ≈ 103ms, 16,000 ≈ 26ms, 4,000 ≈ 7.5ms. [CURRENT] keeps a
 * substantial, non-trivial PBKDF2 pass (16,000 rounds, not zero) as defense-in-depth while
 * cutting the per-launch cost by 16x.
 */
internal object SqlCipherKdfIter {
    /** SQLCipher 4's own default. Used, un-overridden, by every Tempo install before this change. */
    const val LEGACY = 256_000

    /** Value all new and re-keyed databases use going forward. */
    const val CURRENT = 16_000

    fun hookFor(kdfIter: Int): SQLiteDatabaseHook =
        object : SQLiteDatabaseHook {
            override fun preKey(connection: SQLiteConnection) = Unit

            override fun postKey(connection: SQLiteConnection) {
                // Must run in postKey, not preKey: per SQLCipher's own docs, "PRAGMA kdf_iter
                // must be called after PRAGMA key and before the first actual database
                // operation." PBKDF2 derivation is deferred until the first real page access
                // (e.g. the verification query SQLiteConnection.open() runs right after this
                // hook), not performed eagerly by the key pragma itself, so this ordering works.
                connection.execute("PRAGMA kdf_iter = $kdfIter;", null, null)
            }
        }
}
