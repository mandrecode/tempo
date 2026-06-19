package com.mandrecode.tempo.core.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room relation class that combines a HabitChainEntity with its member entries.
 * Used for @Transaction queries that need the full chain + member IDs.
 */
data class HabitChainWithMembers(
    @Embedded val chain: HabitChainEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "chainId",
        entity = HabitChainMemberEntity::class,
    )
    val members: List<HabitChainMemberEntity>,
)
