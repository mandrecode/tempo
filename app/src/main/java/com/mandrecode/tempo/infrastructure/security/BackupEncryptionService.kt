package com.mandrecode.tempo.infrastructure.security

import java.nio.CharBuffer
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
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
 *
 * The PBKDF2 step is hand-rolled on top of [Mac] "HmacSHA256" rather than
 * `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")`: that algorithm name isn't registered
 * until API 26, but this app's minSdk is 24, so relying on it would crash export/import on
 * Android 7.0/7.1. `Mac`/"HmacSHA256" has been available since API 1, and is used unconditionally
 * (not just as a low-API fallback) so every device derives keys the same way — no risk of two
 * implementations disagreeing on the same passphrase/salt/iterations.
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
            val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            // Explicit IV/tag length rather than relying on provider defaults for a bare
            // ENCRYPT_MODE init — decrypt() already hard-codes GCM_TAG_LENGTH_BITS, so encrypt
            // must produce exactly that, not whatever a given provider happens to default to.
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            return EncryptedEnvelope(
                kdf = KDF_NAME,
                iterations = PBKDF2_ITERATIONS,
                salt = salt,
                iv = iv,
                ciphertext = ciphertext,
            )
        }

        fun decrypt(
            envelope: EncryptedEnvelope,
            passphrase: CharArray,
        ): DecryptResult {
            // Reject an unsupported KDF or an out-of-range iteration count up front: an
            // untrusted/corrupted envelope could otherwise drive key derivation with a
            // wildly excessive iteration count (CPU-exhaustion on import) or trip
            // IllegalArgumentException out of the cipher setup, which isn't a
            // GeneralSecurityException and would otherwise escape uncaught below.
            if (envelope.kdf != KDF_NAME || envelope.iterations !in MIN_ITERATIONS..MAX_ITERATIONS) {
                return DecryptResult.Corrupt
            }
            return try {
                val key = deriveKey(passphrase, envelope.salt, envelope.iterations)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, envelope.iv))
                val plaintext = cipher.doFinal(envelope.ciphertext)
                DecryptResult.Success(String(plaintext, Charsets.UTF_8))
            } catch (_: AEADBadTagException) {
                DecryptResult.WrongPassphrase
            } catch (_: GeneralSecurityException) {
                DecryptResult.Corrupt
            } catch (_: IllegalArgumentException) {
                DecryptResult.Corrupt
            }
        }

        /**
         * PBKDF2WithHmacSHA256 (RFC 8018), single output block: HMAC-SHA256's 32-byte block
         * size already equals [KEY_LENGTH_BITS], so no multi-block concatenation is needed.
         */
        private fun deriveKey(
            passphrase: CharArray,
            salt: ByteArray,
            iterations: Int,
        ): SecretKeySpec {
            val passwordBytes = passphrase.toUtf8Bytes()
            return try {
                val mac = Mac.getInstance(HMAC_ALGORITHM)
                mac.init(SecretKeySpec(passwordBytes, HMAC_ALGORITHM))
                check(mac.macLength * BITS_PER_BYTE == KEY_LENGTH_BITS) {
                    "HmacSHA256 output length no longer matches the expected AES key length"
                }
                SecretKeySpec(pbkdf2Block(mac, salt, iterations), "AES")
            } finally {
                // The passphrase's UTF-8 bytes are our own copy (SecretKeySpec clones them
                // again internally) — clear promptly rather than leaving it to the GC.
                Arrays.fill(passwordBytes, 0)
            }
        }

        private companion object {
            const val KDF_NAME = "PBKDF2WithHmacSHA256"
            const val HMAC_ALGORITHM = "HmacSHA256"
            const val PBKDF2_ITERATIONS = 200_000
            const val KEY_LENGTH_BITS = 256
            const val BITS_PER_BYTE = 8
            const val SALT_LENGTH_BYTES = 16
            const val TRANSFORMATION = "AES/GCM/NoPadding"
            const val GCM_IV_LENGTH_BYTES = 12
            const val GCM_TAG_LENGTH_BITS = 128
            const val MIN_ITERATIONS = 1
            const val MAX_ITERATIONS = 2_000_000
        }
    }

/**
 * UTF-8-encodes a passphrase without ever materializing it as a [String] — unlike a char/byte
 * array, a String can't be zeroed afterward, so it would linger in memory indefinitely.
 */
private fun CharArray.toUtf8Bytes(): ByteArray {
    val encoded = Charsets.UTF_8.encode(CharBuffer.wrap(this))
    val bytes = ByteArray(encoded.remaining())
    encoded.get(bytes)
    return bytes
}

/**
 * Computes PBKDF2's F(P, S, c, i) = U1 xor U2 xor ... xor Uc for block index i, using [mac]
 * (already initialized with the password as its HMAC key). Internal, not private, so
 * [Pbkdf2HmacSha256Test] can verify it directly against published RFC 7914 test vectors.
 */
internal fun pbkdf2Block(
    mac: Mac,
    salt: ByteArray,
    iterations: Int,
    blockIndex: Int = 1,
): ByteArray {
    val blockIndexBytes =
        byteArrayOf(
            (blockIndex ushr 24).toByte(),
            (blockIndex ushr 16).toByte(),
            (blockIndex ushr 8).toByte(),
            blockIndex.toByte(),
        )
    mac.update(salt)
    var u = mac.doFinal(blockIndexBytes)
    val result = u.copyOf()
    repeat(iterations - 1) {
        u = mac.doFinal(u)
        for (i in result.indices) {
            result[i] = (result[i].toInt() xor u[i].toInt()).toByte()
        }
    }
    return result
}
