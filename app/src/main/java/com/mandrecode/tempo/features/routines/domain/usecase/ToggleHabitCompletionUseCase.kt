package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import com.mandrecode.tempo.features.routines.domain.util.HabitReminderDateUtil
import com.mandrecode.tempo.features.routines.domain.util.ReminderDateUtil
import com.mandrecode.tempo.util.CompletionHistoryUtil
import jakarta.inject.Inject
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ToggleHabitCompletionUseCase
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val habitChainRepository: HabitChainRepository,
        private val habitReminderScheduler: HabitReminderScheduler,
        private val updateHabitUseCase: UpdateHabitUseCase,
        private val clock: Clock,
    ) {
        suspend operator fun invoke(
            habitId: Long,
            isCompleted: Boolean,
            selectedDate: LocalDate,
            fromNotification: Boolean = false,
        ) {
            habitRepository.toggleHabitCompletion(habitId, isCompleted, selectedDate, fromNotification)

            val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val currentDate = now.date
            if (isCompleted && (selectedDate == currentDate || fromNotification)) {
                val habit = habitRepository.getHabitById(habitId)
                if (habit != null) {
                    if (habit.reminderDate != null) {
                        val advancedReminder =
                            if (habit.reminderDate.date > currentDate) {
                                // Already moved past today (e.g., by the notification receiver);
                                // keep the date but validate the day-of-week.
                                HabitReminderDateUtil.advanceReminderIfNeeded(
                                    habit.reminderDate,
                                    habit.repeatDays,
                                    now,
                                )
                            } else {
                                // Today or past — always advance to the next occurrence so an
                                // already-completed day doesn't trigger a redundant notification.
                                ReminderDateUtil.calculateNextReminderDate(
                                    habit.reminderDate,
                                    habit.repeatDays.orDaily(),
                                    now,
                                )
                            }
                        updateHabitUseCase(habit.copy(reminderDate = advancedReminder))
                    } else {
                        habitReminderScheduler.cancelHabit(habit)
                    }
                }

                rescheduleCompletedChains(habitId, selectedDate, now)
            } else if (!isCompleted && (selectedDate == currentDate || fromNotification)) {
                val habit = habitRepository.getHabitById(habitId)
                if (habit != null) {
                    if (habit.reminderDate != null) {
                        val restoredReminder = LocalDateTime(selectedDate, habit.reminderDate.time)
                        updateHabitUseCase(habit.copy(reminderDate = restoredReminder))
                    }
                }

                restoreUncompletedChains(habitId, selectedDate, now)
            }
        }

        private suspend fun restoreUncompletedChains(
            habitId: Long,
            selectedDate: LocalDate,
            now: LocalDateTime,
        ) {
            val chains = habitChainRepository.getChainsForHabit(habitId)

            for (chain in chains) {
                if (chain.periodicReminder == null) continue

                val restoredReminder = LocalDateTime(selectedDate, chain.periodicReminder.time)
                val advancedReminder = HabitReminderDateUtil.advanceReminderIfNeeded(restoredReminder, chain.repeatDays, now)
                val updatedChain = chain.copy(periodicReminder = advancedReminder)
                habitChainRepository.updateHabitChain(updatedChain)
                habitReminderScheduler.cancelHabitChain(chain)
                habitReminderScheduler.scheduleHabitChain(updatedChain)
            }
        }

        private suspend fun rescheduleCompletedChains(
            habitId: Long,
            selectedDate: LocalDate,
            now: LocalDateTime,
        ) {
            val chains = habitChainRepository.getChainsForHabit(habitId)
            val dateStr = selectedDate.toString()

            val allHabitIds =
                chains
                    .filter { it.periodicReminder != null }
                    .flatMap { it.habitIds }
                    .distinct()
            val habitsById =
                if (allHabitIds.isNotEmpty()) {
                    habitRepository.getHabitsByIds(allHabitIds).associateBy { it.id }
                } else {
                    emptyMap()
                }

            for (chain in chains) {
                if (chain.periodicReminder == null) continue

                val chainHabits = chain.habitIds.mapNotNull { habitsById[it] }
                val allCompleted =
                    chainHabits.isNotEmpty() &&
                        chainHabits.size == chain.habitIds.size &&
                        chainHabits.all { h ->
                            CompletionHistoryUtil.isDateInHistory(h.completionHistory, dateStr)
                        }

                if (allCompleted) {
                    val days = chain.repeatDays.orDaily()
                    val nextReminder =
                        if (chain.periodicReminder.date > now.date) {
                            HabitReminderDateUtil.advanceReminderIfNeeded(chain.periodicReminder, days, now)
                        } else {
                            ReminderDateUtil.calculateNextReminderDate(chain.periodicReminder, days, now)
                        }
                    val updatedChain = chain.copy(periodicReminder = nextReminder)
                    habitChainRepository.updateHabitChain(updatedChain)
                    habitReminderScheduler.cancelHabitChain(chain)
                    habitReminderScheduler.scheduleHabitChain(updatedChain)
                }
            }
        }
    }

private fun Set<DayOfWeek>?.orDaily(): Set<DayOfWeek> = if (isNullOrEmpty()) DayOfWeek.ALL_DAYS else this
