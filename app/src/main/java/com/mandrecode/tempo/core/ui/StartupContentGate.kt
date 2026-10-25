package com.mandrecode.tempo.core.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

/**
 * Keeps the launch splash up until the first screen has something to show (#438).
 *
 * Without it the splash gives way as soon as the database is ready, and the first tab spends its
 * first few frames as its bare background before the list arrives on top of it — a visible
 * flash. The first screen that loads data reports through [HoldSplashWhileFirstLoad]: while it is
 * still loading the gate holds, and the moment it has content the gate opens for good. Screens
 * that never load (onboarding) never hold it.
 *
 * Read from the splash screen's keep-on-screen condition on the main thread and written from
 * composition, which also runs there.
 */
class StartupContentGate {
    private enum class State { WAITING, HOLDING, RELEASED }

    @Volatile
    private var state = State.WAITING

    /** True while the first screen is still loading its first content. */
    val isHolding: Boolean get() = state == State.HOLDING

    internal fun report(isLoading: Boolean) {
        state =
            when {
                state == State.RELEASED -> State.RELEASED
                isLoading -> State.HOLDING
                else -> State.RELEASED
            }
    }
}

/** Implemented by the activity that owns the launch splash. */
interface StartupContentGateHost {
    val startupContentGate: StartupContentGate
}

/**
 * Reports a screen's loading state to the launch splash. Only the first report that ends in
 * content matters; after that the gate stays open, so later tabs never touch the splash.
 */
@Composable
fun HoldSplashWhileFirstLoad(isLoading: Boolean) {
    // Outside the main activity (previews, tests) there is no splash to hold.
    val gate = (LocalActivity.current as? StartupContentGateHost)?.startupContentGate ?: return
    SideEffect { gate.report(isLoading) }
}
