package com.mandrecode.tempo.infrastructure.liveactivity

import android.app.NotificationManager
import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.infrastructure.notifications.NotificationChannelManager
import com.mandrecode.tempo.infrastructure.notifications.NotificationSyncManager
import com.mandrecode.tempo.infrastructure.notifications.RequestCodeGenerator
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

class HabitChainLiveActivityManagerTest {
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationSyncManager: NotificationSyncManager
    private lateinit var manager: HabitChainLiveActivityManager

    // Fixed time: Sunday, June 15, 2025 12:00
    private val testClock: Clock =
        object : Clock {
            override fun now() =
                LocalDateTime(2025, 6, 15, 12, 0)
                    .toInstant(TimeZone.currentSystemDefault())
        }

    @Before
    fun setup() {
        notificationManager = mockk(relaxed = true)
        notificationSyncManager = mockk(relaxed = true)
        context = mockk(relaxed = true)
        every { context.getSystemService(any<String>()) } returns notificationManager
        mockkObject(NotificationChannelManager)
        every { NotificationChannelManager.ensureLiveActivityChannel(any(), any()) } just Runs
        manager = HabitChainLiveActivityManager(context, notificationSyncManager, testClock)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    // --- In-app completion (fromNotification = false, default) ---

    @Test
    fun `updateLiveActivity cancels live activity notification when chain completed from app`() {
        val chainId = 1L
        val chain = HabitChain(id = chainId, title = "Morning Routine")
        val totalCount = 3

        manager.updateLiveActivity(chain, completedCount = totalCount, totalCount = totalCount)

        verify {
            notificationManager.cancel(RequestCodeGenerator.forLiveActivity(chainId))
        }
    }

    @Test
    fun `updateLiveActivity cancels chain reminder notification when chain completed from app`() {
        val chainId = 1L
        val chain = HabitChain(id = chainId, title = "Morning Routine")
        val totalCount = 3

        manager.updateLiveActivity(chain, completedCount = totalCount, totalCount = totalCount)

        verify {
            notificationSyncManager.dismissHabitChainNotification(chainId)
        }
    }

    @Test
    fun `updateLiveActivity does not post a success notification when chain completed from app`() {
        val chain = HabitChain(id = 1L, title = "Morning Routine")
        val totalCount = 3

        manager.updateLiveActivity(chain, completedCount = totalCount, totalCount = totalCount)

        verify(exactly = 0) { notificationManager.notify(any<Int>(), any()) }
        verify(exactly = 0) { notificationManager.notify(any<String>(), any<Int>(), any()) }
    }

    @Test
    fun `updateLiveActivity does not track chain as active when completed from app`() {
        val chainId = 1L
        val chain = HabitChain(id = chainId, title = "Morning Routine")

        manager.updateLiveActivity(chain, completedCount = 3, totalCount = 3)

        assertThat(manager.hasActiveLiveActivity(chainId)).isFalse()
    }

    // --- Notification-triggered completion (fromNotification = true) ---
    // Note: Full notification posting cannot be verified without Robolectric because
    // NotificationCompat.Builder.build() needs real Android framework internals.
    // Instead, we verify the silent-dismiss path is NOT taken and the notification
    // path IS entered by checking side effects (activeChains tracking, cancel calls).

    @Test
    fun `updateLiveActivity does not silently dismiss live activity when chain completed from notification`() {
        val chainId = 1L
        val chain = HabitChain(id = chainId, title = "Morning Routine")

        // NotificationCompat.Builder.build() crashes without Robolectric, but the
        // important behavioral contract can be verified through side effects alone.
        runCatching {
            manager.updateLiveActivity(
                chain,
                completedCount = 3,
                totalCount = 3,
                fromNotification = true,
            )
        }

        // The live activity is NOT silently cancelled — a success notification is shown instead.
        verify(exactly = 0) {
            notificationManager.cancel(RequestCodeGenerator.forLiveActivity(chainId))
        }
        // The chain reminder notification IS dismissed because the live activity supersedes it.
        verify {
            notificationSyncManager.dismissHabitChainNotification(chainId)
        }
        // The chain was added to activeChains (proving the notification path was entered)
        assertThat(manager.hasActiveLiveActivity(chainId)).isTrue()
    }

    // --- Edge cases ---

    @Test
    fun `updateLiveActivity dismisses chain reminder when live activity is shown`() {
        val chainId = 5L
        val chain = HabitChain(id = chainId, title = "Evening Routine")

        // NotificationCompat.Builder.build() may crash without Robolectric
        runCatching {
            manager.updateLiveActivity(chain, completedCount = 1, totalCount = 3)
        }

        verify {
            notificationSyncManager.dismissHabitChainNotification(chainId)
        }
    }

    @Test
    fun `updateLiveActivity does nothing when totalCount is zero`() {
        val chain = HabitChain(id = 1L, title = "Empty Chain")

        manager.updateLiveActivity(chain, completedCount = 0, totalCount = 0)

        verify(exactly = 0) { notificationManager.notify(any<Int>(), any()) }
        verify(exactly = 0) { notificationManager.cancel(any<Int>()) }
        verify(exactly = 0) { notificationManager.cancel(any<String>(), any<Int>()) }
    }

    // --- All habits unchecked ---

    @Test
    fun `updateLiveActivity dismisses live activity when all unchecked and reminder is in the future`() {
        val chainId = 1L
        val chain =
            HabitChain(
                id = chainId,
                title = "Morning Routine",
                // Reminder is tomorrow — in the future relative to testClock (June 15 12:00)
                periodicReminder = LocalDateTime(2025, 6, 16, 8, 0),
            )

        // Ensure the chain is tracked as active first
        runCatching {
            manager.updateLiveActivity(chain, completedCount = 1, totalCount = 3)
        }
        assertThat(manager.hasActiveLiveActivity(chainId)).isTrue()

        manager.updateLiveActivity(chain, completedCount = 0, totalCount = 3)

        verify { notificationManager.cancel(RequestCodeGenerator.forLiveActivity(chainId)) }
        assertThat(manager.hasActiveLiveActivity(chainId)).isFalse()
    }

    @Test
    fun `updateLiveActivity keeps live activity when all unchecked and reminder is in the past`() {
        val chainId = 2L
        val chain =
            HabitChain(
                id = chainId,
                title = "Morning Routine",
                // Reminder is earlier today — in the past relative to testClock (June 15 12:00)
                periodicReminder = LocalDateTime(2025, 6, 15, 8, 0),
            )

        // NotificationCompat.Builder.build() may crash without Robolectric
        runCatching {
            manager.updateLiveActivity(chain, completedCount = 0, totalCount = 3)
        }

        // Live activity is NOT dismissed — the reminder already fired
        verify(exactly = 0) { notificationManager.cancel(RequestCodeGenerator.forLiveActivity(chainId)) }
        assertThat(manager.hasActiveLiveActivity(chainId)).isTrue()
    }

    @Test
    fun `updateLiveActivity keeps live activity when all unchecked and no reminder set`() {
        val chainId = 3L
        val chain =
            HabitChain(
                id = chainId,
                title = "Manual Chain",
                periodicReminder = null,
            )

        // NotificationCompat.Builder.build() may crash without Robolectric
        runCatching {
            manager.updateLiveActivity(chain, completedCount = 0, totalCount = 3)
        }

        // Live activity is NOT dismissed — no alarm exists, this is the user's only tracker
        verify(exactly = 0) { notificationManager.cancel(RequestCodeGenerator.forLiveActivity(chainId)) }
        assertThat(manager.hasActiveLiveActivity(chainId)).isTrue()
    }

    @Test
    fun `updateLiveActivity keeps live activity when unchecked, reminder in future, fromNotification`() {
        // Regression test for #653: when the user taps "Start chain" from the notification,
        // HabitReminderReceiver has already advanced periodicReminder to the next future
        // occurrence. The live activity must still be posted instead of being silently dismissed.
        val chainId = 4L
        val chain =
            HabitChain(
                id = chainId,
                title = "Morning Routine",
                // Reminder advanced to tomorrow by HabitReminderReceiver.rescheduleHabitChain
                periodicReminder = LocalDateTime(2025, 6, 16, 8, 0),
            )

        // NotificationCompat.Builder.build() may crash without Robolectric
        runCatching {
            manager.updateLiveActivity(
                chain,
                completedCount = 0,
                totalCount = 3,
                fromNotification = true,
            )
        }

        // Live activity is NOT dismissed — the explicit notification action must be honored
        verify(exactly = 0) { notificationManager.cancel(RequestCodeGenerator.forLiveActivity(chainId)) }
        assertThat(manager.hasActiveLiveActivity(chainId)).isTrue()
    }
}
