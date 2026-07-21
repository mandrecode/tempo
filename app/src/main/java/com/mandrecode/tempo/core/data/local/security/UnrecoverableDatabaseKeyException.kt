package com.mandrecode.tempo.core.data.local.security

/**
 * Thrown when an encrypted database passphrase blob already exists on disk but the Android
 * Keystore key needed to decrypt it is gone or invalidated. A new passphrase must never be
 * generated in this state — that would permanently orphan the existing encrypted database.
 */
class UnrecoverableDatabaseKeyException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
