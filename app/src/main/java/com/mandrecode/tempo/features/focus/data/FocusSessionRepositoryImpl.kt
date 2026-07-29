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

        override val activeSession: StateFlow<FocusSession?> = sessionFlow.asStateFlow()

        override val defaultLengthMinutes: StateFlow<Int> = defaultLengthFlow.asStateFlow()

        override fun setActiveSession(session: FocusSession?) {
            prefs.edit {
                if (session == null) {
                    remove(KEY_TASK_ID)
                    remove(KEY_TASK_TITLE)
                    remove(KEY_PLANNED_MILLIS)
                    remove(KEY_COMPLETED_MILLIS)
                    remove(KEY_RUNNING_SINCE)
                } else {
                    putLong(KEY_TASK_ID, session.taskId)
                    putString(KEY_TASK_TITLE, session.taskTitle)
                    putLong(KEY_PLANNED_MILLIS, session.plannedLength.inWholeMilliseconds)
                    putLong(KEY_COMPLETED_MILLIS, session.completedBeforeNow.inWholeMilliseconds)
                    // -1 rather than removing the key, so "paused" is a stored state instead of an
                    // absence that could be confused with a partially written record.
                    putLong(KEY_RUNNING_SINCE, session.runningSince?.toEpochMilliseconds() ?: NOT_RUNNING)
                }
            }
            sessionFlow.value = session
        }

        override fun setDefaultLengthMinutes(minutes: Int) {
            val normalized =
                FocusSession.SUPPORTED_LENGTHS_MINUTES.minBy { candidate ->
                    kotlin.math.abs(candidate - minutes)
                }
            prefs.edit { putInt(KEY_DEFAULT_LENGTH, normalized) }
            defaultLengthFlow.value = normalized
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
                completedBeforeNow = prefs.getLong(KEY_COMPLETED_MILLIS, 0L).milliseconds,
                runningSince =
                    runningSince
                        .takeIf { it != NOT_RUNNING }
                        ?.let { Instant.fromEpochMilliseconds(it) },
            )
        }

        private fun readDefaultLength(): Int {
            val fallback = FocusSession.DEFAULT_LENGTH.inWholeMinutes.toInt()
            return prefs.getInt(KEY_DEFAULT_LENGTH, fallback)
        }

        private companion object {
            const val PREFS_NAME = "focus_session_prefs"
            const val KEY_TASK_ID = "session_task_id"
            const val KEY_TASK_TITLE = "session_task_title"
            const val KEY_PLANNED_MILLIS = "session_planned_millis"
            const val KEY_COMPLETED_MILLIS = "session_completed_millis"
            const val KEY_RUNNING_SINCE = "session_running_since"
            const val KEY_DEFAULT_LENGTH = "session_default_length_minutes"
            const val NOT_RUNNING = -1L
        }
    }
