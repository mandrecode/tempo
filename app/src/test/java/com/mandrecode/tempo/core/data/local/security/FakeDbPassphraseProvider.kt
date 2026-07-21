package com.mandrecode.tempo.core.data.local.security

/** Fixed passphrase for JVM tests, since the real Keystore-backed provider needs a device. */
class FakeDbPassphraseProvider(
    private val passphrase: ByteArray = ByteArray(32) { it.toByte() },
) : DbPassphraseProvider {
    override suspend fun getOrCreatePassphrase(): ByteArray = passphrase
}
