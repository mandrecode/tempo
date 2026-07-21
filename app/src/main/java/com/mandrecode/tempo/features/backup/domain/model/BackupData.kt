package com.mandrecode.tempo.features.backup.domain.model

import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task

/**
 * Full snapshot of the user's data as carried by a backup file.
 *
 * [chainMemberships] is the authoritative chain-to-habit relation; the
 * [HabitChain.habitIds] field on the contained chains is not read by
 * backup code (it is derived state populated by the routines data layer).
 *
 * [settings] is null for files that predate the settings section; imports
 * apply it only in [ImportMode.REPLACE] (a full restore), never on merge.
 */
data class BackupData(
    val categories: List<Category>,
    val tasks: List<Task>,
    val habits: List<Habit>,
    val habitChains: List<HabitChain>,
    val chainMemberships: List<ChainMembership>,
    val settings: BackupSettings? = null,
)

/** The app configuration carried by a backup file. */
data class BackupSettings(
    val themeMode: ThemeMode,
    val useTempoColors: Boolean,
    val routinesTabEnabled: Boolean,
    val tasksTabEnabled: Boolean,
    val defaultTab: BackupDefaultTab,
    val autoRemoveCompletedTasks: Boolean,
    val completedTaskRetentionDays: Int,
)

enum class BackupDefaultTab {
    ROUTINES,
    TASKS,
}

/**
 * A single habit-chain membership row: [habitId] belongs to [chainId] at
 * position [sortOrder] within the chain.
 */
data class ChainMembership(
    val chainId: Long,
    val habitId: Long,
    val sortOrder: Int,
)
