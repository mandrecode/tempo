package com.mandrecode.tempo.core.ui.components

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.mandrecode.tempo.core.ui.StartupContentGate

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

/**
 * True while the first screen's first load is still covered by the launch splash. Read where the
 * loaded content first composes: the screen reports to the gate only after that composition, so
 * the content can tell it is the one the splash is waiting for.
 */
@Composable
fun isSplashCoveringFirstLoad(): Boolean {
    val host = LocalActivity.current as? StartupContentGateHost
    return host?.startupContentGate?.isHolding == true
}
