package com.mandrecode.tempo.core.ui

/**
 * Keeps the launch splash up until the first screen has something to show (#438).
 *
 * Without it the splash gives way as soon as the database is ready, and the first tab spends its
 * first few frames as its bare background before the list arrives on top of it — a visible
 * flash. The gate starts closed and opens for good the first time a screen reports content
 * through `HoldSplashWhileFirstLoad` — a tab once its data has loaded, onboarding straight away,
 * as it has nothing to load. Starting closed matters: the splash checks the app's own state the
 * moment it changes, a frame before the first screen has composed and could report anything, and
 * a gate that started open let the splash go in that gap. The splash's time cap bounds the wait
 * in every case, and releases the gate when it runs out.
 *
 * Read from the splash screen's keep-on-screen condition on the main thread and written from
 * composition, which also runs there.
 */
class StartupContentGate {
    @Volatile
    private var released = false

    /** True until the first screen has shown content (or the splash has gone without it). */
    val isHolding: Boolean get() = !released

    /** Opens the gate for good: the splash has gone, whether or not the first load finished. */
    fun release() {
        released = true
    }

    /** A screen's loading state; the first report of content opens the gate. */
    fun report(isLoading: Boolean) {
        if (!isLoading) released = true
    }
}
