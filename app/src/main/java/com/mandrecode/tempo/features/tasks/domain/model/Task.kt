package com.mandrecode.tempo.features.tasks.domain.model

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.MonthDayOption
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import kotlinx.datetime.LocalDateTime

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val categoryId: Long = 0,
    val priority: Priority? = null,
    val reminderDate: LocalDateTime? = null,
    val periodicity: Periodicity? = null,
    val periodicityInterval: Int = 1,
    val repeatDays: Set<DayOfWeek>? = null,
    val monthDayOption: MonthDayOption? = null,
    val parentTaskId: Long? = null,
    val sortOrder: Int = 0,
    val completedAt: LocalDateTime? = null,
    /**
     * When this task was archived as part of a periodic-completion flow, points to the
     * spawned next-occurrence task. Allows the toggle use case to roll back the spawn
     * (delete next instance, restore recurrence) when the user unchecks the archived task.
     */
    val nextInstanceId: Long? = null,
)

/**
 * Legacy constant for the seeded Inbox category (id = -1).
 * Only used in database seed/migration code. UI code should use
 * [Category.isDefault] instead of identity checks.
 */
@Deprecated("Use Category.isDefault instead of identity checks")
val DEFAULT_INBOX_CATEGORY = Category(id = -1, name = "Inbox", isDefault = true)
val DEFAULT_ADD_CATEGORY = Category(id = -2, name = "Add Category")
