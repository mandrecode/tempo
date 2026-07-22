package com.mandrecode.tempo.core.data.local.security

/**
 * Supplies the raw key material used to open the encrypted local database.
 *
 * Implementations must return the same passphrase across calls for a given install so the
 * database can always be reopened; see [KeystoreDbPassphraseProvider] for the production
 * implementation and [UnrecoverableDatabaseKeyException] for the one failure mode that must
 * never be silently papered over.
 */
interface DbPassphraseProvider {
    suspend fun getOrCreatePassphrase(): ByteArray
}
