package com.mandrecode.tempo.core.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StartupContentGateTest {
    @Test
    fun `nothing holds the splash until a screen reports`() {
        assertThat(StartupContentGate().isHolding).isFalse()
    }

    @Test
    fun `a first screen still loading holds the splash until it has content`() {
        val gate = StartupContentGate()

        gate.report(isLoading = true)
        assertThat(gate.isHolding).isTrue()

        gate.report(isLoading = false)
        assertThat(gate.isHolding).isFalse()
    }

    /** Opening another tab later loads too, but the app is long past its splash by then. */
    @Test
    fun `once content has been shown, later loads never hold the splash again`() {
        val gate = StartupContentGate()
        gate.report(isLoading = false)

        gate.report(isLoading = true)

        assertThat(gate.isHolding).isFalse()
    }

    /** The splash's time cap ran out before the first content: the gate opens with it. */
    @Test
    fun `releasing while the first load is still running opens the gate for good`() {
        val gate = StartupContentGate()
        gate.report(isLoading = true)

        gate.release()
        gate.report(isLoading = true)

        assertThat(gate.isHolding).isFalse()
    }
}
