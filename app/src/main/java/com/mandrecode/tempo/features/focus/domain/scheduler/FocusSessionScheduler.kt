package com.mandrecode.tempo.features.focus.domain.scheduler

import com.mandrecode.tempo.features.focus.domain.model.FocusSession

/**
 * Platform hooks for a running session: the alarm that fires when time is up, and the ongoing
 * notification that shows the countdown.
 *
 * Deliberately not a foreground service. The app has never declared one, and since Android 14 every
 * foreground service needs a declared type — none of which describes a timer, leaving only
 * `specialUse` and the Play Console justification it drags along. An exact alarm plus a countdown
 * chronometer gets the same behaviour from machinery the app already ships for reminders.
 */
interface FocusSessionScheduler {
    fun scheduleSessionEnd(session: FocusSession)

    fun cancelSessionEnd()

    fun showOngoingNotification(session: FocusSession)

    fun clearOngoingNotification()
}
