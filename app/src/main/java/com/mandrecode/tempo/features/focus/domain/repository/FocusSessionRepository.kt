package com.mandrecode.tempo.features.focus.domain.repository

import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import kotlinx.coroutines.flow.StateFlow

/**
 * The single active focus session, or `null` when none is running.
 *
 * Backed by preferences rather than Room: it is one small record with no history to query, and it
 * has to be readable from a broadcast receiver on a cold start without opening the encrypted
 * database.
 */
interface FocusSessionRepository {
    val activeSession: StateFlow<FocusSession?>

    /** Default length for sessions started from now on; changing it never affects a running one. */
    val defaultLengthMinutes: StateFlow<Int>

    /** How long a break runs, on the same terms. */
    val breakLengthMinutes: StateFlow<Int>

    /**
     * The task the session screen is showing when nothing is running — opened from Up next to look
     * at the work before committing to a timer.
     *
     * Here rather than in a view model because the session screen is its own navigation entry with
     * its own view model, so this is the same boundary [activeSession] already crosses. In memory
     * only: unlike a session, an unstarted preview is not worth surviving the process.
     */
    val previewTaskId: StateFlow<Long?>

    fun setActiveSession(session: FocusSession?)

    fun setDefaultLengthMinutes(minutes: Int)

    fun setBreakLengthMinutes(minutes: Int)

    fun setPreviewTaskId(taskId: Long?)
}
