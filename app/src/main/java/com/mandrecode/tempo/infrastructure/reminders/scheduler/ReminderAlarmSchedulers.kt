package com.mandrecode.tempo.infrastructure.reminders.scheduler

interface TaskAlarmScheduler {
    fun canScheduleExactAlarms(): Boolean

    fun scheduleTaskReminder(
        taskId: Long,
        triggerAtMillis: Long,
    )

    fun cancelTaskReminder(taskId: Long)
}

interface MissedReminderAlarmScheduler {
    fun canScheduleExactAlarms(): Boolean

    fun scheduleCatchUp(triggerAtMillis: Long)

    fun cancelCatchUp()
}

interface HabitAlarmScheduler {
    fun canScheduleExactAlarms(): Boolean

    fun scheduleHabitReminder(
        habitId: Long,
        triggerAtMillis: Long,
    )

    fun cancelHabitReminder(habitId: Long)

    fun scheduleHabitChainReminder(
        habitChainId: Long,
        triggerAtMillis: Long,
    )

    fun cancelHabitChainReminder(habitChainId: Long)
}
