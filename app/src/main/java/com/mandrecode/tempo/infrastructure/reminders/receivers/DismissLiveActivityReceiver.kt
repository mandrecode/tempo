package com.mandrecode.tempo.infrastructure.reminders.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mandrecode.tempo.infrastructure.liveactivity.HabitChainLiveActivityManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fired by the live activity notification's delete intent when the user removes it from the
 * shade. This is the only signal that distinguishes a deliberate dismissal from the notification
 * being cleared by a reboot or force-stop, so recovery knows not to rebuild it.
 */
@AndroidEntryPoint
class DismissLiveActivityReceiver : BroadcastReceiver() {
    @Inject
    lateinit var liveActivityManager: HabitChainLiveActivityManager

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        chainIdFrom(intent)?.let(liveActivityManager::dismissLiveActivity)
    }

    companion object {
        const val EXTRA_HABIT_CHAIN_ID = "HABIT_CHAIN_ID"

        /** The chain whose live activity was dismissed, or null when the intent carries no valid id. */
        fun chainIdFrom(intent: Intent): Long? =
            intent.getLongExtra(EXTRA_HABIT_CHAIN_ID, MISSING_CHAIN_ID).takeIf { it != MISSING_CHAIN_ID }

        private const val MISSING_CHAIN_ID = -1L
    }
}
