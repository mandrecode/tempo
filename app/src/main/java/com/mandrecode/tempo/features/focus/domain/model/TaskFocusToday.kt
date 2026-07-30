package com.mandrecode.tempo.features.focus.domain.model

/**
 * What a single task has had out of today: sessions that went the distance, and minutes worked.
 *
 * Two numbers rather than one, because they answer different questions and can disagree. A session
 * the user stopped early banks its minutes but is not a session done, so a task can show real time
 * spent and no completed runs — which is exactly the case a count alone would report as untouched.
 */
data class TaskFocusToday(
    val sessions: Int = 0,
    val minutes: Int = 0,
) {
    val hasHistory: Boolean get() = sessions > 0 || minutes > 0
}
