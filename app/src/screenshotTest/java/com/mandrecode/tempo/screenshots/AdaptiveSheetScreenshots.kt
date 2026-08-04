package com.mandrecode.tempo.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.adaptive.DockedEditorPadding
import com.mandrecode.tempo.core.ui.adaptive.DockedEditorWidth
import com.mandrecode.tempo.core.ui.adaptive.rememberSheetPlacement
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.TasksContract
import com.mandrecode.tempo.features.tasks.presentation.components.TaskBottomSheet

private val categories =
    listOf(
        Category(id = 1L, name = "Inbox", icon = "ic_category", isDefault = true, sortOrder = 0),
        Category(id = 2L, name = "Work", color = "material_blue", icon = "ic_work", sortOrder = 1),
        Category(
            id = 3L,
            name = "Personal",
            color = "material_green",
            icon = "ic_person",
            sortOrder = 2,
        ),
    )

private val editedTask =
    Task(
        id = 1L,
        title = "Review pull request",
        description = "Check the new category feature implementation",
        categoryId = 2L,
        priority = Priority.HIGH,
        isCompleted = false,
        sortOrder = 1,
    )

/**
 * The real editor sheet, resolved through the real placement rule, in the container the real
 * screen gives it.
 *
 * `rememberSheetPlacement()` reads the window width, so this docks on the tablet spec and stays
 * a bottom sheet on the other two — the same fork `PlanTasksSheet` took when it shipped broken
 * on a 1280dp tablet in #366. That bug was invisible to the 265 instrumented tests because they
 * all run at one window size and a Compose test cannot resize the host window; a preview renders
 * at whatever the device spec says, which is exactly the axis those tests cannot move.
 *
 * The [Row] mirrors `TasksScreen`: live content takes the remaining width and the editor gets a
 * [DockedEditorWidth] column at the end. Rendering the sheet bare instead would let the docked
 * pane stretch across the whole window and bake *the #366 bug itself* into the reference image.
 *
 * On the phone and foldable specs the sheet is a `Dialog`, and layoutlib does not render dialog
 * windows in previews — those references capture the scrim only. They are kept anyway: a
 * scrim-only image is a precise assertion that the width falls on the bottom-sheet side of the
 * 1200dp breakpoint, so moving the breakpoint fails them.
 */
@Composable
private fun AdaptiveTaskSheet(task: Task?) {
    ScreenshotTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier =
                    Modifier
                        .width(DockedEditorWidth)
                        .fillMaxHeight()
                        .padding(
                            end = DockedEditorPadding,
                            top = DockedEditorPadding,
                            bottom = DockedEditorPadding,
                        ),
            ) {
                TaskBottomSheet(
                    categories = categories,
                    selectedCategoryIdFromFilter = if (task == null) 1L else 2L,
                    formState = TasksContract.TaskFormState(isVisible = true),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                    task = task,
                    onDelete = if (task != null) ({}) else null,
                    onToggleCompletion = if (task != null) ({}) else null,
                    placement = rememberSheetPlacement(),
                )
            }
        }
    }
}

@PreviewTest
@PreviewAdaptiveFormFactors
@Composable
private fun AdaptiveTaskSheetNewTask() {
    AdaptiveTaskSheet(task = null)
}

@PreviewTest
@PreviewAdaptiveFormFactors
@Composable
private fun AdaptiveTaskSheetEditTask() {
    AdaptiveTaskSheet(task = editedTask)
}
