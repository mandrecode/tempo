package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.core.domain.model.RestoreResult
import com.mandrecode.tempo.features.routines.domain.model.HabitChainDeletionSnapshot
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import jakarta.inject.Inject

class RestoreDeletedHabitChainUseCase
    @Inject
    constructor(
        private val habitChainRepository: HabitChainRepository,
        private val habitReminderScheduler: HabitReminderScheduler,
    ) {
        suspend operator fun invoke(snapshot: HabitChainDeletionSnapshot): RestoreResult {
            habitChainRepository.restoreDeletedHabitChain(snapshot)
            snapshot.habitsBeforeDeletion.forEach { habitReminderScheduler.cancelHabit(it) }
            snapshot.affectedChains.forEach { habitReminderScheduler.cancelHabitChain(it) }
            val habitResults =
                snapshot.habitsBeforeDeletion
                    .filter { it.reminderDate != null }
                    .map { habitReminderScheduler.scheduleHabit(it) }
            val chainResults =
                snapshot.affectedChains
                    .filter { it.periodicReminder != null }
                    .map { habitReminderScheduler.scheduleHabitChain(it) }
            return RestoreResult(habitResults + chainResults)
        }
    }
