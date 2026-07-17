package com.mandrecode.tempo.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Returns [current] while [isLive] is true, and the last value observed while [isLive] was true
 * once it flips to false — for keeping an exit animation's content stable instead of recomposing
 * into whatever [current] has already been reset to.
 *
 * The frozen snapshot is held in a plain (non-Compose-State) box, so updating it while [isLive] is
 * true neither writes Compose state during composition nor schedules an extra recomposition on
 * every [current] change — only reading it after [isLive] flips false matters, and that read
 * already happens on the recomposition [isLive] itself triggers.
 */
@Composable
fun <T> rememberFrozenWhileHidden(
    current: T,
    isLive: Boolean,
): T {
    val lastLive = remember { MutableBox(current) }
    if (isLive) {
        lastLive.value = current
    }
    return if (isLive) current else lastLive.value
}

private class MutableBox<T>(
    var value: T,
)
