package com.mandrecode.tempo.infrastructure.security

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackupEncryptionServiceTest {
    private val service = BackupEncryptionService()

    @Test
    fun `round-trips plaintext through encrypt then decrypt`() {
        val envelope = service.encrypt("hello world", "correct horse".toCharArray())

        val result = service.decrypt(envelope, "correct horse".toCharArray())

        assertThat(result).isEqualTo(DecryptResult.Success("hello world"))
    }

    @Test
    fun `each encryption uses a fresh random salt and iv`() {
        val first = service.encrypt("same plaintext", "pw".toCharArray())
        val second = service.encrypt("same plaintext", "pw".toCharArray())

        assertThat(first.salt).isNotEqualTo(second.salt)
        assertThat(first.iv).isNotEqualTo(second.iv)
        assertThat(first.ciphertext).isNotEqualTo(second.ciphertext)
    }

    @Test
    fun `wrong passphrase is reported distinctly, not as garbled output`() {
        val envelope = service.encrypt("secret data", "right passphrase".toCharArray())

        val result = service.decrypt(envelope, "wrong passphrase".toCharArray())

        assertThat(result).isEqualTo(DecryptResult.WrongPassphrase)
    }

    @Test
    fun `tampered ciphertext fails the same auth-tag check as a wrong passphrase`() {
        // AES-GCM can't distinguish "wrong key" from "tampered ciphertext" — both fail
        // the same authentication check, so both surface as WrongPassphrase.
        val envelope = service.encrypt("secret data", "pw".toCharArray())
        val tampered = envelope.copy(ciphertext = ByteArray(envelope.ciphertext.size) { it.toByte() })

        val result = service.decrypt(tampered, "pw".toCharArray())

        assertThat(result).isEqualTo(DecryptResult.WrongPassphrase)
    }

    @Test
    fun `malformed iv is reported as corrupt rather than throwing`() {
        val envelope = service.encrypt("secret data", "pw".toCharArray())
        val malformed = envelope.copy(iv = ByteArray(0))

        val result = service.decrypt(malformed, "pw".toCharArray())

        assertThat(result).isEqualTo(DecryptResult.Corrupt)
    }

    @Test
    fun `encrypted envelope records the kdf and iteration count`() {
        val envelope = service.encrypt("data", "pw".toCharArray())

        assertThat(envelope.kdf).isEqualTo("PBKDF2WithHmacSHA256")
        assertThat(envelope.iterations).isEqualTo(200_000)
    }

    @Test
    fun `unsupported kdf name is reported as corrupt without attempting derivation`() {
        val envelope = service.encrypt("secret data", "pw".toCharArray())
        val malformed = envelope.copy(kdf = "PBKDF2WithHmacSHA1")

        val result = service.decrypt(malformed, "pw".toCharArray())

        assertThat(result).isEqualTo(DecryptResult.Corrupt)
    }

    @Test
    fun `zero iteration count is reported as corrupt rather than throwing`() {
        val envelope = service.encrypt("secret data", "pw".toCharArray())
        val malformed = envelope.copy(iterations = 0)

        val result = service.decrypt(malformed, "pw".toCharArray())

        assertThat(result).isEqualTo(DecryptResult.Corrupt)
    }

    @Test
    fun `excessive iteration count is reported as corrupt rather than hanging`() {
        val envelope = service.encrypt("secret data", "pw".toCharArray())
        val malformed = envelope.copy(iterations = Int.MAX_VALUE)

        val result = service.decrypt(malformed, "pw".toCharArray())

        assertThat(result).isEqualTo(DecryptResult.Corrupt)
    }
}
