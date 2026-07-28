package com.mandrecode.tempo.core.domain.repository

import com.mandrecode.tempo.core.domain.model.VacationPeriod
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

/**
 * Stores the app-level vacation periods that pause habit tracking.
 *
 * Every mutation is a plain persistence write: nothing here schedules, cancels, or posts
 * anything. Habit reminders keep rescheduling themselves throughout a pause and are filtered
 * at delivery time instead, so a pause has no alarm state to restore and survives reboots,
 * process death, and a revoked exact-alarm permission untouched.
 *
 * All mutations are idempotent and always leave the stored list in the canonical form
 * described by [VacationPeriod.normalize].
 */
interface VacationModeRepository {
    /** Every stored period, past and present, in canonical form. */
    val periods: StateFlow<List<VacationPeriod>>

    /**
     * Turns vacation mode on or off as of [today], mirroring the Settings switch.
     *
     * Pausing starts an open-ended period on [today], and is a no-op when [today] is already
     * paused, so it can never produce a duplicate or overlapping period. Un-pausing ends the
     * covering period on the day *before* [today] — otherwise the switch, which reads "is today
     * paused", would spring straight back on — and removes it entirely when the pause also
     * started today. Un-pausing while nothing is paused is a no-op.
     */
    fun setPaused(
        today: LocalDate,
        paused: Boolean,
    )

    /**
     * Sets (or, with `null`, clears) the planned end date of the period covering [today].
     * Ignored when [today] is not paused or when [endInclusive] precedes that period's start.
     */
    fun setPlannedEnd(
        today: LocalDate,
        endInclusive: LocalDate?,
    )

    /** Replaces the whole list, for a full-restore import. Normalizes whatever it is given. */
    fun replaceAll(periods: List<VacationPeriod>)
}
