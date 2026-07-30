package com.mandrecode.tempo.features.focus.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.focus.domain.model.TaskFocusToday
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@Singleton
class FocusSessionRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : FocusSessionRepository {
        private val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        private val sessionFlow = MutableStateFlow(readSession())
        private val defaultLengthFlow = MutableStateFlow(readDefaultLength())
        private val breakLengthFlow = MutableStateFlow(readBreakLength())
        private val previewTaskIdFlow = MutableStateFlow<Long?>(null)
        private val focusTodayFlow = MutableStateFlow(readFocusToday())

        override val activeSession: StateFlow<FocusSession?> = sessionFlow.asStateFlow()

        override val defaultLengthMinutes: StateFlow<Int> = defaultLengthFlow.asStateFlow()

        override val breakLengthMinutes: StateFlow<Int> = breakLengthFlow.asStateFlow()

        override val previewTaskId: StateFlow<Long?> = previewTaskIdFlow.asStateFlow()

        override val focusToday: StateFlow<Map<Long, TaskFocusToday>> = focusTodayFlow.asStateFlow()

        override fun recordSessionFor(
            taskId: Long,
            today: LocalDate,
        ) = update(taskId, today) { it.copy(sessions = it.sessions + 1) }

        override fun addFocusMinutesFor(
            taskId: Long,
            minutes: Int,
            today: LocalDate,
        ) {
            if (minutes <= 0) return
            update(taskId, today) { it.copy(minutes = it.minutes + minutes) }
        }

        /**
         * One writer for both numbers, so they can never fall on opposite sides of midnight: the
         * whole record is stamped with the date it belongs to and starts over when that date moves,
         * rather than yesterday's runs colouring today's card.
         */
        private fun update(
            taskId: Long,
            today: LocalDate,
            change: (TaskFocusToday) -> TaskFocusToday,
        ) {
            val current =
                if (prefs.getString(KEY_SESSIONS_DATE, null) == today.toString()) {
                    focusTodayFlow.value
                } else {
                    emptyMap()
                }
            val updated =
                current + (taskId to change(current[taskId] ?: TaskFocusToday()))
            prefs.edit {
                putString(KEY_SESSIONS_DATE, today.toString())
                putString(KEY_SESSIONS_BY_TASK, updated.serialize())
            }
            focusTodayFlow.value = updated
        }

        override fun setActiveSession(session: FocusSession?) {
            prefs.edit {
                if (session == null) {
                    remove(KEY_TASK_ID)
                    remove(KEY_TASK_TITLE)
                    remove(KEY_PLANNED_MILLIS)
                    remove(KEY_COMPLETED_MILLIS)
                    remove(KEY_RUNNING_SINCE)
                    remove(KEY_IS_BREAK)
                } else {
                    putLong(KEY_TASK_ID, session.taskId)
                    putString(KEY_TASK_TITLE, session.taskTitle)
                    putLong(KEY_PLANNED_MILLIS, session.plannedLength.inWholeMilliseconds)
                    putLong(KEY_COMPLETED_MILLIS, session.completedBeforeNow.inWholeMilliseconds)
                    // -1 rather than removing the key, so "paused" is a stored state instead of an
                    // absence that could be confused with a partially written record.
                    putLong(KEY_RUNNING_SINCE, session.runningSince?.toEpochMilliseconds() ?: NOT_RUNNING)
                    putBoolean(KEY_IS_BREAK, session.isBreak)
                }
            }
            sessionFlow.value = session
        }

        override fun setPreviewTaskId(taskId: Long?) {
            previewTaskIdFlow.value = taskId
        }

        override fun setDefaultLengthMinutes(minutes: Int) {
            val normalized = minutes.coerceIn(FocusSession.SESSION_LENGTH_RANGE)
            prefs.edit { putInt(KEY_DEFAULT_LENGTH, normalized) }
            defaultLengthFlow.value = normalized
        }

