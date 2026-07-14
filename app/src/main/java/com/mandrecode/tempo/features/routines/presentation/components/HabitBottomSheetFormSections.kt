package com.mandrecode.tempo.features.routines.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.ColorPicker
import com.mandrecode.tempo.core.ui.components.DayOfWeekSelector
import com.mandrecode.tempo.core.ui.components.ExpressiveChip
import com.mandrecode.tempo.core.ui.components.HabitCompletionCheckbox
import com.mandrecode.tempo.core.ui.components.IconPicker
import com.mandrecode.tempo.core.ui.theme.inputTitle
import com.mandrecode.tempo.core.ui.theme.resolveColor
import com.mandrecode.tempo.core.ui.util.DescriptionEditorState
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract.HabitSheetTab
import com.mandrecode.tempo.features.routines.presentation.components.sections.HabitHistoryView
import com.mandrecode.tempo.util.CompletionHistoryUtil
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

internal const val HABIT_BOTTOM_SHEET_TITLE_FIELD_TEST_TAG = "habit_bottom_sheet_title_field"

@Composable
internal fun HabitBottomSheetBody(
    state: HabitBottomSheetBodyState,
    descriptionState: DescriptionEditorState,
    actions: HabitBottomSheetBodyActions,
    focusConfig: HabitBottomSheetFocusConfig,
) {
    Column {
        val editingHabit = state.formState.editingHabit
        val editingHabitChain = state.formState.editingHabitChain
        val isEditing = editingHabit != null || editingHabitChain != null
        val isCreatingForChain = state.formState.targetChainId != null

        if (!isEditing && !isCreatingForChain) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                HabitSheetTab.entries.forEachIndexed { index, tab ->
                    ExpressiveChip(
                        label =
                            when (tab) {
                                HabitSheetTab.HABIT -> stringResource(R.string.habit)
                                HabitSheetTab.HABIT_CHAIN -> stringResource(R.string.habit_chain)
                            },
                        isSelected = state.formState.selectedTab == tab,
                        onClick = { actions.onSelectTab(tab) },
                        isFirst = index == 0,
                        isLast = index == HabitSheetTab.entries.size - 1,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        AnimatedVisibility(
            visible =
                state.formState.selectedTab == HabitSheetTab.HABIT &&
                    !isCreatingForChain &&
                    !state.isHabitInChain,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                HabitTypeSelector(
                    selectedType = state.formState.selectedHabitType,
                    onTypeSelect = actions.onSetHabitType,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        HabitTitleSection(
            state = state,
            actions = actions,
            focusConfig = focusConfig,
            editingHabit = editingHabit,
        )

        HabitDescriptionSection(
            descriptionState = descriptionState,
            descriptionError = state.formState.descriptionError,
            onDescriptionChange = actions.onDescriptionChanged,
            focusConfig = focusConfig,
        )

        Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))

        HabitIconSection(
            selectedIcon = state.formState.selectedIcon,
            onSetIcon = actions.onSetIcon,
            onClearIcon = actions.onClearIcon,
        )

        Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))

        if (!state.isHabitInChain) {
            HabitColorAndRepeatSection(
                state = state,
                actions = actions,
            )
        }

        if (state.formState.selectedTab == HabitSheetTab.HABIT_CHAIN) {
            HabitChainSelectionSection(
                habits = state.habits,
                selectedHabitIds = state.selectedHabitIds,
                onSelectHabits = actions.onSelectHabits,
                selectedDate = state.selectedDate,
                onToggleHabitCompletion =
                    if (editingHabitChain != null) {
                        actions.onToggleHabitCompletion
                    } else {
                        null
                    },
            )

            Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))
        }

        val showReminderUI =
            when (state.formState.selectedTab) {
                HabitSheetTab.HABIT -> !state.isHabitInChain
                HabitSheetTab.HABIT_CHAIN -> true
            }

        if (showReminderUI) {
            HabitReminderSection(
                formattedReminder = state.formattedReminder,
                onSetReminderClick = actions.onSetReminderClicked,
                onClearReminder = actions.onClearReminder,
            )

            Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))
        }

        if (state.formState.selectedTab == HabitSheetTab.HABIT && editingHabit != null) {
            HabitHistorySection(
                completionHistory = editingHabit.completionHistory,
                createdDate = editingHabit.createdDate.date,
                repeatDays = editingHabit.repeatDays,
                habitType = state.formState.selectedHabitType,
            )

            Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))
        }

        if (state.formState.selectedTab == HabitSheetTab.HABIT_CHAIN && editingHabitChain != null) {
            HabitHistorySection(
                completionHistory = editingHabitChain.completionHistory,
                createdDate = editingHabitChain.createdDate.date,
                repeatDays = editingHabitChain.repeatDays,
                habitType = null,
            )

            Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))
        }

        HabitBottomSheetFooter(
            selectedTab = state.formState.selectedTab,
            editingHabit = editingHabit != null,
            editingHabitChain = editingHabitChain != null,
            selectedHabitIds = state.selectedHabitIds,
            title = state.title,
            hasUnsavedChanges = state.hasUnsavedChanges,
            autoSaveEnabled = state.autoSaveEnabled,
            onDeleteHabit = actions.onDeleteHabit,
            onDeleteHabitChain = actions.onDeleteHabitChain,
            onRequestDismiss = actions.onRequestDismiss,
            onConfirmClick = actions.onConfirmClick,
        )
    }
}

