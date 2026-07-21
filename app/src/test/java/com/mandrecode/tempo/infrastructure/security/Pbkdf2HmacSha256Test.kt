package com.mandrecode.tempo.infrastructure.security

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Verifies [pbkdf2Block] — the hand-rolled PBKDF2-HMAC-SHA256 step [BackupEncryptionService]
 * uses instead of `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")` (unavailable below API
 * 26, while this app's minSdk is 24) — against the published PBKDF2-HMAC-SHA256 test vectors
 * from RFC 7914 Appendix, whose 32-byte (dkLen=32) vectors match a single output block exactly.
 */
class Pbkdf2HmacSha256Test {
    @Test
    fun `matches RFC 7914 test vector 1 (password, salt, 1 iteration)`() {
        val derived = derive(password = "password", salt = "salt", iterations = 1)

        assertThat(derived.toHex())
            .isEqualTo("120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b")
    }

    @Test
    fun `matches RFC 7914 test vector 2 (password, salt, 2 iterations)`() {
        val derived = derive(password = "password", salt = "salt", iterations = 2)

        assertThat(derived.toHex())
            .isEqualTo("ae4d0c95af6b46d32d0adff928f06dd02a303f8ef3c251dfd6e2d85a95474c43")
    }

    @Test
    fun `matches an independently computed vector at the app's production iteration count`() {
        // Cross-checked against Python's hashlib.pbkdf2_hmac (a separate, trusted implementation)
        // at the real 200,000 iterations this app uses, not just RFC 7914's low-iteration vectors.
        val derived =
            derive(
                password = "longer passphrase with spaces",
                salt = "0123456789abcdef0123456789abcdef",
                iterations = 200_000,
            )

        assertThat(derived.toHex())
            .isEqualTo("9752402c1252c52aa43d5fe009df1e61f09e5f43774da8b5ad030fe81619020e")
    }

    private fun derive(
        password: String,
        salt: String,
        iterations: Int,
    ): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(password.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return pbkdf2Block(mac, salt.toByteArray(Charsets.UTF_8), iterations)
    }

    // Mask to an unsigned int before formatting: relying on "%02x" alone to render a negative
    // Byte as exactly two hex digits depends on java.util.Formatter's own byte-specific handling
    // rather than anything this code controls.
    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it.toInt() and 0xFF) }
}
