package com.mandrecode.tempo.features.focus.data

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class FocusSessionRepositoryImplTest {
    private val start = Instant.fromEpochMilliseconds(1_800_000_000_000)

    private lateinit var editor: SharedPreferences.Editor
    private lateinit var prefs: SharedPreferences

    /**
     * Builds the repository over a mock that behaves like a real store for the keys we set, so a
     * write followed by a fresh read exercises the round trip rather than the mock's defaults.
     */
    private fun createRepository(stored: MutableMap<String, Any> = mutableMapOf()): FocusSessionRepositoryImpl {
        editor =
            mockk(relaxed = true) {
                every { putLong(any(), any()) } answers {
                    stored[firstArg()] = secondArg<Long>()
                    this@mockk
                }
                every { putString(any(), any()) } answers {
                    stored[firstArg()] = secondArg<String>()
                    this@mockk
                }
                every { putInt(any(), any()) } answers {
                    stored[firstArg()] = secondArg<Int>()
                    this@mockk
                }
                every { putBoolean(any(), any()) } answers {
                    stored[firstArg()] = secondArg<Boolean>()
                    this@mockk
                }
                every { remove(any()) } answers {
                    stored.remove(firstArg())
                    this@mockk
                }
                every { apply() } just Runs
            }
        prefs =
            mockk {
                every { edit() } returns editor
                every { contains(any()) } answers { stored.containsKey(firstArg()) }
                every { getLong(any(), any()) } answers { stored[firstArg()] as? Long ?: secondArg() }
                every { getString(any(), any()) } answers { stored[firstArg()] as? String ?: secondArg() }
                every { getInt(any(), any()) } answers { stored[firstArg()] as? Int ?: secondArg() }
                every { getBoolean(any(), any()) } answers { stored[firstArg()] as? Boolean ?: secondArg() }
            }
        val context = mockk<Context> { every { getSharedPreferences(any(), any()) } returns prefs }
        return FocusSessionRepositoryImpl(context)
    }

    @Test
    fun `no session is stored initially`() {
        assertThat(createRepository().activeSession.value).isNull()
    }

    @Test
    fun `a running session round-trips through storage`() {
        val stored = mutableMapOf<String, Any>()
        val session =
            FocusSession.start(taskId = 7, taskTitle = "Report", now = start, length = 30.minutes)

        createRepository(stored).setActiveSession(session)

        assertThat(createRepository(stored).activeSession.value).isEqualTo(session)
    }

    @Test
    fun `a paused session round-trips with its banked time`() {
        val stored = mutableMapOf<String, Any>()
        val paused =
            FocusSession
                .start(taskId = 7, taskTitle = "Report", now = start)
                .pause(start + 8.minutes)

        createRepository(stored).setActiveSession(paused)
        val restored = createRepository(stored).activeSession.value

        assertThat(restored?.isPaused).isTrue()
        assertThat(restored?.completedBeforeNow).isEqualTo(8.minutes)
    }

    @Test
    fun `clearing the session removes every key rather than leaving a partial record`() {
        val stored = mutableMapOf<String, Any>()
        val repository = createRepository(stored)
        repository.setActiveSession(FocusSession.start(1, "Report", start))

        repository.setActiveSession(null)

        assertThat(repository.activeSession.value).isNull()
        assertThat(createRepository(stored).activeSession.value).isNull()
    }

    @Test
    fun `a record with no planned length is treated as absent`() {
        // Models a partially written or hand-edited store.
        val stored = mutableMapOf<String, Any>("session_task_id" to 5L)

        assertThat(createRepository(stored).activeSession.value).isNull()
    }

    @Test
    fun `the default length falls back to the standard session`() {
        assertThat(createRepository().defaultLengthMinutes.value)
            .isEqualTo(FocusSession.DEFAULT_LENGTH.inWholeMinutes.toInt())
    }

    @Test
    fun `setting a supported length stores it verbatim`() {
        val repository = createRepository()

        repository.setDefaultLengthMinutes(45)

        assertThat(repository.defaultLengthMinutes.value).isEqualTo(45)
        verify { editor.putInt("session_default_length_minutes", 45) }
    }

    @Test
    fun `a length outside the range is clamped to it`() {
        val repository = createRepository()

        repository.setDefaultLengthMinutes(FocusSession.SESSION_LENGTH_RANGE.last + 30)
        assertThat(repository.defaultLengthMinutes.value)
            .isEqualTo(FocusSession.SESSION_LENGTH_RANGE.last)

        repository.setDefaultLengthMinutes(0)
        assertThat(repository.defaultLengthMinutes.value)
            .isEqualTo(FocusSession.SESSION_LENGTH_RANGE.first)
    }

    @Test
    fun `the break length is its own setting`() {
        val repository = createRepository()

        repository.setBreakLengthMinutes(15)

        assertThat(repository.breakLengthMinutes.value).isEqualTo(15)
        // Changing one never moves the other.
        assertThat(repository.defaultLengthMinutes.value)
            .isEqualTo(FocusSession.DEFAULT_LENGTH.inWholeMinutes.toInt())
    }

    @Test
    fun `a break round-trips as a break`() {
        val stored = mutableMapOf<String, Any>()
        val brk =
            FocusSession.start(taskId = 7, taskTitle = "Report", now = start, isBreak = true)

        createRepository(stored).setActiveSession(brk)

        assertThat(createRepository(stored).activeSession.value?.isBreak).isTrue()
    }

    @Test
    fun `the stored default length is read back`() {
        val stored = mutableMapOf<String, Any>("session_default_length_minutes" to 15)

        assertThat(createRepository(stored).defaultLengthMinutes.value).isEqualTo(15)
    }
}
