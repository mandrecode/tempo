package com.mandrecode.tempo.features.focus.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * A running or paused focus session.
 *
 * There is at most one of these at a time — starting a session on another item replaces this one
 * rather than queueing. Modelled as a single nullable value everywhere for that reason, never a
 * collection.
 *
 * Time is kept as two pieces: [completedBeforeNow], the time banked by earlier run segments, and
 * [runningSince], when the current segment began. Nothing has to tick — remaining time is always
 * derived from the clock, so the session stays correct across process death, and the notification's
 * chronometer counts down against the same [endsAt] the alarm fires at.
 */
data class FocusSession(
    val taskId: Long,
    val taskTitle: String,
    val plannedLength: Duration,
    /** A break counts down and notifies like a session, but banks no focus minutes. */
    val isBreak: Boolean = false,
    /** Time already banked by previous run segments; grows on each pause. */
    val completedBeforeNow: Duration = Duration.ZERO,
    /** Start of the current run segment, or `null` while paused. */
    val runningSince: Instant? = null,
) {
    val isPaused: Boolean get() = runningSince == null

    /** When the session will finish if left running. Meaningless while paused. */
    val endsAt: Instant?
        get() = runningSince?.plus(plannedLength - completedBeforeNow)

    fun elapsed(now: Instant): Duration {
        val current = runningSince?.let { now - it } ?: Duration.ZERO
        return (completedBeforeNow + current).coerceIn(Duration.ZERO, plannedLength)
    }

    fun remaining(now: Instant): Duration = (plannedLength - elapsed(now)).coerceAtLeast(Duration.ZERO)

    fun hasExpired(now: Instant): Boolean = !isPaused && remaining(now) <= Duration.ZERO

    /** Whole minutes worth banking; a session stopped inside its first minute banks nothing. */
    fun bankableMinutes(now: Instant): Int = elapsed(now).inWholeMinutes.toInt()

    fun pause(now: Instant): FocusSession =
        if (isPaused) {
            this
        } else {
            copy(completedBeforeNow = elapsed(now), runningSince = null)
        }

    fun resume(now: Instant): FocusSession = if (isPaused) copy(runningSince = now) else this

    companion object {
        val DEFAULT_LENGTH: Duration = 25.minutes

        /** Fixed for v1 — configuring both lengths is more choice than the feature needs yet. */
        val BREAK_LENGTH: Duration = 5.minutes

        val SUPPORTED_LENGTHS_MINUTES: List<Int> = listOf(15, 20, 25, 30, 45, 60)

        fun start(
            taskId: Long,
            taskTitle: String,
            now: Instant,
            length: Duration = DEFAULT_LENGTH,
            isBreak: Boolean = false,
        ): FocusSession =
            FocusSession(
                taskId = taskId,
                taskTitle = taskTitle,
                plannedLength = length,
                isBreak = isBreak,
                runningSince = now,
            )

        fun lengthOf(minutes: Int): Duration = minutes.coerceAtLeast(1).minutes
    }
}
