package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import jakarta.inject.Inject

class DeleteHabitChainUseCase
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val habitChainRepository: HabitChainRepository,
        private val habitReminderScheduler: HabitReminderScheduler,
    ) {
        suspend operator fun invoke(
            habitChain: HabitChain,
            deleteHabits: Boolean,
        ) {
            val habitIds = habitChain.habitIds

            if (deleteHabits) {
                if (habitIds.isNotEmpty()) {
                    habitRepository.deleteHabitsByIds(habitIds)
                    habitIds.forEach { habitId ->
                        habitReminderScheduler.cancelHabit(habitId)
                    }
                }
            } else {
                if (habitIds.isNotEmpty() && habitChain.periodicReminder != null) {
                    habitRepository.updateHabitsReminder(habitIds, habitChain.periodicReminder)
                    habitIds.forEach { habitId ->
                        habitReminderScheduler.scheduleHabit(habitId, habitChain.periodicReminder)
                    }
                }
            }

            habitChainRepository.deleteHabitChain(habitChain)
            habitReminderScheduler.cancelHabitChain(habitChain)
        }
    }
