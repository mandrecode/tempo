package com.mandrecode.tempo.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.adaptive.DockedEditorPane
import com.mandrecode.tempo.core.ui.adaptive.rememberSheetPlacement
import com.mandrecode.tempo.core.ui.glass.ambientBackground
import com.mandrecode.tempo.core.ui.navigation.FloatingRailExpandedContentStartPadding
import com.mandrecode.tempo.core.ui.navigation.adaptiveScreenContentLayout
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.TasksContent
import com.mandrecode.tempo.features.tasks.presentation.TasksContract
import com.mandrecode.tempo.features.tasks.presentation.components.TaskBottomSheet
import com.mandrecode.tempo.features.tasks.presentation.model.ActiveGroupKey
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList

// Colors are `ColorOption.labelKey` values and icons are `TempoIcon.iconName` values, because
// that is what `resolveColor()` and `TempoIcon.fromName()` match on. Drawable-style names
// ("ic_work", "material_blue") resolve to nothing and silently fall back to the default glyph
// and color, which would pin reference images that do not look like the app.
private val categories =
    listOf(
        Category(id = 1L, name = "Inbox", icon = "inbox", isDefault = true, sortOrder = 0),
        Category(id = 2L, name = "Work", color = "color_m3_blue", icon = "work", sortOrder = 1),
        Category(
            id = 3L,
            name = "Personal",
            color = "color_m3_green",
            icon = "home",
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

private val listTasks =
    persistentListOf(
        editedTask,
        Task(
            id = 2L,
            title = "Book dentist appointment",
            description = "",
            categoryId = 2L,
            sortOrder = 2,
        ),
        Task(
            id = 3L,
            title = "Plan the team offsite",
            description = "",
            categoryId = 2L,
            priority = Priority.MEDIUM,
            sortOrder = 3,
        ),
    )

private val tasksUiState =
    TasksContract.UiState(
        tasks = listTasks,
        categories = categories.toPersistentList(),
        isLoading = false,
        selectedCategoryId = 2L,
        uncompletedTasksCounts = persistentMapOf(1L to 0, 2L to listTasks.size, 3L to 0),
        activeTasks = persistentMapOf(ActiveGroupKey.ByDate(null) to listTasks),
    )

/**
 * The docked Tasks screen: the real list and the real editor sheet, resolved through the real
 * placement rule, in the container the real screen gives them.
 *
 * `rememberSheetPlacement()` reads the window width, so at the docking specs this takes the docked
 * path — the same fork `PlanTasksSheet` took when it shipped broken on a 1280dp tablet in #366.
 * That bug was invisible to the 265 instrumented tests because they all run at one window size
 * and a Compose test cannot resize the host window; a preview renders at whatever the device
 * spec says, which is exactly the axis those tests cannot move.
 *
 * The [Row] mirrors `TasksScreen`: the list takes the remaining width behind the expanded rail's
 * clearance, leaning towards the pane as it does while docked, and the editor sits in the real
 * [DockedEditorPane]. Rendering the sheet bare instead would let the docked pane stretch across the
 * whole window and bake *the #366 bug itself* into the reference image; rendering it beside an
 * empty spacer would hide the seam between the list's content block and the pane — the gutter,
 * the matching top edge and the matching corners — which is what these references pin. The rail
 * itself is app-level chrome and not drawn; its clearance is.
 *
 * Docking widths only by way of [PreviewDockedWindow] — below the breakpoint this is a `Dialog`,
 * which layoutlib does not render. See that annotation for why no reference is kept for those
 * widths.
 */
@Composable
private fun DockedTasksScreen(task: Task?) {
    ScreenshotTheme {
        var contentBlockTop by remember { mutableStateOf(0.dp) }
        // The background has to be painted explicitly: `showBackground = true` fills the preview
        // white whatever the theme, so without this the live-content side of a dark-theme tablet
        // reference comes out white and the image misrepresents the app.
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .ambientBackground(),
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                TasksContent(
                    uiState = tasksUiState,
                    onEvent = {},
                    selectedTaskId = task?.id,
                    onContentBlockTopChange = { contentBlockTop = it },
                    modifier =
                        Modifier.adaptiveScreenContentLayout(
                            railClearance = FloatingRailExpandedContentStartPadding,
                            horizontalBias = 1f,
                        ),
                )
            }
            DockedEditorPane(visible = true, contentBlockTop = contentBlockTop) {
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
@PreviewDockedWindow
@Composable
private fun DockedTasksScreenNewTask() {
    DockedTasksScreen(task = null)
}

@PreviewTest
@PreviewDockedWindow
@Composable
private fun DockedTasksScreenEditTask() {
    DockedTasksScreen(task = editedTask)
}
