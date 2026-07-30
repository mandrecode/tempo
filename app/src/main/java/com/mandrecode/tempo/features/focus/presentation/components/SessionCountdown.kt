package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration

/**
 * The remaining time of a running session, recomputed once a second.
 *
 * Deliberately its own state holder rather than a field on the screen's UiState: only the session
 * card, the immersive view and the floating-bar chip read it, so a running session never
 * recomposes the agenda list once per second.
 *
 * Derived from the clock rather than counted down, so it stays correct across process death,
 * device sleep and a paused session.
 */
@Composable
internal fun rememberSessionCountdown(
    session: FocusSession?,
    clock: Clock = Clock.System,
): State<Duration> {
    val paused = session?.isPaused ?: true
    return produceState(
        initialValue = session?.remaining(clock.now()) ?: Duration.ZERO,
        session,
        paused,
    ) {
        if (session == null) {
            value = Duration.ZERO
            return@produceState
        }
        // A paused session needs one value, not a ticker.
        if (paused) {
            value = session.remaining(clock.now())
            return@produceState
        }
        while (true) {
            value = session.remaining(clock.now())
            if (value <= Duration.ZERO) break
            delay(TICK_MILLIS)
        }
    }
}

/**
 * `mm:ss` while under an hour, `hh:mm` once over it.
 *
 * A Pomodoro reads as minutes and seconds, but "90:00" does not read as anything — past the hour
 * the useful unit changes, and the seconds stop being what you are watching.
 */
internal fun Duration.asCountdownLabel(): String {
    val totalSeconds = inWholeSeconds.coerceAtLeast(0)
    val hours = totalSeconds / SECONDS_PER_HOUR
    return if (hours > 0) {
        "%02d:%02d".format(hours, (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE)
    } else {
        "%02d:%02d".format(totalSeconds / SECONDS_PER_MINUTE, totalSeconds % SECONDS_PER_MINUTE)
    }
}

private const val TICK_MILLIS = 1_000L
private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 60 * 60

/**
 * The tick that means done, everywhere in Focus.
 *
 * The bare check the task and habit cards already tick work off with — not `ic_check`, which draws
 * its own ring. Focus was the only place in the app where finishing something looked different
 * depending on which surface you finished it from.
 */
@Composable
internal fun doneIcon(): Painter = rememberVectorPainter(Icons.Filled.Check)