@Composable
private fun HabitTitleSection(
    state: HabitBottomSheetBodyState,
    actions: HabitBottomSheetBodyActions,
    focusConfig: HabitBottomSheetFocusConfig,
    editingHabit: com.mandrecode.tempo.features.routines.domain.model.Habit?,
) {
    val onToggleHabitCompletion = actions.onToggleHabitCompletion
    if (state.formState.selectedTab == HabitSheetTab.HABIT &&
        editingHabit != null &&
        onToggleHabitCompletion != null
    ) {
        val dateStr = remember(state.selectedDate) { state.selectedDate.toString() }
        val isCompleted =
            remember(editingHabit.completionHistory, dateStr) {
                CompletionHistoryUtil.isDateInHistory(editingHabit.completionHistory, dateStr)
            }
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val yesterday = today.minus(DatePeriod(days = 1))
        val canToggle = state.selectedDate == today || state.selectedDate == yesterday
        val resolvedColor =
            remember(
                state.formState.selectedColorKey,
                editingHabit.colorKey,
                state.colorScheme,
                state.isDarkTheme,
            ) {
                val key = state.formState.selectedColorKey ?: editingHabit.colorKey
                key?.let { resolveColor(it, state.colorScheme, state.isDarkTheme) }
            }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HabitCompletionCheckbox(
                isCompleted = isCompleted,
                onToggle = { onToggleHabitCompletion(editingHabit.id, !isCompleted) },
                color = resolvedColor,
                iconName = state.formState.selectedIcon ?: editingHabit.icon,
                canToggle = canToggle,
                isContainerCompleted = false,
            )
            Spacer(modifier = Modifier.width(8.dp))
            HabitTitleField(
                state = state,
                actions = actions,
                focusConfig = focusConfig,
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        HabitTitleField(
            state = state,
            actions = actions,
            focusConfig = focusConfig,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HabitTitleField(
    state: HabitBottomSheetBodyState,
    actions: HabitBottomSheetBodyActions,
    focusConfig: HabitBottomSheetFocusConfig,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = state.title,
        onValueChange = actions.onTitleChanged,
        placeholder = {
            Text(
                when (state.formState.selectedTab) {
                    HabitSheetTab.HABIT ->
                        when (state.formState.selectedHabitType) {
                            HabitType.BUILD -> stringResource(R.string.habit_title_placeholder)
                            HabitType.QUIT -> stringResource(R.string.habit_title_placeholder_quit)
                        }

                    HabitSheetTab.HABIT_CHAIN -> stringResource(R.string.habit_chain_title_placeholder)
                },
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
            modifier
                .testTag(HABIT_BOTTOM_SHEET_TITLE_FIELD_TEST_TAG)
                .focusRequester(focusConfig.titleFocusRequester),
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
        keyboardActions =
            androidx.compose.foundation.text.KeyboardActions(
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

@Composable
private fun HabitDescriptionSection(
    descriptionState: DescriptionEditorState,
    descriptionError: Int?,
    onDescriptionChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    focusConfig: HabitBottomSheetFocusConfig,
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
                    .weight(1f)
                    .padding(start = 4.dp)
                    .testTag(HABIT_BOTTOM_SHEET_DESCRIPTION_FIELD_TEST_TAG)
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
}

@Composable
private fun HabitIconSection(
    selectedIcon: String?,
    onSetIcon: (String) -> Unit,
    onClearIcon: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mood),
                contentDescription = stringResource(R.string.habit_icon_label),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconPicker(
            selectedIconName = selectedIcon,
            onSelectIcon = onSetIcon,
            onClearIcon = onClearIcon,
            enabled = true,
            disabledMessage = null,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HabitColorAndRepeatSection(
    state: HabitBottomSheetBodyState,
    actions: HabitBottomSheetBodyActions,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_draw),
                    contentDescription = stringResource(R.string.material_you_colors),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ColorPicker(
                selectedColorKey = state.formState.selectedColorKey,
                onSelectColorKey = actions.onSetColorKey,
                onClearColor = actions.onClearColor,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))

        val showRepeatDays =
            state.formState.selectedTab != HabitSheetTab.HABIT ||
                state.formState.selectedHabitType != HabitType.QUIT
        AnimatedVisibility(
            visible = showRepeatDays,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.width(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_repeat),
                            contentDescription = stringResource(R.string.repeat),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DayOfWeekSelector(
                        selectedDays = state.formState.selectedRepeatDays,
                        onDaysChange = { days -> actions.onSetRepeatDays?.invoke(days) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(PROPERTY_ROW_GAP))
            }
        }
    }
}

@Composable
private fun HabitChainSelectionSection(
    habits: List<com.mandrecode.tempo.features.routines.domain.model.Habit>,
    selectedHabitIds: List<Long>,
    onSelectHabits: (List<Long>) -> Unit,
    selectedDate: kotlinx.datetime.LocalDate,
    onToggleHabitCompletion: ((habitId: Long, isCompleted: Boolean) -> Unit)?,
) {
    val iconTopPadding = if (selectedHabitIds.isEmpty()) 12.dp else 16.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier =
                Modifier
                    .width(48.dp)
                    .padding(top = iconTopPadding),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_list),
                contentDescription = stringResource(R.string.select_habits),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HabitMultiSelector(
            habits = habits,
            selectedHabitIds = selectedHabitIds,
            onSelectHabits = onSelectHabits,
            modifier = Modifier.weight(1f),
            selectedDate = selectedDate,
            onToggleHabitCompletion = onToggleHabitCompletion,
        )
    }
}

@Composable
private fun HabitReminderSection(
    formattedReminder: String?,
    onSetReminderClick: () -> Unit,
    onClearReminder: () -> Unit,
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
                painter = painterResource(R.drawable.ic_reminder),
                contentDescription = stringResource(R.string.reminder_label),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (formattedReminder != null) {
            InputChip(
                selected = true,
                onClick = onSetReminderClick,
                shape =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(24.dp),
                label = {
                    Text(
                        text = formattedReminder,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onClearReminder) {
                        Icon(
                            painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.clear_reminder),
                            modifier = Modifier.size(18.dp),
                        )
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
        } else {
            ExpressiveChip(
                label = stringResource(R.string.add_reminder),
                isSelected = false,
                isFirst = true,
                isLast = true,
                onClick = onSetReminderClick,
                horizontalArrangement = Arrangement.Start,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun HabitHistorySection(
    completionHistory: String,
    createdDate: kotlinx.datetime.LocalDate,
    repeatDays: Set<com.mandrecode.tempo.core.domain.model.DayOfWeek>?,
    habitType: HabitType?,
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
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = stringResource(R.string.completion_history),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
        ) {
            HabitHistoryView(
                completionHistory = completionHistory,
                createdDate = createdDate,
                modifier = Modifier.fillMaxWidth(),
                repeatDays = repeatDays,
                habitType = habitType ?: HabitType.BUILD,
            )
        }
    }
}
