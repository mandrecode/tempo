package com.mandrecode.tempo.core.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table representing the many-to-many relationship between habit chains and habits.
 * Replaces the CSV-based habitIds field in HabitChainEntity.
 */
@Entity(
    tableName = "habit_chain_members",
    primaryKeys = ["chainId", "habitId"],
    foreignKeys = [
        ForeignKey(
            entity = HabitChainEntity::class,
            parentColumns = ["id"],
            childColumns = ["chainId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habitId")],
)
data class HabitChainMemberEntity(
    val chainId: Long,
    val habitId: Long,
    val sortOrder: Int,
)
