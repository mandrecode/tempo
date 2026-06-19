package com.mandrecode.tempo.features.tasks.presentation.components.cards

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDateTime

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TaskItemShortTitlePreview() {
    TempoTheme {
        TaskItem(
            task = Task(id = 1, title = "Buy groceries", description = ""),
            onToggleCompletion = {},
            onEdit = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TaskItemLongTitlePreview() {
    TempoTheme {
        TaskItem(
            task =
                Task(
                    id = 2,
                    title = "Review the quarterly budget report and prepare notes for the team meeting",
                    description = "",
                ),
            onToggleCompletion = {},
            onEdit = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TaskItemWithSubtasksCollapsedPreview() {
    TempoTheme {
        TaskItem(
            task =
                Task(
                    id = 3,
                    title = "Plan vacation",
                    description = "",
                    priority = Priority.HIGH,
                ),
            onToggleCompletion = {},
            onEdit = {},
            subtasks =
                listOf(
                    Task(id = 31, title = "Book flights", description = "", parentTaskId = 3),
                    Task(id = 32, title = "Reserve hotel", description = "", parentTaskId = 3),
                    Task(
                        id = 33,
                        title = "Pack bags",
                        description = "",
                        parentTaskId = 3,
                        isCompleted = true,
                    ),
                ),
            isSubtasksExpanded = false,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TaskItemWithSubtasksExpandedPreview() {
    TempoTheme {
        TaskItem(
            task =
                Task(
                    id = 4,
                    title = "Plan vacation",
                    description = "",
                    priority = Priority.HIGH,
                ),
            onToggleCompletion = {},
            onEdit = {},
            subtasks =
                listOf(
                    Task(id = 41, title = "Book flights", description = "", parentTaskId = 4),
                    Task(id = 42, title = "Reserve hotel", description = "", parentTaskId = 4),
                    Task(
                        id = 43,
                        title = "Pack bags",
                        description = "",
                        parentTaskId = 4,
                        isCompleted = true,
                    ),
                ),
            isSubtasksExpanded = true,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TaskItemWithSubtaskMetadataPreview() {
    TempoTheme {
        TaskItem(
            task =
                Task(
                    id = 6,
                    title = "Plan vacation",
                    description = "",
                    priority = Priority.HIGH,
                ),
            onToggleCompletion = {},
            onEdit = {},
            subtasks =
                listOf(
                    Task(
                        id = 61,
                        title = "Book flights",
                        description = "Check prices on multiple airlines",
                        parentTaskId = 6,
                        priority = Priority.HIGH,
                        reminderDate = kotlinx.datetime.LocalDateTime(2026, 4, 1, 10, 0),
                    ),
                    Task(
                        id = 62,
                        title = "Reserve hotel",
                        description = "",
                        parentTaskId = 6,
                        priority = Priority.MEDIUM,
                    ),
                    Task(
                        id = 63,
                        title = "Pack bags",
                        description = "Don't forget the charger",
                        parentTaskId = 6,
                        isCompleted = true,
                        completedAt = kotlinx.datetime.LocalDateTime(2026, 3, 25, 14, 30),
                    ),
                    Task(
                        id = 64,
                        title = "Buy travel insurance",
                        description = "",
                        parentTaskId = 6,
                    ),
                ),
            isSubtasksExpanded = true,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TaskItemWithLongDescriptionPreview() {
    TempoTheme {
        TaskItem(
            task =
                Task(
                    id = 5,
                    title = "Write documentation",
                    description =
                        "This is a very long description that should overflow and trigger " +
                            "the expand button alongside the add subtask button to verify horizontal layout",
                ),
            onToggleCompletion = {},
            onEdit = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TaskItemMetadataConstrainedWidthPreview() {
    TempoTheme {
        TaskItem(
            task =
                Task(
                    id = 7,
                    title = "Finalize migration rollout",
                    description = "",
                    priority = Priority.HIGH,
                    periodicity = Periodicity.WEEKLY,
                    isCompleted = true,
                    completedAt = LocalDateTime(2026, 6, 15, 9, 45),
                ),
            onToggleCompletion = {},
            onEdit = {},
            modifier =
                Modifier
                    .width(240.dp)
                    .padding(8.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TaskItemReminderDateNoSubtasksPreview() {
    TempoTheme {
        TaskItem(
            task =
                Task(
                    id = 9,
                    title = "Review design mockups",
                    description = "",
                    priority = Priority.HIGH,
                    periodicity = Periodicity.WEEKLY,
                    reminderDate = LocalDateTime(2026, 6, 17, 11, 48),
                ),
            onToggleCompletion = {},
            onEdit = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TaskItemReminderDateWithSubtasksPreview() {
    val parentTaskId = 10L
    TempoTheme {
        TaskItem(
            task =
                Task(
                    id = parentTaskId,
                    title = "Review design mockups",
                    description = "Check all screens in Figma",
                    priority = Priority.HIGH,
                    periodicity = Periodicity.WEEKLY,
                    reminderDate = LocalDateTime(2026, 6, 17, 11, 48),
                ),
            onToggleCompletion = {},
            onEdit = {},
            subtasks =
                listOf(
                    Task(
                        id = 101,
                        title = "Mobile screens",
                        description = "",
                        parentTaskId = parentTaskId,
                        isCompleted = true,
                    ),
                ),
            isSubtasksExpanded = false,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TaskItemDescriptionAndAllIconsPreview() {
    val parentTaskId = 8L
    TempoTheme {
        TaskItem(
            task =
                Task(
                    id = parentTaskId,
                    title = "Launch v1.2 release",
                    description = "Prepare release notes, validate rollout checklist, and notify stakeholders.",
                    priority = Priority.HIGH,
                    periodicity = Periodicity.WEEKLY,
                    isCompleted = false,
                    reminderDate = LocalDateTime(2026, 6, 15, 9, 0),
                ),
            onToggleCompletion = {},
            onEdit = {},
            subtasks =
                listOf(
                    Task(
                        id = 81,
                        title = "Prepare changelog",
                        description = "",
                        parentTaskId = parentTaskId,
                        isCompleted = true,
                    ),
                    Task(
                        id = 82,
                        title = "Publish announcement",
                        description = "",
                        parentTaskId = parentTaskId,
                        isCompleted = false,
                    ),
                ),
            isSubtasksExpanded = false,
            modifier = Modifier.padding(8.dp),
        )
    }
}
