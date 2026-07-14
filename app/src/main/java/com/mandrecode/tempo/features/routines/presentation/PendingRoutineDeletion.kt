package com.mandrecode.tempo.features.routines.presentation

import com.mandrecode.tempo.features.routines.domain.model.HabitChainDeletionSnapshot
import com.mandrecode.tempo.features.routines.domain.model.HabitDeletionSnapshot

internal sealed interface PendingRoutineDeletion {
    data class Habit(
        val snapshot: HabitDeletionSnapshot,
    ) : PendingRoutineDeletion

    data class HabitChain(
        val snapshot: HabitChainDeletionSnapshot,
    ) : PendingRoutineDeletion
}
