package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import com.mandrecode.tempo.features.focus.domain.scheduler.FocusSessionScheduler
import jakarta.inject.Inject
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Starting, pausing, resuming and ending the one focus session.
 *
 * Grouped in a single class because every operation shares the same three side effects — persist
 * the session, reschedule the alarm, redraw the notification — and splitting them across four
 * classes would mean repeating that trio four times.
 */
class FocusSessionUseCases
    @Inject
    constructor(
        private val sessionRepository: FocusSessionRepository,
        private val activityRepository: DailyFocusActivityRepository,
        private val scheduler: FocusSessionScheduler,
        private val clock: Clock,
    ) {
        /**
         * Begins a session on [taskId]. Any session already running is ended first and its minutes
         * banked, so starting on a second task replaces rather than stacks.
         *
         * [lengthMinutes] overrides the configured length for this start alone, for the times one
         * session wants to be shorter or longer without changing what every session is.
         */
        suspend fun start(
            taskId: Long,
            taskTitle: String,
            isBreak: Boolean = false,
            lengthMinutes: Int? = null,
        ) {
            end()
            val configured =
                if (isBreak) {
                    sessionRepository.breakLengthMinutes.value
                } else {
                    sessionRepository.defaultLengthMinutes.value
                }
            val length = FocusSession.lengthOf(lengthMinutes ?: configured)
            apply(
                FocusSession.start(
                    taskId = taskId,
                    taskTitle = taskTitle,
                    now = clock.now(),
                    length = length,
                    isBreak = isBreak,
                ),
            )
        }

        fun pause() {
            val session = sessionRepository.activeSession.value ?: return
            if (session.isPaused) return
            // The alarm and its countdown are both tied to a wall-clock end time, so a paused
            // session must not keep either.
            scheduler.cancelSessionEnd()
            apply(session.pause(clock.now()), reschedule = false)
        }

        fun resume() {
            val session = sessionRepository.activeSession.value ?: return
            if (!session.isPaused) return
            apply(session.resume(clock.now()))
        }

        /**
         * Ends the session, banking whole elapsed minutes into today's activity record. Safe to
         * call when nothing is running.
         *
         * @return the session that was ended, or `null` if there was none.
         */
        suspend fun end(): FocusSession? {
            val session = sessionRepository.activeSession.value ?: return null
            val now = clock.now()

            scheduler.cancelSessionEnd()
            scheduler.clearOngoingNotification()
            sessionRepository.setActiveSession(null)

            // A break is time away from the work, so it is never banked as focus time, and it is
            // not a run at the task either.
            val today = clock.todayIn(TimeZone.currentSystemDefault())
            val minutes = if (session.isBreak) 0 else session.bankableMinutes(now)
            if (minutes > 0) {
                activityRepository.addFocusMinutes(today, minutes)
            }
            if (!session.isBreak) {
                sessionRepository.recordSessionFor(session.taskId, today)
            }
            return session
        }

        /**
         * Reconciles a session restored from storage on app start or after a reboot: one whose time
         * already ran out is completed rather than resumed, so no stale countdown is ever shown.
         *
         * @return the session that had already finished, or `null` if there was nothing to settle.
         */
        suspend fun reconcile(): FocusSession? {
            val session = sessionRepository.activeSession.value ?: return null
            return if (session.hasExpired(clock.now())) {
                end()
            } else {
                if (!session.isPaused) apply(session)
                null
            }
        }

        private fun apply(
            session: FocusSession,
            reschedule: Boolean = true,
        ) {
            sessionRepository.setActiveSession(session)
            if (reschedule) scheduler.scheduleSessionEnd(session)
            scheduler.showOngoingNotification(session)
        }
    }
