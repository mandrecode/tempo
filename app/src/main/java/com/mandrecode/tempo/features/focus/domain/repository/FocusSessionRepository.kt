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

    fun setActiveSession(session: FocusSession?)

    fun setDefaultLengthMinutes(minutes: Int)
}
