package com.mandrecode.tempo.features.routines.domain.model

data class HabitDeletionSnapshot(
    val habit: Habit,
    val affectedChains: List<HabitChain>,
)

data class HabitChainDeletionSnapshot(
    val chain: HabitChain,
    val habitsBeforeDeletion: List<Habit>,
    val affectedChains: List<HabitChain>,
    val deletedHabits: Boolean,
)
