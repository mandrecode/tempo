package com.mandrecode.tempo.infrastructure.focus

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mandrecode.tempo.MainActivity
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.di.IoDispatcher
import com.mandrecode.tempo.features.focus.domain.usecase.FocusSessionUseCases
import com.mandrecode.tempo.infrastructure.notifications.NotificationChannelManager
import com.mandrecode.tempo.infrastructure.notifications.RequestCodeGenerator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fires when a session's time is up: banks the elapsed minutes and replaces the ongoing countdown
 * with a one-shot "time's up" notification, so the user is told even if the app is not foregrounded.
 *
 * In-app, the completion sheet is what the user actually sees; this notification is the fallback
 * for a session that ran out while they were elsewhere.
 */
@AndroidEntryPoint
class FocusSessionEndReceiver : BroadcastReceiver() {
    @Inject
    lateinit var focusSessionUseCases: FocusSessionUseCases

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                val finished = focusSessionUseCases.end() ?: return@launch
                notifyTimeIsUp(context, finished.taskTitle, finished.plannedLength.inWholeMinutes.toInt())
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun notifyTimeIsUp(
        context: Context,
        taskTitle: String,
        minutes: Int,
    ) {
        if (!NotificationChannelManager.canPostNotifications(context)) return
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationChannelManager.ensureFocusSessionChannel(context, notificationManager)

        val openFocus =
            android.app.PendingIntent.getActivity(
                context,
                RequestCodeGenerator.forFocusSession(),
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(AndroidFocusSessionScheduler.EXTRA_OPEN_FOCUS, true)
                },
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(context, NotificationChannelManager.FOCUS_SESSION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_focus)
                .setContentTitle(context.getString(R.string.focus_session_finished_title, minutes))
                .setContentText(taskTitle)
                .setAutoCancel(true)
                .setContentIntent(openFocus)
                .build()

        notificationManager.notify(RequestCodeGenerator.forFocusSession(), notification)
    }
}
