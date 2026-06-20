package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.MonthDayOption
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.components.HandleReminderPermissions
import com.mandrecode.tempo.core.ui.components.TempoTimePickerDialog
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.TasksContract
import com.mandrecode.tempo.infrastructure.permissions.hasNotificationPermissions
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class, FormatStringsInDatetimeFormats::class)
@Composable
internal fun TaskBottomSheetContent(
    categories: List<Category>,
    selectedCategoryIdFromFilter: Long?,
    formState: TasksContract.TaskFormState,
    onSetPriority: (Priority) -> Unit,
    onClearPriority: () -> Unit,
    onSetReminder: (Int, Int, Int, Int, Int) -> Unit,
    onClearReminder: () -> Unit,
    onSetPeriodicity: (Periodicity) -> Unit,
    onClearPeriodicity: () -> Unit,
    onSetPeriodicityInterval: (Int) -> Unit,
    onSetRepeatDays: (Set<DayOfWeek>?) -> Unit,
    onSetMonthDayOption: (MonthDayOption?) -> Unit,
    onDismiss: () -> Unit,
    onClearErrors: () -> Unit,
    onConfirm: (title: String, description: String, categoryId: Long) -> Unit,
    modifier: Modifier = Modifier,
    onAutoSave: ((title: String, description: String, categoryId: Long) -> Unit)? = null,
    task: Task? = null,
    onDelete: (() -> Unit)? = null,
    onToggleCompletion: ((Task) -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val editingTargetId = task?.id
    var taskTitle by remember(editingTargetId) { mutableStateOf(task?.title ?: "") }
    var taskDescription by remember(editingTargetId) { mutableStateOf(TextFieldValue(task?.description ?: "")) }

    val defaultCategoryId = categories.firstOrNull { it.isDefault }?.id ?: categories.firstOrNull()?.id ?: 0L
    val initialCategoryId =
        when {
            task != null -> task.categoryId
            selectedCategoryIdFromFilter != null &&
                selectedCategoryIdFromFilter != defaultCategoryId ->
                selectedCategoryIdFromFilter
            else -> defaultCategoryId
        }
    var selectedCategoryId by
        remember(editingTargetId, selectedCategoryIdFromFilter, defaultCategoryId) {
            mutableLongStateOf(initialCategoryId)
        }
    var isTitleError by remember(editingTargetId) { mutableStateOf(false) }
    var showPermissionCheck by remember { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    var focusDescriptionTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(focusDescriptionTrigger) {
        if (focusDescriptionTrigger > 0) {
            descriptionFocusRequester.requestFocus()
        }
    }

    val formattedReminder = rememberFormattedDateTime(formState.reminderDate)
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempSelectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val onSetReminderClicked: () -> Unit = {
        if (task == null || !task.isCompleted) {
            showPermissionCheck = true
        }
    }

    if (showDatePicker) {
        val futureDatesOnly =
            remember {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val today =
                            Clock.System
                                .now()
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                        return utcTimeMillis >= today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
                    }
                }
            }
        com.mandrecode.tempo.core.ui.components.TempoDatePickerDialog(
            initialDate =
                formState.reminderDate?.date
                    ?: Clock.System
                        .now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date,
            onConfirm = { date ->
                tempSelectedDate = date
                showDatePicker = false
                showTimePicker = true
            },
            onDismiss = { showDatePicker = false },
            selectableDates = futureDatesOnly,
        )
    }

    if (showTimePicker) {
        val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }

        TempoTimePickerDialog(
            initialHour = formState.reminderDate?.hour ?: now.hour,
            initialMinute = formState.reminderDate?.minute ?: now.minute,
            onConfirm = { hour, minute ->
                val dateToUse = tempSelectedDate ?: now.date

                onSetReminder(
                    dateToUse.year,
                    dateToUse.month.number,
                    dateToUse.day,
                    hour,
                    minute,
                )
                showTimePicker = false
                tempSelectedDate = null
            },
            onDismiss = {
                showTimePicker = false
                tempSelectedDate = null
            },
        )
    }

    if (showPermissionCheck) {
        HandleReminderPermissions(
            show = true,
            onGrantPermissions = {
                showDatePicker = true
                showPermissionCheck = false
            },
            onDismiss = {
                showPermissionCheck = false
            },
        )
    }

    val currentOnClearReminder by rememberUpdatedState(onClearReminder)
    LaunchedEffect(task) {
        if (task != null && formState.reminderDate != null) {
            if (!context.hasNotificationPermissions()) {
                currentOnClearReminder()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (task == null) {
            titleFocusRequester.requestFocus()
        }
    }

    val taskSnapshot =
        remember(task?.id) {
            task?.let {
                TaskFormSnapshot(
                    title = it.title,
                    description = it.description,
                    categoryId = it.categoryId,
                    priority = it.priority,
                    reminderDate = it.reminderDate,
                    periodicity = it.periodicity,
                    periodicityInterval = it.periodicityInterval,
                    repeatDays = it.repeatDays,
                    monthDayOption = it.monthDayOption,
                )
            }
        }

    val hasUnsavedChanges =
        remember(
            taskSnapshot,
            taskTitle,
            taskDescription.text,
            selectedCategoryId,
            initialCategoryId,
            formState.priority,
            formState.reminderDate,
            formState.periodicity,
            formState.periodicityInterval,
            formState.repeatDays,
            formState.monthDayOption,
        ) {
            if (taskSnapshot == null) {
                taskTitle.isNotBlank() ||
                    taskDescription.text.isNotBlank() ||
                    selectedCategoryId != initialCategoryId ||
                    formState.priority != null ||
                    formState.reminderDate != null ||
                    formState.periodicity != null
            } else {
                taskTitle != taskSnapshot.title ||
                    taskDescription.text != taskSnapshot.description ||
                    selectedCategoryId != taskSnapshot.categoryId ||
                    formState.priority != taskSnapshot.priority ||
                    formState.reminderDate != taskSnapshot.reminderDate ||
                    formState.periodicity != taskSnapshot.periodicity ||
                    formState.periodicityInterval != taskSnapshot.periodicityInterval ||
                    formState.repeatDays != taskSnapshot.repeatDays ||
                    formState.monthDayOption != taskSnapshot.monthDayOption
            }
        }
    val isEditingTask = task != null
    val autoSaveEnabled = isEditingTask && onAutoSave != null
    val isPriorityReadOnly = task?.isCompleted == true

    val currentAutoSaveSnapshot =
        TaskFormSnapshot(
            title = taskTitle,
            description = taskDescription.text,
            categoryId = selectedCategoryId,
            priority = formState.priority,
            reminderDate = formState.reminderDate,
            periodicity = formState.periodicity,
            periodicityInterval = formState.periodicityInterval,
            repeatDays = formState.repeatDays,
            monthDayOption = formState.monthDayOption,
        )
    var lastDispatchedSnapshot by remember(task?.id) { mutableStateOf<TaskFormSnapshot?>(null) }

    LaunchedEffect(autoSaveEnabled, currentAutoSaveSnapshot, hasUnsavedChanges) {
        if (!autoSaveEnabled || !hasUnsavedChanges || taskTitle.isBlank()) return@LaunchedEffect
        delay(AUTO_SAVE_DEBOUNCE_MS)
        if (lastDispatchedSnapshot == currentAutoSaveSnapshot) return@LaunchedEffect
        onAutoSave?.invoke(
            taskTitle,
            taskDescription.text,
            selectedCategoryId,
        )
        lastDispatchedSnapshot = currentAutoSaveSnapshot
    }

    val hasPendingAutoSave =
        autoSaveEnabled && hasUnsavedChanges && currentAutoSaveSnapshot != lastDispatchedSnapshot

    val onSheetDismissRequest: () -> Unit = {
        if (autoSaveEnabled && hasPendingAutoSave && taskTitle.isNotBlank()) {
            onAutoSave?.invoke(
                taskTitle,
                taskDescription.text,
                selectedCategoryId,
            )
            lastDispatchedSnapshot = currentAutoSaveSnapshot
        }
        onDismiss()
    }

    com.mandrecode.tempo.core.ui.components.TempoModalBottomSheet(
        onDismissRequest = onSheetDismissRequest,
        modifier = modifier,
        hasUnsavedChanges = if (autoSaveEnabled && taskTitle.isNotBlank()) false else hasUnsavedChanges,
    ) { onRequestDismiss ->
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 32.dp),
        ) {
            TaskBottomSheetBody(
                state =
                    TaskBottomSheetBodyState(
                        categories = categories,
                        formState = formState,
                        task = task,
                        taskTitle = taskTitle,
                        taskDescription = taskDescription,
                        selectedCategoryId = selectedCategoryId,
                        formattedReminder = formattedReminder,
                        isTitleError = isTitleError,
                        autoSaveEnabled = autoSaveEnabled,
                        isEditingTask = isEditingTask,
                        isPriorityReadOnly = isPriorityReadOnly,
                    ),
                actions =
                    TaskBottomSheetBodyActions(
                        onTaskTitleChanged = { newValue ->
                            if (newValue.contains("\n")) {
                                focusManager.clearFocus()
                            } else if (newValue.length > MAX_TITLE_LENGTH) {
                                val overflow = newValue.substring(MAX_TITLE_LENGTH)
                                taskTitle = newValue.substring(0, MAX_TITLE_LENGTH)
                                taskDescription =
                                    TextFieldValue(
                                        text = overflow + taskDescription.text,
                                        selection = TextRange(overflow.length),
                                    )
                                focusDescriptionTrigger++
                            } else {
                                taskTitle = newValue
                                isTitleError = newValue.isBlank()
                                onClearErrors()
                            }
                        },
                        onTaskDescriptionChanged = {
                            taskDescription = it
                            onClearErrors()
                        },
                        onSelectCategory = { selectedCategoryId = it },
                        onSetPriority = onSetPriority,
                        onClearPriority = onClearPriority,
                        onSetReminderClicked = onSetReminderClicked,
                        onClearReminder = onClearReminder,
                        onSetPeriodicity = onSetPeriodicity,
                        onClearPeriodicity = onClearPeriodicity,
                        onSetPeriodicityInterval = onSetPeriodicityInterval,
                        onSetRepeatDays = onSetRepeatDays,
                        onSetMonthDayOption = onSetMonthDayOption,
                        onRequestDismiss = onRequestDismiss,
                        onDelete = onDelete,
                        onToggleCompletion = onToggleCompletion,
                        onConfirmClick = {
                            isTitleError = taskTitle.isBlank()
                            if (!isTitleError) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onConfirm(
                                    taskTitle,
                                    taskDescription.text,
                                    selectedCategoryId,
                                )
                            }
                        },
                    ),
                focusConfig =
                    TaskBottomSheetFocusConfig(
                        focusManager = focusManager,
                        titleFocusRequester = titleFocusRequester,
                        descriptionFocusRequester = descriptionFocusRequester,
                    ),
            )
        }
    }
}