        override fun setBreakLengthMinutes(minutes: Int) {
            val normalized = minutes.coerceIn(FocusSession.BREAK_LENGTH_RANGE)
            prefs.edit { putInt(KEY_BREAK_LENGTH, normalized) }
            breakLengthFlow.value = normalized
        }

        private fun readSession(): FocusSession? {
            val plannedMillis =
                if (prefs.contains(KEY_TASK_ID)) prefs.getLong(KEY_PLANNED_MILLIS, 0L) else 0L
            if (plannedMillis <= 0L) return null

            val runningSince = prefs.getLong(KEY_RUNNING_SINCE, NOT_RUNNING)
            return FocusSession(
                taskId = prefs.getLong(KEY_TASK_ID, 0L),
                taskTitle = prefs.getString(KEY_TASK_TITLE, null).orEmpty(),
                plannedLength = plannedMillis.milliseconds,
                isBreak = prefs.getBoolean(KEY_IS_BREAK, false),
                completedBeforeNow = prefs.getLong(KEY_COMPLETED_MILLIS, 0L).milliseconds,
                runningSince =
                    runningSince
                        .takeIf { it != NOT_RUNNING }
                        ?.let { Instant.fromEpochMilliseconds(it) },
            )
        }

        /**
         * A flat `id:sessions:minutes` list — small, and never queried by anything but this class.
         *
         * The two-field `id:sessions` records written before minutes were tracked still parse, as
         * a day with runs behind it and no time recorded; the next write brings them up to date.
         */
        private fun readFocusToday(): Map<Long, TaskFocusToday> =
            prefs
                .getString(KEY_SESSIONS_BY_TASK, null)
                .orEmpty()
                .split(',')
                .mapNotNull { record ->
                    val parts = record.split(':').takeIf { it.size in 2..3 } ?: return@mapNotNull null
                    val taskId = parts[0].toLongOrNull() ?: return@mapNotNull null
                    val sessions = parts[1].toIntOrNull() ?: return@mapNotNull null
                    val minutes = parts.getOrNull(2)?.toIntOrNull() ?: 0
                    taskId to TaskFocusToday(sessions = sessions, minutes = minutes)
                }.toMap()

        private fun readDefaultLength(): Int {
            val fallback = FocusSession.DEFAULT_LENGTH.inWholeMinutes.toInt()
            return prefs.getInt(KEY_DEFAULT_LENGTH, fallback).coerceIn(FocusSession.SESSION_LENGTH_RANGE)
        }

        private fun readBreakLength(): Int {
            val fallback = FocusSession.DEFAULT_BREAK_LENGTH.inWholeMinutes.toInt()
            return prefs.getInt(KEY_BREAK_LENGTH, fallback).coerceIn(FocusSession.BREAK_LENGTH_RANGE)
        }

        private companion object {
            const val PREFS_NAME = "focus_session_prefs"
            const val KEY_TASK_ID = "session_task_id"
            const val KEY_TASK_TITLE = "session_task_title"
            const val KEY_PLANNED_MILLIS = "session_planned_millis"
            const val KEY_COMPLETED_MILLIS = "session_completed_millis"
            const val KEY_RUNNING_SINCE = "session_running_since"
            const val KEY_IS_BREAK = "session_is_break"
            const val KEY_DEFAULT_LENGTH = "session_default_length_minutes"
            const val KEY_BREAK_LENGTH = "session_break_length_minutes"
            const val KEY_SESSIONS_DATE = "sessions_today_date"
            const val KEY_SESSIONS_BY_TASK = "sessions_today_by_task"
            const val NOT_RUNNING = -1L
        }
    }

/** The stored form of a day: `id:sessions:minutes`, one record per task. */
private fun Map<Long, TaskFocusToday>.serialize(): String =
    entries.joinToString(",") { (taskId, day) -> "$taskId:${day.sessions}:${day.minutes}" }
