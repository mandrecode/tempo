package com.mandrecode.tempo.infrastructure.reminders

import com.mandrecode.tempo.features.focus.domain.usecase.HasFocusTimeTodayUseCase
import com.mandrecode.tempo.features.tasks.domain.repository.MissedReminderPreferences
import com.mandrecode.tempo.features.tasks.domain.scheduler.MissedReminderScheduler
import com.mandrecode.tempo.features.tasks.domain.usecase.GetOverdueIncompleteTasksUseCase
import com.mandrecode.tempo.infrastructure.notifications.TaskReminderNotifier
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

/**
 * The daily catch-up sweep, split out of [receivers.MissedReminderCatchUpReceiver] so it can be
 * exercised without an Android runtime.
 */
@Singleton
class MissedReminderCatchUpRunner
    @Inject
    constructor(
        private val preferences: MissedReminderPreferences,
        private val getOverdueIncompleteTasks: GetOverdueIncompleteTasksUseCase,
        private val taskReminderNotifier: TaskReminderNotifier,
        private val hasFocusTimeToday: HasFocusTimeTodayUseCase,
        private val missedReminderScheduler: MissedReminderScheduler,
        private val clock: Clock,
    ) {
        suspend fun run() {
            try {
                if (!preferences.isEnabled.value) return
                // One instant for the whole sweep, so no task is judged against two "now"s.
                val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
                getOverdueIncompleteTasks(now)
                    // The sweep exists to catch what slipped past you, and work you have already
                    // put a session into today is the opposite of that.
                    .filterNot { hasFocusTimeToday(it.id) }
                    .forEach(taskReminderNotifier::notify)
            } finally {
                // Losing tomorrow's catch-up would be worse than a failed sweep, so re-arm even
                // when the sweep threw.
                missedReminderScheduler.sync()
            }
        }
    }
