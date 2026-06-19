package com.mandrecode.tempo.features.tasks.domain.scheduler

import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.tasks.domain.model.Task

interface TaskReminderScheduler {
    fun schedule(task: Task): ScheduleResult

    fun cancel(task: Task)

    /**
     * Dismisses any active tray notification for the given task without
     * touching its scheduled alarm. Useful when the alarm is being
     * overwritten (e.g. the reminder is updated) and the previously-shown
     * notification should no longer be visible.
     */
    fun dismissNotification(taskId: Long)
}
