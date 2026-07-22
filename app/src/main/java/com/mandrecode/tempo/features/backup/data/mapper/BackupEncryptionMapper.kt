package com.mandrecode.tempo.features.backup.data.mapper

import com.mandrecode.tempo.features.backup.data.model.BackupEncryptedEnvelopeDto
import com.mandrecode.tempo.features.backup.data.model.ENCRYPTION_ENVELOPE_VERSION
import com.mandrecode.tempo.infrastructure.security.EncryptedEnvelope
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Maps the generic [EncryptedEnvelope] crypto result to/from its base64 on-disk JSON shape.
 * Uses `kotlin.io.encoding.Base64` (pure Kotlin stdlib) rather than `android.util.Base64` or
 * `java.util.Base64`: the former isn't available in plain JVM unit tests (no Robolectric in
 * this project) and the latter requires API 26+, above this app's minSdk 24.
 */

@OptIn(ExperimentalEncodingApi::class)
fun EncryptedEnvelope.toDto(): BackupEncryptedEnvelopeDto =
    BackupEncryptedEnvelopeDto(
        encryptionVersion = ENCRYPTION_ENVELOPE_VERSION,
        kdf = kdf,
        iterations = iterations,
        salt = Base64.Default.encode(salt),
        iv = Base64.Default.encode(iv),
        ciphertext = Base64.Default.encode(ciphertext),
    )

@OptIn(ExperimentalEncodingApi::class)
fun BackupEncryptedEnvelopeDto.toEnvelope(): EncryptedEnvelope =
    EncryptedEnvelope(
        kdf = kdf,
        iterations = iterations,
        salt = Base64.Default.decode(salt),
        iv = Base64.Default.decode(iv),
        ciphertext = Base64.Default.decode(ciphertext),
    )
