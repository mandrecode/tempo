package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitChainDeletionSnapshot
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import jakarta.inject.Inject

class DeleteHabitChainUseCase
    @Inject
    constructor(
        private val habitChainRepository: HabitChainRepository,
        private val habitReminderScheduler: HabitReminderScheduler,
    ) {
        suspend operator fun invoke(
            habitChain: HabitChain,
            deleteHabits: Boolean,
        ): HabitChainDeletionSnapshot {
            val snapshot = habitChainRepository.deleteHabitChainWithSnapshot(habitChain.id, deleteHabits)
            val persistedChain = snapshot.chain
            val habitIds = persistedChain.habitIds

            if (deleteHabits) {
                if (habitIds.isNotEmpty()) {
                    habitIds.forEach { habitId ->
                        habitReminderScheduler.cancelHabit(habitId)
                    }
                }
            } else {
                if (habitIds.isNotEmpty() && persistedChain.periodicReminder != null) {
                    habitIds.forEach { habitId ->
                        habitReminderScheduler.scheduleHabit(habitId, persistedChain.periodicReminder)
                    }
                }
            }

            habitReminderScheduler.cancelHabitChain(persistedChain)
            return snapshot
        }
    }
