package com.mandrecode.tempo.infrastructure.reminders.scheduler

import android.content.Context
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import com.mandrecode.tempo.infrastructure.notifications.NotificationSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class HabitReminderSchedulerImpl(
    private val context: Context,
    private val notificationSyncManager: NotificationSyncManager,
    private val habitAlarmScheduler: HabitAlarmScheduler,
    private val currentTimeMillisProvider: () -> Long = { System.currentTimeMillis() },
) : HabitReminderScheduler {
    override suspend fun scheduleHabit(habit: Habit): ScheduleResult =
        withContext(Dispatchers.IO) {
            val reminderDate =
                habit.reminderDate
                    ?: return@withContext ScheduleResult.Failure(context.getString(R.string.error_habit_no_reminder_date))
            scheduleHabit(habit.id, reminderDate)
        }

    override suspend fun scheduleHabit(
        habitId: Long,
        reminderDate: LocalDateTime,
    ): ScheduleResult =
        withContext(Dispatchers.IO) {
            val triggerAtMillis =
                reminderDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val currentTimeMillis = currentTimeMillisProvider()

            // Silently skip past reminders — periodicity will reschedule them
            if (triggerAtMillis < currentTimeMillis) {
                return@withContext ScheduleResult.Skipped
            }

            if (!habitAlarmScheduler.canScheduleExactAlarms()) {
                return@withContext ScheduleResult.PermissionError(context.getString(R.string.error_exact_alarm_permission))
            }

            try {
                habitAlarmScheduler.scheduleHabitReminder(habitId, triggerAtMillis)
            } catch (_: SecurityException) {
                return@withContext ScheduleResult.PermissionError(context.getString(R.string.error_security_exception))
            }

            return@withContext ScheduleResult.Success(reminderDate)
        }

    override suspend fun cancelHabit(habit: Habit) =
        withContext(Dispatchers.IO) {
            cancelHabit(habit.id)
        }

    override suspend fun cancelHabit(habitId: Long) =
        withContext(Dispatchers.IO) {
            habitAlarmScheduler.cancelHabitReminder(habitId)
            notificationSyncManager.dismissHabitNotification(habitId)
        }

    override suspend fun scheduleHabitChain(habitChain: HabitChain): ScheduleResult =
        withContext(Dispatchers.IO) {
            val reminderDate =
                habitChain.periodicReminder
                    ?: return@withContext ScheduleResult.Failure(context.getString(R.string.error_habit_chain_no_reminder_date))

            val triggerAtMillis =
                reminderDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val currentTimeMillis = currentTimeMillisProvider()

            // Silently skip past reminders — periodicity will reschedule them
            if (triggerAtMillis < currentTimeMillis) {
                return@withContext ScheduleResult.Skipped
            }

            if (!habitAlarmScheduler.canScheduleExactAlarms()) {
                return@withContext ScheduleResult.PermissionError(context.getString(R.string.error_exact_alarm_permission))
            }

            try {
                habitAlarmScheduler.scheduleHabitChainReminder(habitChain.id, triggerAtMillis)
            } catch (_: SecurityException) {
                return@withContext ScheduleResult.PermissionError(context.getString(R.string.error_security_exception))
            }

            return@withContext ScheduleResult.Success(reminderDate)
        }

    override suspend fun cancelHabitChain(habitChain: HabitChain) =
        withContext(Dispatchers.IO) {
            habitAlarmScheduler.cancelHabitChainReminder(habitChain.id)
            notificationSyncManager.dismissHabitChainNotification(habitChain.id)
        }
}
