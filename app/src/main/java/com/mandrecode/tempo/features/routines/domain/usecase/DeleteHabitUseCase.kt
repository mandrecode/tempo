package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import jakarta.inject.Inject

class DeleteHabitUseCase
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val habitReminderScheduler: HabitReminderScheduler,
    ) {
        suspend operator fun invoke(habit: Habit) {
            habitRepository.deleteHabit(habit)
            habitReminderScheduler.cancelHabit(habit)
        }
    }
