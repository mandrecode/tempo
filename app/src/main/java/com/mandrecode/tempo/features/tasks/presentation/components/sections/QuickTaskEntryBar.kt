package com.mandrecode.tempo.features.tasks.presentation.components.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.util.getIconForSortOption
import com.mandrecode.tempo.core.ui.util.rememberPressableIconButtonAnimation
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption

private const val MAX_TITLE_LENGTH = 65
internal const val QUICK_TASK_ENTRY_TITLE_FIELD_TEST_TAG = "quick_task_entry_title_field"

@Composable
fun QuickTaskEntryBar(
    onAddTask: (String) -> Unit,
    sortOption: SortOption,
    onSortClick: () -> Unit,
    showClearCompleted: Boolean,
    onClearCompletedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var taskTitle by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val submitTask = {
        if (taskTitle.isNotBlank()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onAddTask(taskTitle)
            focusManager.clearFocus()
            taskTitle = ""
        }
    }

    // Match the tonal elevation of the Bottom Navigation Bar (usually 3.dp or 8.dp)
    // Using 8.dp to ensure it blends seamlessly if the bottom bar is also 8.dp
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize(),
        tonalElevation = 8.dp,
        shadowElevation = 4.dp, // Add a subtle shadow to separate from content if needed
    ) {
        Row(
            modifier =
                Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
            // Reduced vertical padding for better integration
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SortButton(
                sortOption = sortOption,
                onClick = onSortClick,
            )

            AnimatedVisibility(
                visible = showClearCompleted,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                Row {
                    Spacer(modifier = Modifier.width(12.dp))
                    ClearCompletedButton(onClick = onClearCompletedClick)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(28.dp),
                        ).border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(28.dp),
                        ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = taskTitle,
                    onValueChange = { newValue ->
                        taskTitle = newValue.take(MAX_TITLE_LENGTH)
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.task_title_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    },
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag(QUICK_TASK_ENTRY_TITLE_FIELD_TEST_TAG)
                            .padding(start = 16.dp, end = 8.dp),
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                        ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = { submitTask() },
                        ),
                )

                QuickEntryButton(
                    enabled = taskTitle.isNotBlank(),
                    onClick = submitTask,
                )
            }
        }
    }
}

@Composable
private fun SortButton(
    sortOption: SortOption,
    onClick: () -> Unit,
) {
    val baseSortRadius = if (sortOption != SortOption.MANUAL) 16.dp else 48.dp
    val (sortInteractionSource, buttonCornerRadius) =
        rememberPressableIconButtonAnimation(
            baseRadius = baseSortRadius,
        )

    val containerColor by animateColorAsState(
        targetValue =
            if (sortOption != SortOption.MANUAL) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        animationSpec = tween(300),
        label = "sort_button_container_color",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (sortOption != SortOption.MANUAL) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
        animationSpec = tween(300),
        label = "sort_button_content_color",
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(buttonCornerRadius.value),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation =
            FloatingActionButtonDefaults.bottomAppBarFabElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
            ),
        interactionSource = sortInteractionSource,
    ) {
        Icon(
            painter = painterResource(getIconForSortOption(sortOption)),
            contentDescription = stringResource(R.string.sort_tasks_by_format, sortOption.value),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun ClearCompletedButton(onClick: () -> Unit) {
    val (deleteInteractionSource, deleteCornerRadius) = rememberPressableIconButtonAnimation()

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(deleteCornerRadius.value),
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        elevation =
            FloatingActionButtonDefaults.bottomAppBarFabElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
            ),
        interactionSource = deleteInteractionSource,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_remove_done),
            contentDescription = stringResource(R.string.delete_all_completed_tasks),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun QuickEntryButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val (interactionSource, cornerRadius) =
        rememberPressableIconButtonAnimation(
            baseRadius = 48.dp, // Circle
            pressedRadius = 12.dp,
        )

    val containerColor by animateColorAsState(
        targetValue =
            if (enabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            },
        animationSpec = tween(durationMillis = 300),
        label = "button_color",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (enabled) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(durationMillis = 300),
        label = "button_content_color",
    )

    FloatingActionButton(
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        modifier =
            Modifier
                .padding(end = 4.dp)
                .size(40.dp),
        // Slightly smaller to fit better inside the 48dp bar
        shape = RoundedCornerShape(cornerRadius.value),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation =
            FloatingActionButtonDefaults.bottomAppBarFabElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
            ),
        interactionSource = interactionSource,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_upward),
            contentDescription = stringResource(R.string.add_task),
            modifier = Modifier.size(24.dp),
        )
    }
}
