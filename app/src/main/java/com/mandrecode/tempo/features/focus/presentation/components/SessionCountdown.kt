package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
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

/** `mm:ss`, which is what a Pomodoro reads as even when it runs over an hour. */
internal fun Duration.asCountdownLabel(): String {
    val totalSeconds = inWholeSeconds.coerceAtLeast(0)
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "%02d:%02d".format(minutes, seconds)
}

private const val TICK_MILLIS = 1_000L
private const val SECONDS_PER_MINUTE = 60
