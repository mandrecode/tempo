package com.mandrecode.tempo.features.tasks.domain.scheduler

/**
 * Keeps the daily missed-reminder catch-up trigger in sync with the user's preferences.
 */
interface MissedReminderScheduler {
    /**
     * Arms the next catch-up for the configured time, or cancels any armed catch-up when the
     * feature is disabled. Idempotent: repeated calls leave at most one armed catch-up.
     */
    fun sync()
}
