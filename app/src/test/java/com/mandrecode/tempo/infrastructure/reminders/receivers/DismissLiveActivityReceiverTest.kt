package com.mandrecode.tempo.infrastructure.reminders.receivers

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class DismissLiveActivityReceiverTest {
    @Test
    fun `chainIdFrom returns the chain id carried by the delete intent`() {
        val intent = intentWithChainId(7L)

        assertThat(DismissLiveActivityReceiver.chainIdFrom(intent)).isEqualTo(7L)
    }

    @Test
    fun `chainIdFrom returns null when the intent carries no chain id`() {
        val intent = intentWithChainId(-1L)

        assertThat(DismissLiveActivityReceiver.chainIdFrom(intent)).isNull()
    }

    @Test
    fun `chainIdFrom accepts a zero chain id`() {
        val intent = intentWithChainId(0L)

        assertThat(DismissLiveActivityReceiver.chainIdFrom(intent)).isEqualTo(0L)
    }

    private fun intentWithChainId(chainId: Long): Intent =
        mockk {
            every {
                getLongExtra(DismissLiveActivityReceiver.EXTRA_HABIT_CHAIN_ID, -1L)
            } returns chainId
        }
}
