package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import jakarta.inject.Inject

class ClearAllHabitRemindersUseCase
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val habitReminderScheduler: HabitReminderScheduler,
    ) {
        suspend operator fun invoke() {
            val habitsWithReminders = habitRepository.getHabitsWithReminders()
            habitRepository.clearAllReminders()
            habitsWithReminders.forEach { habit ->
                habitReminderScheduler.cancelHabit(habit)
            }
        }
    }
