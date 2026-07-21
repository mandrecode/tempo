package com.mandrecode.tempo.features.backup.data.model

import kotlinx.serialization.Serializable

/**
 * On-disk backup format, schema version 1 — documented in `docs/BACKUP_FORMAT.md`.
 *
 * These DTOs are the stable persisted contract: they are deliberately decoupled
 * from Room entities and domain models so refactors cannot silently change the
 * file format. Evolution rules: fields may only be added with defaults under the
 * same [BackupFileDto.schemaVersion]; renames, removals or semantic changes must
 * bump it. Dates are ISO-8601 local date-times, enums are serialized by name,
 * repeat days are ISO day numbers (Monday = 1).
 */
@Serializable
data class BackupFileDto(
    val schemaVersion: Int,
    val appVersion: String = "",
    val exportedAt: String = "",
    val categories: List<CategoryBackupDto> = emptyList(),
    val tasks: List<TaskBackupDto> = emptyList(),
    val habits: List<HabitBackupDto> = emptyList(),
    val habitChains: List<HabitChainBackupDto> = emptyList(),
    val habitChainMembers: List<ChainMemberBackupDto> = emptyList(),
    val settings: SettingsBackupDto? = null,
)

/** App configuration section; absent in files written before it existed. */
@Serializable
data class SettingsBackupDto(
    val themeMode: String = "SYSTEM",
    val useTempoColors: Boolean = false,
    val routinesTabEnabled: Boolean = true,
    val tasksTabEnabled: Boolean = true,
    val defaultTab: String = "ROUTINES",
    val autoRemoveCompletedTasks: Boolean = false,
    val completedTaskRetentionDays: Int = 30,
)

/** Minimal projection used to read [BackupFileDto.schemaVersion] before full decoding. */
@Serializable
data class BackupEnvelopeDto(
    val schemaVersion: Int,
)

/**
 * On-disk shape of an encrypted export (the `.tempo` format) — an outer envelope wrapping an
 * AES-256-GCM-encrypted [BackupFileDto] JSON string, keyed by a passphrase-derived key (see
 * `BackupEncryptionService`). Orthogonal to [BackupFileDto.schemaVersion]: this only changes
 * when the *encryption* format changes, documented in `docs/BACKUP_FORMAT.md`.
 */
@Serializable
data class BackupEncryptedEnvelopeDto(
    val encryptionVersion: Int = 1,
    val kdf: String,
    val iterations: Int,
    val salt: String,
    val iv: String,
    val ciphertext: String,
)

@Serializable
data class CategoryBackupDto(
    val id: Long,
    val name: String,
    val color: String? = null,
    val icon: String? = null,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
)

@Serializable
data class TaskBackupDto(
    val id: Long,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val categoryId: Long,
    val priority: String? = null,
    val reminderDate: String? = null,
    val periodicity: String? = null,
    val periodicityInterval: Int = 1,
    val repeatDays: List<Int>? = null,
    val monthDayOption: String? = null,
    val parentTaskId: Long? = null,
    val sortOrder: Int = 0,
    val completedAt: String? = null,
    val nextInstanceId: Long? = null,
)

@Serializable
data class HabitBackupDto(
    val id: Long,
    val title: String,
    val description: String = "",
    val icon: String? = null,
    val colorKey: String? = null,
    val reminderDate: String? = null,
    val isCompleted: Boolean = false,
    val habitType: String = "BUILD",
    val createdDate: String,
    val completionHistory: String = "",
    val repeatDays: List<Int>? = null,
)

@Serializable
data class HabitChainBackupDto(
    val id: Long,
    val title: String,
    val description: String = "",
    val colorKey: String? = null,
    val icon: String? = null,
    val periodicReminder: String? = null,
    val createdDate: String,
    val completionHistory: String = "",
    val repeatDays: List<Int>? = null,
)

@Serializable
data class ChainMemberBackupDto(
    val chainId: Long,
    val habitId: Long,
    val sortOrder: Int,
)
