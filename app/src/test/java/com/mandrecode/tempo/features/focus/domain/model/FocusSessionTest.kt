package com.mandrecode.tempo.features.focus.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class FocusSessionTest {
    private val start = Instant.fromEpochMilliseconds(1_800_000_000_000)

    private fun session(length: Int = 25): FocusSession {
        val title = "Report"
        return FocusSession.start(taskId = 1, taskTitle = title, now = start, length = length.minutes)
    }

    @Test
    fun `a fresh session has its full length remaining`() {
        assertThat(session().remaining(start)).isEqualTo(25.minutes)
    }

    @Test
    fun `remaining time is derived from the clock, not counted down`() {
        assertThat(session().remaining(start + 10.minutes)).isEqualTo(15.minutes)
    }

    @Test
    fun `remaining time never goes negative`() {
        assertThat(session().remaining(start + 40.minutes)).isEqualTo(kotlin.time.Duration.ZERO)
    }

    @Test
    fun `a session expires once its time is up`() {
        val session = session()

        assertThat(session.hasExpired(start + 24.minutes)).isFalse()
        assertThat(session.hasExpired(start + 25.minutes)).isTrue()
    }

    @Test
    fun `pausing banks the elapsed time and stops the clock`() {
        val paused = session().pause(start + 10.minutes)

        assertThat(paused.isPaused).isTrue()
        assertThat(paused.elapsed(start + 30.minutes)).isEqualTo(10.minutes)
        assertThat(paused.remaining(start + 30.minutes)).isEqualTo(15.minutes)
    }

    @Test
    fun `a paused session never expires`() {
        val paused = session().pause(start + 10.minutes)

        assertThat(paused.hasExpired(start + 90.minutes)).isFalse()
    }

    @Test
    fun `resuming continues from where it paused`() {
        val paused = session().pause(start + 10.minutes)
        val resumed = paused.resume(start + 60.minutes)

        assertThat(resumed.isPaused).isFalse()
        assertThat(resumed.remaining(start + 60.minutes)).isEqualTo(15.minutes)
        assertThat(resumed.remaining(start + 65.minutes)).isEqualTo(10.minutes)
    }

    @Test
    fun `pausing twice is a no-op rather than double-banking`() {
        val paused = session().pause(start + 10.minutes)

        assertThat(paused.pause(start + 20.minutes)).isEqualTo(paused)
    }

    @Test
    fun `resuming a running session is a no-op`() {
        val running = session()

        assertThat(running.resume(start + 5.minutes)).isEqualTo(running)
    }

    @Test
    fun `elapsed time is capped at the planned length`() {
        assertThat(session().elapsed(start + 90.minutes)).isEqualTo(25.minutes)
    }

    @Test
    fun `a session stopped inside its first minute banks nothing`() {
        assertThat(session().bankableMinutes(start + 45.seconds)).isEqualTo(0)
    }

    @Test
    fun `only whole minutes are banked`() {
        assertThat(session().bankableMinutes(start + 10.minutes + 59.seconds)).isEqualTo(10)
    }

    @Test
    fun `endsAt is the wall-clock time the alarm and chronometer share`() {
        assertThat(session().endsAt).isEqualTo(start + 25.minutes)
    }

    @Test
    fun `a paused session has no end time to schedule against`() {
        assertThat(session().pause(start + 5.minutes).endsAt).isNull()
    }

    @Test
    fun `resuming pushes the end time out by the time spent paused`() {
        val resumed = session().pause(start + 10.minutes).resume(start + 60.minutes)

        assertThat(resumed.endsAt).isEqualTo(start + 75.minutes)
    }
}
