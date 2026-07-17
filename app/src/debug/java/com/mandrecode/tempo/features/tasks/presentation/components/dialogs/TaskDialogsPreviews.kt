package com.mandrecode.tempo.features.tasks.presentation.components.dialogs

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeleteTaskConfirmDialogPreview() {
    TempoTheme {
        DeleteTaskConfirmDialog(
            onCancelDeleteTask = {},
            onConfirmDeleteTask = {},
            taskToDelete = Task(id = 1, title = "Buy groceries", description = ""),
            subtasksCount = 0,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeleteTaskConfirmDialogWithSubtasksPreview() {
    TempoTheme {
        DeleteTaskConfirmDialog(
            onCancelDeleteTask = {},
            onConfirmDeleteTask = {},
            taskToDelete = Task(id = 1, title = "Plan the offsite", description = ""),
            subtasksCount = 3,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryDialogAddPreview() {
    TempoTheme {
        CategoryDialog(
            title = "Add category",
            onDismiss = {},
            onClearError = {},
            onConfirm = {},
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryDialogEditPreview() {
    TempoTheme {
        CategoryDialog(
            title = "Edit category",
            category = Category(id = 1, name = "Work"),
            onDismiss = {},
            onClearError = {},
            onConfirm = {},
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeleteCategoryDialogPreview() {
    TempoTheme {
        DeleteCategoryDialog(
            onCancelDeleteCategory = {},
            onDeleteCategory = {},
            categoryToDelete = Category(id = 1, name = "Work"),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeleteCompletedConfirmationDialogPreview() {
    TempoTheme {
        DeleteCompletedConfirmationDialog(
            onCancelDeleteCompletedTasks = {},
            onConfirmDeleteCompletedTasks = {},
        )
    }
}
