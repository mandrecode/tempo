package com.mandrecode.tempo.infrastructure.focus

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mandrecode.tempo.MainActivity
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.focus.domain.scheduler.FocusSessionScheduler
import com.mandrecode.tempo.infrastructure.notifications.NotificationChannelManager
import com.mandrecode.tempo.infrastructure.notifications.RequestCodeGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Drives a focus session with an exact alarm and an ongoing notification — no foreground service.
 *
 * The countdown is rendered by the platform's own chronometer against a fixed end timestamp, so the
 * app posts the notification once per state change rather than once per second, and the displayed
 * time stays correct even while the process is dead.
 */
@Singleton
class AndroidFocusSessionScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : FocusSessionScheduler {
        private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        override fun scheduleSessionEnd(session: FocusSession) {
            val endsAt = session.endsAt ?: return
            val pendingIntent = sessionEndPendingIntent()
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    endsAt.toEpochMilliseconds(),
                    pendingIntent,
                )
            } else {
                // Exact alarms can be denied or revoked. The session still runs and the
                // notification's countdown stays accurate, since both are anchored to the same end
                // timestamp — only the moment the completion sheet appears becomes approximate.
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    endsAt.toEpochMilliseconds(),
                    pendingIntent,
                )
            }
        }

        override fun cancelSessionEnd() {
            alarmManager.cancel(sessionEndPendingIntent())
        }

        override fun showOngoingNotification(session: FocusSession) {
            if (!NotificationChannelManager.canPostNotifications(context)) return
            NotificationChannelManager.ensureFocusSessionChannel(context, notificationManager)

            val builder =
                NotificationCompat
                    .Builder(context, NotificationChannelManager.FOCUS_SESSION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_focus)
                    .setContentTitle(session.taskTitle)
                    .setOngoing(true)
                    // Alerts when it arrives and then stays quiet: the pause and resume updates
                    // that follow are the same notification changing, not news.
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
                    // Promoted the way a habit chain's live activity is, so a running session can be
                    // glanced at from the status bar rather than only found in the shade.
                    .setRequestPromotedOngoing(true)
                    .setContentIntent(openFocusPendingIntent())

            val endsAt = session.endsAt
            if (endsAt != null) {
                // The platform renders the countdown itself from this timestamp; nothing here ticks.
                builder
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(endsAt.toEpochMilliseconds())
                    .setShowWhen(true)
            } else {
                builder.setContentText(context.getString(R.string.focus_session_paused))
            }

            notificationManager.notify(RequestCodeGenerator.forFocusSession(), builder.build())
        }

        override fun clearOngoingNotification() {
            notificationManager.cancel(RequestCodeGenerator.forFocusSession())
        }

        private fun canScheduleExactAlarms(): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

        private fun sessionEndPendingIntent(): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                RequestCodeGenerator.forFocusSession(),
                Intent(context, FocusSessionEndReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun openFocusPendingIntent(): PendingIntent =
            PendingIntent.getActivity(
                context,
                RequestCodeGenerator.forFocusSession(),
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_OPEN_FOCUS, true)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        companion object {
            const val EXTRA_OPEN_FOCUS = "extra_open_focus"
        }
    }
