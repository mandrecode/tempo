package com.mandrecode.tempo.infrastructure.security

import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

/** Raw encrypted form of a backup payload; feature layers map this to their own on-disk DTO. */
data class EncryptedEnvelope(
    val kdf: String,
    val iterations: Int,
    val salt: ByteArray,
    val iv: ByteArray,
    val ciphertext: ByteArray,
)

sealed interface DecryptResult {
    data class Success(
        val plaintext: String,
    ) : DecryptResult

    /** Decryption failed authentication — almost always a wrong passphrase. */
    data object WrongPassphrase : DecryptResult

    /** The envelope itself is malformed (bad lengths, unsupported KDF, etc). */
    data object Corrupt : DecryptResult
}

/**
 * Encrypts/decrypts backup payloads with a passphrase-derived key: PBKDF2WithHmacSHA256 to
 * stretch the user-supplied passphrase into an AES-256 key, then AES-256-GCM (authenticated,
 * so a wrong passphrase or corrupted payload is detected via auth-tag failure rather than
 * producing silently garbled output).
 */
class BackupEncryptionService
    @Inject
    constructor() {
        fun encrypt(
            plaintext: String,
            passphrase: CharArray,
        ): EncryptedEnvelope {
            val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
            val key = deriveKey(passphrase, salt, PBKDF2_ITERATIONS)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            return EncryptedEnvelope(
                kdf = KDF_NAME,
                iterations = PBKDF2_ITERATIONS,
                salt = salt,
                iv = cipher.iv,
                ciphertext = ciphertext,
            )
        }

        fun decrypt(
            envelope: EncryptedEnvelope,
            passphrase: CharArray,
        ): DecryptResult =
            try {
                val key = deriveKey(passphrase, envelope.salt, envelope.iterations)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, envelope.iv))
                val plaintext = cipher.doFinal(envelope.ciphertext)
                DecryptResult.Success(String(plaintext, Charsets.UTF_8))
            } catch (_: AEADBadTagException) {
                DecryptResult.WrongPassphrase
            } catch (_: GeneralSecurityException) {
                DecryptResult.Corrupt
            }

        private fun deriveKey(
            passphrase: CharArray,
            salt: ByteArray,
            iterations: Int,
        ): SecretKeySpec {
            val factory = SecretKeyFactory.getInstance(KDF_NAME)
            val spec = PBEKeySpec(passphrase, salt, iterations, KEY_LENGTH_BITS)
            val keyBytes = factory.generateSecret(spec).encoded
            return SecretKeySpec(keyBytes, "AES")
        }

        private companion object {
            const val KDF_NAME = "PBKDF2WithHmacSHA256"
            const val PBKDF2_ITERATIONS = 200_000
            const val KEY_LENGTH_BITS = 256
            const val SALT_LENGTH_BYTES = 16
            const val TRANSFORMATION = "AES/GCM/NoPadding"
            const val GCM_TAG_LENGTH_BITS = 128
        }
    }
