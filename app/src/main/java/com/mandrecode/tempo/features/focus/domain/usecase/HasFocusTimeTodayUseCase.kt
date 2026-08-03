package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import jakarta.inject.Inject
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Whether a task has had focus time today — a session banked against it, minutes worked on it, or a
 * timer running on it right now.
 *
 * Exists so the reminder paths can stop talking over the work. A notification saying "here is that
 * task" is worth nothing to someone who has just spent twenty-five minutes on it, and arriving in
 * the same shade as the session's own "25 minutes done" made Tempo look like two apps.
 *
 * Resolves today itself: both callers are background entry points with no selected day, and the
 * whole point of the question is which day the work landed on.
 */
class HasFocusTimeTodayUseCase
    @Inject
    constructor(
        private val sessionRepository: FocusSessionRepository,
        private val clock: Clock,
    ) {
        operator fun invoke(taskId: Long): Boolean {
            // The running session first: it is a field read, where the day's record is a stored one,
            // and the sweep asks this once per overdue task.
            //
            // Asked at all because minutes are only banked when a session ends — the record alone
            // would let a reminder through five minutes into a run. A break counts too: it is the
            // rest half of the work that just happened on this task, not a gap in it.
            if (sessionRepository.activeSession.value?.taskId == taskId) return true

            val today = clock.todayIn(TimeZone.currentSystemDefault())
            return sessionRepository.focusOn(taskId, today).hasHistory
        }
    }
