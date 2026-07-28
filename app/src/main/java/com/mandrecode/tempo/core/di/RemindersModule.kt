package com.mandrecode.tempo.core.di

import android.content.Context
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import com.mandrecode.tempo.features.tasks.domain.scheduler.MissedReminderScheduler
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import com.mandrecode.tempo.infrastructure.notifications.NotificationSyncManager
import com.mandrecode.tempo.infrastructure.reminders.MissedReminderSchedulerImpl
import com.mandrecode.tempo.infrastructure.reminders.scheduler.HabitAlarmScheduler
import com.mandrecode.tempo.infrastructure.reminders.scheduler.HabitReminderSchedulerImpl
import com.mandrecode.tempo.infrastructure.reminders.scheduler.MissedReminderAlarmScheduler
import com.mandrecode.tempo.infrastructure.reminders.scheduler.TaskAlarmScheduler
import com.mandrecode.tempo.infrastructure.reminders.scheduler.TaskReminderSchedulerImpl
import com.mandrecode.tempo.infrastructure.reminders.scheduler.android.AndroidHabitAlarmScheduler
import com.mandrecode.tempo.infrastructure.reminders.scheduler.android.AndroidMissedReminderAlarmScheduler
import com.mandrecode.tempo.infrastructure.reminders.scheduler.android.AndroidTaskAlarmScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Alarm-backed reminder delivery: per-entity reminder schedulers plus the daily
 * missed-reminder catch-up.
 */
@Module
@InstallIn(SingletonComponent::class)
object RemindersModule {
    @Provides
    @Singleton
    fun provideTaskReminderScheduler(
        @ApplicationContext context: Context,
        notificationSyncManager: NotificationSyncManager,
        taskAlarmScheduler: TaskAlarmScheduler,
    ): TaskReminderScheduler = TaskReminderSchedulerImpl(context, notificationSyncManager, taskAlarmScheduler)

    @Provides
    @Singleton
    fun provideHabitReminderScheduler(
        @ApplicationContext context: Context,
        notificationSyncManager: NotificationSyncManager,
        habitAlarmScheduler: HabitAlarmScheduler,
    ): HabitReminderScheduler = HabitReminderSchedulerImpl(context, notificationSyncManager, habitAlarmScheduler)

    @Provides
    @Singleton
    fun provideTaskAlarmScheduler(
        @ApplicationContext context: Context,
    ): TaskAlarmScheduler = AndroidTaskAlarmScheduler(context)

    @Provides
    @Singleton
    fun provideHabitAlarmScheduler(
        @ApplicationContext context: Context,
    ): HabitAlarmScheduler = AndroidHabitAlarmScheduler(context)

    @Provides
    @Singleton
    fun provideMissedReminderAlarmScheduler(
        @ApplicationContext context: Context,
    ): MissedReminderAlarmScheduler = AndroidMissedReminderAlarmScheduler(context)

    @Provides
    @Singleton
    fun provideMissedReminderScheduler(impl: MissedReminderSchedulerImpl): MissedReminderScheduler = impl
}
