package com.mandrecode.tempo.features.routines.domain.scheduler

import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import kotlinx.datetime.LocalDateTime

interface HabitReminderScheduler {
    suspend fun scheduleHabit(habit: Habit): ScheduleResult

    suspend fun cancelHabit(habit: Habit)

    suspend fun scheduleHabitChain(habitChain: HabitChain): ScheduleResult

    suspend fun cancelHabitChain(habitChain: HabitChain)

    suspend fun scheduleHabit(
        habitId: Long,
        reminderDate: LocalDateTime,
    ): ScheduleResult

    suspend fun cancelHabit(habitId: Long)
}
