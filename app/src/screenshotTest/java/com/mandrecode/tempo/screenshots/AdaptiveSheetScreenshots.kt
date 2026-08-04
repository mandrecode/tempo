package com.mandrecode.tempo.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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

/**
 * The real editor sheet, resolved through the real placement rule, in the container the real
 * screen gives it.
 *
 * `rememberSheetPlacement()` reads the window width, so at the tablet spec this takes the docked
 * path — the same fork `PlanTasksSheet` took when it shipped broken on a 1280dp tablet in #366.
 * That bug was invisible to the 265 instrumented tests because they all run at one window size
 * and a Compose test cannot resize the host window; a preview renders at whatever the device
 * spec says, which is exactly the axis those tests cannot move.
 *
 * The [Row] mirrors `TasksScreen`: live content takes the remaining width and the editor gets a
 * [DockedEditorWidth] column at the end. Rendering the sheet bare instead would let the docked
 * pane stretch across the whole window and bake *the #366 bug itself* into the reference image.
 *
 * Tablet-only by way of [PreviewDockedWindow] — below the breakpoint this is a `Dialog`, which
 * layoutlib does not render. See that annotation for why no reference is kept for those widths.
 */
@Composable
private fun AdaptiveTaskSheet(task: Task?) {
    ScreenshotTheme {
        // The background has to be painted explicitly: `showBackground = true` fills the preview
        // white whatever the theme, so without this the live-content side of a dark-theme tablet
        // reference comes out white and the image misrepresents the app.
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
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
@PreviewDockedWindow
@Composable
private fun AdaptiveTaskSheetNewTask() {
    AdaptiveTaskSheet(task = null)
}

@PreviewTest
@PreviewDockedWindow
@Composable
private fun AdaptiveTaskSheetEditTask() {
    AdaptiveTaskSheet(task = editedTask)
}
