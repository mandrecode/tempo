package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.core.domain.model.RestoreResult
import com.mandrecode.tempo.features.routines.domain.model.HabitDeletionSnapshot
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import jakarta.inject.Inject

class RestoreDeletedHabitUseCase
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val habitReminderScheduler: HabitReminderScheduler,
    ) {
        suspend operator fun invoke(snapshot: HabitDeletionSnapshot): RestoreResult {
            habitRepository.restoreDeletedHabit(snapshot)
            habitReminderScheduler.cancelHabit(snapshot.habit)
            val results =
                if (snapshot.habit.reminderDate == null) {
                    emptyList()
                } else {
                    listOf(habitReminderScheduler.scheduleHabit(snapshot.habit))
                }
            return RestoreResult(results)
        }
    }
