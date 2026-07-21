package com.mandrecode.tempo.features.backup.domain.scheduler

/**
 * Reminder side effects around an import. These touch the platform alarm
 * infrastructure, never the database, and are deliberately kept outside the
 * import transaction: rescheduling rebuilds alarms from whatever the database
 * contains, so running it after a rolled-back import restores the prior alarms.
 */
interface BackupReminderScheduler {
    /** Cancels the alarms of every task, habit and chain that currently has a reminder. */
    suspend fun cancelAllReminders()

    /** Rebuilds all reminder alarms from current database state (idempotent). */
    fun rescheduleAllReminders()
}
