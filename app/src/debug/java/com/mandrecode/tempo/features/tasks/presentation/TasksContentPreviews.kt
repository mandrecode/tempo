package com.mandrecode.tempo.features.tasks.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.preview.PreviewFormFactors
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.model.ActiveGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.CompletedGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

// Fixed dates for deterministic, reproducible previews
private val PREVIEW_TODAY = LocalDate(2025, 6, 15)
private val PREVIEW_YESTERDAY = LocalDate(2025, 6, 14)

private fun Map<CompletedGroupKey, List<Task>>.toCompletedPreviewMap() =
    entries
        .associate { (key, tasks) -> key to tasks.toPersistentList() }
        .toPersistentMap()

private fun Map<ActiveGroupKey, List<Task>>.toActivePreviewMap() =
    entries
        .associate { (key, tasks) -> key to tasks.toPersistentList() }
        .toPersistentMap()

// region CompletedGroupHeader Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedGroupHeaderDatePreview() {
    TempoTheme {
        CompletedGroupHeader(label = "Today")
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedGroupHeaderPriorityPreview() {
    TempoTheme {
        CompletedGroupHeader(label = "High")
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedGroupHeaderNullPreview() {
    TempoTheme {
        CompletedGroupHeader(label = null)
    }
}

// endregion

// region ActiveGroupHeader Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActiveGroupHeaderDatePreview() {
    TempoTheme {
        ActiveGroupHeader(label = "Today", isFirst = true)
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActiveGroupHeaderPriorityPreview() {
    TempoTheme {
        ActiveGroupHeader(label = "High", isFirst = false)
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActiveGroupHeaderNullPreview() {
    TempoTheme {
        ActiveGroupHeader(label = null, isFirst = false)
    }
}

// endregion

// region CompletedTasksSeparator Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedTasksSeparatorCollapsedPreview() {
    TempoTheme {
        CompletedTasksSeparator(isExpanded = false, onToggle = {})
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedTasksSeparatorExpandedWithDividerPreview() {
    TempoTheme {
        CompletedTasksSeparator(
            isExpanded = true,
            onToggle = {},
            showDivider = true,
            firstGroupLabel = "Today",
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedTasksSeparatorExpandedNoDividerPreview() {
    TempoTheme {
        CompletedTasksSeparator(
            isExpanded = true,
            onToggle = {},
            showDivider = false,
        )
    }
}

// endregion

// region CompletedTasksSeparator – ByTitle Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedTasksSeparatorByTitlePreview() {
    TempoTheme {
        CompletedTasksSeparator(
            isExpanded = true,
            onToggle = {},
            showDivider = true,
            firstGroupLabel = "A → Z",
        )
    }
}

// endregion

// region Full Content Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedTasksMultiDayPreview() {
    val threeDaysAgo = LocalDate(2025, 6, 12)

    fun taskAt(
        id: Long,
        title: String,
        date: LocalDate?,
    ) = Task(
        id = id,
        title = title,
        description = "",
        isCompleted = true,
        completedAt = date?.let { LocalDateTime(it.year, it.monthNumber, it.dayOfMonth, 12, 0) },
    )

    val completedTaskGroups: Map<CompletedGroupKey, List<Task>> =
        linkedMapOf(
            CompletedGroupKey.ByDate(PREVIEW_TODAY) to
                listOf(
                    taskAt(1, "Buy groceries", PREVIEW_TODAY),
                    taskAt(2, "Reply to emails", PREVIEW_TODAY),
                ),
            CompletedGroupKey.ByDate(PREVIEW_YESTERDAY) to
                listOf(
                    taskAt(3, "Read chapter 5", PREVIEW_YESTERDAY),
                ),
            CompletedGroupKey.ByDate(threeDaysAgo) to
                listOf(
                    taskAt(4, "Fix login bug", threeDaysAgo),
                ),
            CompletedGroupKey.ByDate(null) to
                listOf(
                    taskAt(5, "Old migrated task", null),
                ),
        )

    TempoTheme {
        TasksContent(
            uiState =
                TasksContract.UiState(
                    isLoading = false,
                    showCompletedTasks = true,
                    sortOption = SortOption.BY_DATE,
                    completedTaskGroups = completedTaskGroups.toCompletedPreviewMap(),
                ),
            onEvent = {},
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedTasksByPriorityPreview() {
    fun taskWithPriority(
        id: Long,
        title: String,
        priority: Priority?,
    ) = Task(
        id = id,
        title = title,
        description = "",
        isCompleted = true,
        priority = priority,
    )

    val completedTaskGroups: Map<CompletedGroupKey, List<Task>> =
        linkedMapOf(
            CompletedGroupKey.ByPriority(Priority.HIGH) to
                listOf(
                    taskWithPriority(1, "Fix critical bug", Priority.HIGH),
                ),
            CompletedGroupKey.ByPriority(Priority.MEDIUM) to
                listOf(
                    taskWithPriority(2, "Update docs", Priority.MEDIUM),
                    taskWithPriority(3, "Review PR", Priority.MEDIUM),
                ),
            CompletedGroupKey.ByPriority(null) to
                listOf(
                    taskWithPriority(4, "Misc task", null),
                ),
        )

    TempoTheme {
        TasksContent(
            uiState =
                TasksContract.UiState(
                    isLoading = false,
                    showCompletedTasks = true,
                    sortOption = SortOption.BY_PRIORITY,
                    completedTaskGroups = completedTaskGroups.toCompletedPreviewMap(),
                ),
            onEvent = {},
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedTasksByTitlePreview() {
    val completedTaskGroups: Map<CompletedGroupKey, List<Task>> =
        mapOf(
            CompletedGroupKey.ByTitle to
                listOf(
                    Task(id = 1, title = "Apple picking", description = "", isCompleted = true),
                    Task(id = 2, title = "Buy groceries", description = "", isCompleted = true),
                    Task(id = 3, title = "Clean kitchen", description = "", isCompleted = true),
                ),
        )

    TempoTheme {
        TasksContent(
            uiState =
                TasksContract.UiState(
                    isLoading = false,
                    showCompletedTasks = true,
                    sortOption = SortOption.BY_TITLE,
                    completedTaskGroups = completedTaskGroups.toCompletedPreviewMap(),
                ),
            onEvent = {},
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedTasksFlatPreview() {
    val completedTaskGroups: Map<CompletedGroupKey, List<Task>> =
        mapOf(
            CompletedGroupKey.Flat to
                listOf(
                    Task(id = 1, title = "Task A", description = "", isCompleted = true),
                    Task(id = 2, title = "Task B", description = "", isCompleted = true),
                ),
        )

    TempoTheme {
        TasksContent(
            uiState =
                TasksContract.UiState(
                    isLoading = false,
                    showCompletedTasks = true,
                    sortOption = SortOption.MANUAL,
                    completedTaskGroups = completedTaskGroups.toCompletedPreviewMap(),
                ),
            onEvent = {},
        )
    }
}

// endregion

// region Active Task Group Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActiveTasksByDatePreview() {
    val previewTomorrow = LocalDate(2025, 6, 16)
    val nextWeek = LocalDate(2025, 6, 20)
    val overdueDateA = LocalDate(2025, 6, 10)
    val overdueDateB = LocalDate(2025, 6, 13)

    fun taskWithDate(
        id: Long,
        title: String,
        date: LocalDate?,
    ) = Task(
        id = id,
        title = title,
        description = "",
        reminderDate = date?.let { LocalDateTime(it.year, it.monthNumber, it.dayOfMonth, 9, 0) },
    )

    val activeTaskGroups: Map<ActiveGroupKey, List<Task>> =
        linkedMapOf(
            ActiveGroupKey.Overdue to
                listOf(
                    taskWithDate(6, "Overdue report", overdueDateA),
                    taskWithDate(7, "Missed deadline", overdueDateB),
                ),
            ActiveGroupKey.ByDate(PREVIEW_TODAY) to
                listOf(
                    taskWithDate(1, "Morning standup", PREVIEW_TODAY),
                    taskWithDate(2, "Review pull request", PREVIEW_TODAY),
                ),
            ActiveGroupKey.ByDate(previewTomorrow) to
                listOf(
                    taskWithDate(3, "Dentist appointment", previewTomorrow),
                ),
            ActiveGroupKey.ByDate(nextWeek) to
                listOf(
                    taskWithDate(4, "Team retrospective", nextWeek),
                ),
            ActiveGroupKey.ByDate(null) to
                listOf(
                    taskWithDate(5, "Someday task", null),
                ),
        )

    TempoTheme {
        TasksContent(
            uiState =
                TasksContract.UiState(
                    isLoading = false,
                    sortOption = SortOption.BY_DATE,
                    activeTasks = activeTaskGroups.toActivePreviewMap(),
                ),
            onEvent = {},
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActiveTasksByPriorityPreview() {
    fun taskWithPriority(
        id: Long,
        title: String,
        priority: Priority?,
    ) = Task(
        id = id,
        title = title,
        description = "",
        priority = priority,
    )

    val activeTaskGroups: Map<ActiveGroupKey, List<Task>> =
        linkedMapOf(
            ActiveGroupKey.ByPriority(Priority.HIGH) to
                listOf(
                    taskWithPriority(1, "Fix production bug", Priority.HIGH),
                ),
            ActiveGroupKey.ByPriority(Priority.MEDIUM) to
                listOf(
                    taskWithPriority(2, "Write unit tests", Priority.MEDIUM),
                    taskWithPriority(3, "Update README", Priority.MEDIUM),
                ),
            ActiveGroupKey.ByPriority(Priority.LOW) to
                listOf(
                    taskWithPriority(4, "Refactor utils", Priority.LOW),
                ),
            ActiveGroupKey.ByPriority(null) to
                listOf(
                    taskWithPriority(5, "Random task", null),
                ),
        )

    TempoTheme {
        TasksContent(
            uiState =
                TasksContract.UiState(
                    isLoading = false,
                    sortOption = SortOption.BY_PRIORITY,
                    activeTasks = activeTaskGroups.toActivePreviewMap(),
                ),
            onEvent = {},
        )
    }
}

// endregion

@PreviewFormFactors
@Composable
private fun TasksContentPreviewFormFactors() {
    fun taskWithDate(
        id: Long,
        title: String,
        date: LocalDate?,
    ) = Task(
        id = id,
        title = title,
        description = "",
        reminderDate = date?.let { LocalDateTime(it.year, it.monthNumber, it.dayOfMonth, 9, 0) },
    )

    val activeTaskGroups: Map<ActiveGroupKey, List<Task>> =
        linkedMapOf(
            ActiveGroupKey.ByDate(PREVIEW_TODAY) to
                listOf(
                    taskWithDate(1, "Morning standup", PREVIEW_TODAY),
                    taskWithDate(2, "Review pull request", PREVIEW_TODAY),
                ),
            ActiveGroupKey.ByDate(null) to
                listOf(
                    taskWithDate(3, "Someday task", null),
                ),
        )

    TempoTheme {
        TasksContent(
            uiState =
                TasksContract.UiState(
                    isLoading = false,
                    sortOption = SortOption.BY_DATE,
                    activeTasks = activeTaskGroups.toActivePreviewMap(),
                ),
            onEvent = {},
        )
    }
}
