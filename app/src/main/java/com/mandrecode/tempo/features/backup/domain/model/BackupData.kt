package com.mandrecode.tempo.features.backup.domain.model

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
 */
data class BackupData(
    val categories: List<Category>,
    val tasks: List<Task>,
    val habits: List<Habit>,
    val habitChains: List<HabitChain>,
    val chainMemberships: List<ChainMembership>,
)

/**
 * A single habit-chain membership row: [habitId] belongs to [chainId] at
 * position [sortOrder] within the chain.
 */
data class ChainMembership(
    val chainId: Long,
    val habitId: Long,
    val sortOrder: Int,
)
