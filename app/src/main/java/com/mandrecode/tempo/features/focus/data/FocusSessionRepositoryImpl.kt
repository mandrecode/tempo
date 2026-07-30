package com.mandrecode.tempo.features.focus.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
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
        private val sessionsTodayFlow = MutableStateFlow(readSessionsToday())

        override val activeSession: StateFlow<FocusSession?> = sessionFlow.asStateFlow()

        override val defaultLengthMinutes: StateFlow<Int> = defaultLengthFlow.asStateFlow()

        override val breakLengthMinutes: StateFlow<Int> = breakLengthFlow.asStateFlow()

        override val previewTaskId: StateFlow<Long?> = previewTaskIdFlow.asStateFlow()

        override val sessionsToday: StateFlow<Map<Long, Int>> = sessionsTodayFlow.asStateFlow()

        override fun recordSessionFor(
            taskId: Long,
            today: LocalDate,
        ) {
            // Stamped with the date it belongs to, so yesterday's runs never colour today's card.
            val current =
                if (prefs.getString(KEY_SESSIONS_DATE, null) == today.toString()) {
                    sessionsTodayFlow.value
                } else {
                    emptyMap()
                }
            val updated = current + (taskId to (current[taskId] ?: 0) + 1)
            prefs.edit {
                putString(KEY_SESSIONS_DATE, today.toString())
                putString(
                    KEY_SESSIONS_BY_TASK,
                    updated.entries.joinToString(",") { (taskId, runs) -> "$taskId:$runs" },
                )
            }
            sessionsTodayFlow.value = updated
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

        /** A flat `id:count,id:count` list — small, and never queried by anything but this class. */
        private fun readSessionsToday(): Map<Long, Int> =
            prefs
                .getString(KEY_SESSIONS_BY_TASK, null)
                .orEmpty()
                .split(',')
                .mapNotNull { pair ->
                    val (id, count) = pair.split(':').takeIf { it.size == 2 } ?: return@mapNotNull null
                    val taskId = id.toLongOrNull() ?: return@mapNotNull null
                    val runs = count.toIntOrNull() ?: return@mapNotNull null
                    taskId to runs
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
