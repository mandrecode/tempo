package com.mandrecode.tempo.features.focus.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import com.mandrecode.tempo.features.focus.domain.scheduler.FocusSessionScheduler
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class FocusSessionUseCasesTest {
    private val start = Instant.fromEpochMilliseconds(1_800_000_000_000)
    private var now = start

    private val sessionFlow = MutableStateFlow<FocusSession?>(null)
    private val lengthFlow = MutableStateFlow(25)

    private val sessionRepository =
        mockk<FocusSessionRepository>(relaxed = true) {
            every { activeSession } returns sessionFlow
            every { defaultLengthMinutes } returns lengthFlow
            every { setActiveSession(any()) } answers { sessionFlow.value = firstArg() }
        }
    private val activityRepository = mockk<DailyFocusActivityRepository>(relaxed = true)
    private val scheduler = mockk<FocusSessionScheduler>(relaxed = true)

    private val useCases =
        FocusSessionUseCases(
            sessionRepository = sessionRepository,
            activityRepository = activityRepository,
            scheduler = scheduler,
            clock =
                object : Clock {
                    override fun now(): Instant = now
                },
        )

    @Test
    fun `starting schedules the alarm and posts the notification`() =
        runTest {
            useCases.start(taskId = 1, taskTitle = "Report")

            assertThat(sessionFlow.value?.taskId).isEqualTo(1)
            verify { scheduler.scheduleSessionEnd(any()) }
            verify { scheduler.showOngoingNotification(any()) }
        }

    @Test
    fun `starting uses the configured default length`() =
        runTest {
            lengthFlow.value = 45

            useCases.start(taskId = 1, taskTitle = "Report")

            assertThat(sessionFlow.value?.plannedLength).isEqualTo(45.minutes)
        }

    @Test
    fun `starting on a second task replaces the first and banks its minutes`() =
        runTest {
            useCases.start(taskId = 1, taskTitle = "Report")
            now = start + 10.minutes

            useCases.start(taskId = 2, taskTitle = "Emails")

            assertThat(sessionFlow.value?.taskId).isEqualTo(2)
            coVerify { activityRepository.addFocusMinutes(any<LocalDate>(), 10) }
        }

    @Test
    fun `there is only ever one session`() =
        runTest {
            useCases.start(taskId = 1, taskTitle = "Report")
            useCases.start(taskId = 2, taskTitle = "Emails")

            assertThat(sessionFlow.value?.taskId).isEqualTo(2)
        }

    @Test
    fun `pausing cancels the alarm, since it is tied to a wall-clock end`() =
        runTest {
            useCases.start(taskId = 1, taskTitle = "Report")
            now = start + 5.minutes

            useCases.pause()

            assertThat(sessionFlow.value?.isPaused).isTrue()
            verify { scheduler.cancelSessionEnd() }
        }

    @Test
    fun `resuming rearms the alarm`() =
        runTest {
            useCases.start(taskId = 1, taskTitle = "Report")
            now = start + 5.minutes
            useCases.pause()
            now = start + 30.minutes

            useCases.resume()

            assertThat(sessionFlow.value?.isPaused).isFalse()
            verify(atLeast = 2) { scheduler.scheduleSessionEnd(any()) }
        }

    @Test
    fun `ending banks whole elapsed minutes and clears everything`() =
        runTest {
            useCases.start(taskId = 1, taskTitle = "Report")
            now = start + 12.minutes

            val ended = useCases.end()

            assertThat(ended?.taskId).isEqualTo(1)
            assertThat(sessionFlow.value).isNull()
            coVerify { activityRepository.addFocusMinutes(any<LocalDate>(), 12) }
            verify { scheduler.cancelSessionEnd() }
            verify { scheduler.clearOngoingNotification() }
        }

    @Test
    fun `ending inside the first minute banks nothing`() =
        runTest {
            useCases.start(taskId = 1, taskTitle = "Report")

            useCases.end()

            coVerify(exactly = 0) { activityRepository.addFocusMinutes(any<LocalDate>(), any()) }
        }

    @Test
    fun `ending when nothing runs is a no-op`() =
        runTest {
            assertThat(useCases.end()).isNull()
        }

    @Test
    fun `reconcile completes a session whose time passed while the app was gone`() =
        runTest {
            useCases.start(taskId = 1, taskTitle = "Report")
            now = start + 40.minutes

            val settled = useCases.reconcile()

            assertThat(settled?.taskId).isEqualTo(1)
            assertThat(sessionFlow.value).isNull()
            coVerify { activityRepository.addFocusMinutes(any<LocalDate>(), 25) }
        }

    @Test
    fun `reconcile restores a session that still has time left`() =
        runTest {
            useCases.start(taskId = 1, taskTitle = "Report")
            now = start + 5.minutes

            val settled = useCases.reconcile()

            assertThat(settled).isNull()
            assertThat(sessionFlow.value?.taskId).isEqualTo(1)
            verify(atLeast = 2) { scheduler.scheduleSessionEnd(any()) }
        }

    @Test
    fun `reconcile leaves a paused session paused rather than rearming it`() =
        runTest {
            useCases.start(taskId = 1, taskTitle = "Report")
            now = start + 5.minutes
            useCases.pause()
            now = start + 300.minutes

            val settled = useCases.reconcile()

            assertThat(settled).isNull()
            assertThat(sessionFlow.value?.isPaused).isTrue()
        }

    @Test
    fun `reconcile with no session does nothing`() =
        runTest {
            assertThat(useCases.reconcile()).isNull()
        }
}
