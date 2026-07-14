package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.MonthDayOption
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.components.DayOfWeekSelector
import com.mandrecode.tempo.core.ui.components.ExpressiveChip
import com.mandrecode.tempo.core.ui.components.TaskCompletionCheckbox
import com.mandrecode.tempo.core.ui.theme.TempoIcon
import com.mandrecode.tempo.core.ui.theme.inputTitle
import com.mandrecode.tempo.core.ui.util.DescriptionEditorState
import com.mandrecode.tempo.core.ui.util.color
import com.mandrecode.tempo.core.ui.util.titleResId
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

@Composable
internal fun TaskBottomSheetBody(
    state: TaskBottomSheetBodyState,
    descriptionState: DescriptionEditorState,
    actions: TaskBottomSheetBodyActions,
    focusConfig: TaskBottomSheetFocusConfig,
) {
    Column {
        TaskTitleSection(
            state = state,
            actions = actions,
            focusConfig = focusConfig,
        )

        TaskDescriptionSection(
            descriptionState = descriptionState,
            descriptionError = state.formState.descriptionError,
            onDescriptionChange = actions.onTaskDescriptionChanged,
            focusConfig = focusConfig,
        )

        Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))

        TaskCategorySection(
            state = state,
            onSelectCategory = actions.onSelectCategory,
        )

        Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))

        TaskPrioritySection(
            state = state,
            onSetPriority = actions.onSetPriority,
            onClearPriority = actions.onClearPriority,
        )

        Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))

        TaskReminderAndPeriodicitySection(
            state = state,
            actions = actions,
        )

        TaskBottomSheetFooter(
            isEditingTask = state.isEditingTask,
            taskTitle = state.taskTitle,
            autoSaveEnabled = state.autoSaveEnabled,
            onDelete = actions.onDelete,
            onRequestDismiss = actions.onRequestDismiss,
            onConfirmClick = actions.onConfirmClick,
        )
    }
}

@Composable
private fun TaskTitleSection(
    state: TaskBottomSheetBodyState,
    actions: TaskBottomSheetBodyActions,
    focusConfig: TaskBottomSheetFocusConfig,
) {
    val task = state.task
    val onToggleCompletion = actions.onToggleCompletion
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (task != null && onToggleCompletion != null) {
            TaskCompletionCheckbox(
                isCompleted = task.isCompleted,
                onToggle = { onToggleCompletion(task) },
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        TextField(
            value = state.taskTitle,
            onValueChange = actions.onTaskTitleChanged,
            placeholder = {
                Text(
                    stringResource(R.string.task_title_placeholder),
                    style = MaterialTheme.typography.inputTitle,
                )
            },
            textStyle = MaterialTheme.typography.inputTitle,
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    errorIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    errorContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
            modifier =
                Modifier
                    .weight(1f)
                    .testTag(TASK_BOTTOM_SHEET_TITLE_FIELD_TEST_TAG)
                    .focusRequester(focusConfig.titleFocusRequester),
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        defaultKeyboardAction(ImeAction.Done)
                        focusManager.clearFocus()
                    },
                ),
            singleLine = false,
            maxLines = 3,
            isError = state.isTitleError || state.formState.titleError != null,
            supportingText =
                if (state.isTitleError || state.formState.titleError != null) {
                    {
                        Text(
                            state.formState.titleError?.let { stringResource(it) }
                                ?: stringResource(R.string.task_title_required),
                        )
                    }
                } else {
                    null
                },
        )
    }
}

@Composable
private fun TaskDescriptionSection(
    descriptionState: DescriptionEditorState,
    descriptionError: Int?,
    onDescriptionChange: (TextFieldValue) -> Unit,
    focusConfig: TaskBottomSheetFocusConfig,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.width(48.dp).padding(top = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_short_text),
                contentDescription = stringResource(R.string.description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
        ) {
            TaskDescriptionField(
                descriptionState = descriptionState,
                descriptionError = descriptionError,
                onDescriptionChange = onDescriptionChange,
                focusConfig = focusConfig,
            )
        }
    }
}

