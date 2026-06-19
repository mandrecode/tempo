package com.mandrecode.tempo.features.routines.presentation.components.dialogs

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.habit
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.quitHabit
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.sampleChain

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeleteHabitConfirmDialogPreview() {
    TempoTheme {
        DeleteHabitConfirmDialog(
            onCancel = {},
            onConfirm = {},
            habitToDelete = habit(title = "Morning Stretch"),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeleteHabitChainConfirmDialogPreview() {
    TempoTheme {
        DeleteHabitChainConfirmDialog(
            onCancel = {},
            onConfirm = {},
            habitChainToDelete = sampleChain(),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyHabitChainConfirmDialogPreview() {
    TempoTheme {
        EmptyHabitChainConfirmDialog(
            onCancel = {},
            onConfirm = {},
            habitChain = sampleChain(),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClearRemindersConfirmDialogSinglePreview() {
    TempoTheme {
        ClearRemindersConfirmDialog(
            onCancel = {},
            onConfirm = {},
            habitsWithReminders = listOf(habit(title = "Stretch", reminderHour = 7)),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClearRemindersConfirmDialogMultiplePreview() {
    TempoTheme {
        ClearRemindersConfirmDialog(
            onCancel = {},
            onConfirm = {},
            habitsWithReminders =
                listOf(
                    habit(id = 1L, title = "Stretch", reminderHour = 7),
                    habit(id = 2L, title = "Meditate", reminderHour = 8),
                    quitHabit(id = 100L, title = "No Smoking"),
                ),
        )
    }
}