@Composable
private fun TaskDescriptionField(
    descriptionState: DescriptionEditorState,
    descriptionError: Int?,
    onDescriptionChange: (TextFieldValue) -> Unit,
    focusConfig: TaskBottomSheetFocusConfig,
) {
    TextField(
        value = descriptionState.value,
        onValueChange = onDescriptionChange,
        placeholder = {
            Text(
                stringResource(R.string.add_details),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                errorIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                errorContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(TASK_BOTTOM_SHEET_DESCRIPTION_FIELD_TEST_TAG)
                .focusRequester(focusConfig.descriptionFocusRequester),
        maxLines = 5,
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
        isError = descriptionError != null,
        supportingText =
            if (descriptionError != null) {
                { Text(stringResource(descriptionError)) }
            } else {
                null
            },
    )
}

@Composable
private fun TaskCategorySection(
    state: TaskBottomSheetBodyState,
    onSelectCategory: (Long) -> Unit,
) {
    val isAddingNewSubtask = state.task == null && state.formState.parentTaskId != null
    val categoryEnabled = !isAddingNewSubtask

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_category),
                contentDescription = stringResource(R.string.category_label),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(state.categories) { index, category ->
                val isSelected = category.id == state.selectedCategoryId

                ExpressiveChip(
                    label = category.name,
                    isSelected = isSelected,
                    isFirst = index == 0,
                    isLast = index == state.categories.size - 1,
                    onClick = {
                        if (categoryEnabled) {
                            onSelectCategory(category.id)
                        }
                    },
                    enabled = categoryEnabled,
                    icon = {
                        val iconRes =
                            category.icon?.let { iconName ->
                                TempoIcon.fromName(iconName)?.iconRes
                            } ?: R.drawable.ic_category
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun TaskPrioritySection(
    state: TaskBottomSheetBodyState,
    onSetPriority: (Priority) -> Unit,
    onClearPriority: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_flag),
                contentDescription = stringResource(R.string.sort_option_priority),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(Priority.priorities) { index, priority ->
                val isSelected = state.formState.priority == priority

                ExpressiveChip(
                    label = stringResource(priority.titleResId),
                    isSelected = isSelected,
                    isFirst = index == 0,
                    isLast = index == Priority.priorities.size - 1,
                    height = 48.dp,
                    enabled = !state.isPriorityReadOnly,
                    onClick = {
                        if (state.isPriorityReadOnly) {
                            return@ExpressiveChip
                        }
                        if (isSelected) {
                            onClearPriority()
                        } else {
                            onSetPriority(priority)
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_flag),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    selectedContainerColor = priority.color.copy(alpha = 0.2f),
                    selectedContentColor = priority.color,
                    modifier =
                        Modifier.testTag(
                            "taskPriorityChip_${priority.name}",
                        ),
                )
            }
        }
    }
}

@Composable
private fun TaskReminderAndPeriodicitySection(
    state: TaskBottomSheetBodyState,
    actions: TaskBottomSheetBodyActions,
) {
    Column {
        if (state.formattedReminder != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.width(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_reminder),
                        contentDescription = stringResource(R.string.reminder_label),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                InputChip(
                    selected = true,
                    onClick = actions.onSetReminderClicked,
                    shape =
                        androidx.compose.foundation.shape
                            .RoundedCornerShape(24.dp),
                    label = {
                        Text(
                            text = state.formattedReminder,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    },
                    trailingIcon = {
                        if (state.task == null || !state.task.isCompleted) {
                            IconButton(onClick = actions.onClearReminder) {
                                Icon(
                                    painterResource(R.drawable.ic_close),
                                    contentDescription = stringResource(R.string.clear_reminder),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                    colors =
                        InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    border = null,
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(start = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))

            if ((state.task == null || state.task.parentTaskId == null) && state.formState.parentTaskId == null) {
                TaskPeriodicitySection(
                    state = state,
                    actions = actions,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.width(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_reminder),
                        contentDescription = stringResource(R.string.reminder_label),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                ExpressiveChip(
                    label = stringResource(R.string.add_reminder),
                    isSelected = false,
                    isFirst = true,
                    isLast = true,
                    onClick = actions.onSetReminderClicked,
                    enabled = state.task == null || !state.task.isCompleted,
                    horizontalArrangement = Arrangement.Start,
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun TaskPeriodicitySection(
    state: TaskBottomSheetBodyState,
    actions: TaskBottomSheetBodyActions,
) {
    val isPeriodicityReadOnly = state.task?.isCompleted == true
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(48.dp)
                    .then(
                        if (isPeriodicityReadOnly) {
                            Modifier.alpha(0.5f)
                        } else {
                            Modifier
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_repeat),
                contentDescription = stringResource(R.string.repeat),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val periodicityListState = rememberLazyListState()

        LaunchedEffect(state.formState.periodicity) {
            val index =
                Periodicity.periods.indexOfFirst {
                    it == state.formState.periodicity
                }
            if (index < 0) return@LaunchedEffect

            val isFullyVisible =
                snapshotFlow {
                    val info = periodicityListState.layoutInfo
                    if (info.visibleItemsInfo.isEmpty()) {
                        null
                    } else {
                        val item =
                            info.visibleItemsInfo.firstOrNull {
                                it.index == index
                            }
                        item != null &&
                            item.offset >= info.viewportStartOffset &&
                            (item.offset + item.size) <=
                            info.viewportEndOffset
                    }
                }.filterNotNull().first()

            if (!isFullyVisible) {
                periodicityListState.animateScrollToItem(index)
            }
        }

        LazyRow(
            state = periodicityListState,
            contentPadding = PaddingValues(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(Periodicity.periods) { index, period ->
                val isSelected = state.formState.periodicity == period

                ExpressiveChip(
                    label =
                        periodicityChipLabel(
                            period = period,
                            isSelected = isSelected,
                            interval = state.formState.periodicityInterval,
                            repeatDays = state.formState.repeatDays,
                            monthDayOption = state.formState.monthDayOption,
                        ),
                    isSelected = isSelected,
                    isFirst = index == 0,
                    isLast = index == Periodicity.periods.size - 1,
                    height = 44.dp,
                    enabled = !isPeriodicityReadOnly,
                    onClick = {
                        if (isSelected) {
                            actions.onClearPeriodicity()
                        } else {
                            actions.onSetPeriodicity(period)
                        }
                    },
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier =
                        Modifier.testTag(
                            "taskPeriodicityChip_${period.name}",
                        ),
                )
            }
        }
    }

    AnimatedVisibility(
        visible = state.formState.periodicity != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(48.dp))

                PeriodicityIntervalSelector(
                    interval = state.formState.periodicityInterval,
                    onIntervalChange = actions.onSetPeriodicityInterval,
                    modifier = Modifier.padding(start = 4.dp),
                    enabled = !isPeriodicityReadOnly,
                    maxInterval =
                        if (state.formState.periodicity == Periodicity.HOURLY) {
                            Periodicity.MAX_HOURLY_INTERVAL
                        } else {
                            Int.MAX_VALUE
                        },
                )
            }
        }
    }

    AnimatedContent(
        targetState = state.formState.periodicity,
        transitionSpec = {
            (fadeIn() + expandVertically()).togetherWith(
                fadeOut() + shrinkVertically(),
            )
        },
        label = "periodicity_sub_options",
    ) { periodicity ->
        when (periodicity) {
            Periodicity.WEEKLY -> {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(48.dp))

                        DayOfWeekSelector(
                            selectedDays = state.formState.repeatDays,
                            onDaysChange = actions.onSetRepeatDays,
                            modifier = Modifier.weight(1f),
                            showAllDaysOption = false,
                            enabled = !isPeriodicityReadOnly,
                            selectedContainerColor =
                                MaterialTheme.colorScheme.tertiaryContainer,
                            selectedContentColor =
                                MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }

            Periodicity.MONTHLY -> {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(48.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            itemsIndexed(MonthDayOption.options) { index, option ->
                                val isOptionSelected =
                                    state.formState.monthDayOption == option ||
                                        (
                                            state.formState.monthDayOption == null &&
                                                option == MonthDayOption.SAME_DAY
                                        )

                                ExpressiveChip(
                                    label =
                                        when (option) {
                                            MonthDayOption.SAME_DAY ->
                                                stringResource(R.string.same_day)

                                            MonthDayOption.FIRST_DAY ->
                                                stringResource(
                                                    R.string.first_day_of_month,
                                                )

                                            MonthDayOption.LAST_DAY ->
                                                stringResource(
                                                    R.string.last_day_of_month,
                                                )
                                        },
                                    isSelected = isOptionSelected,
                                    isFirst = index == 0,
                                    isLast =
                                        index == MonthDayOption.options.size - 1,
                                    height = 44.dp,
                                    enabled = !isPeriodicityReadOnly,
                                    onClick = {
                                        if (option == MonthDayOption.SAME_DAY) {
                                            actions.onSetMonthDayOption(null)
                                        } else {
                                            actions.onSetMonthDayOption(option)
                                        }
                                    },
                                    selectedContainerColor =
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                    selectedContentColor =
                                        MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                    }
                }
            }

            else -> { /* No sub-options for DAILY / YEARLY */ }
        }
    }
}
