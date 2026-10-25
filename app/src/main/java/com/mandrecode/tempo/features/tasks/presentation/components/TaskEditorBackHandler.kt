package com.mandrecode.tempo.features.tasks.presentation.components

import android.os.Build
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onInterceptKeyBeforeSoftKeyboard
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager

internal enum class TaskEditorField {
    Title,
    Description,
    None,
}

/**
 * Explicit task editor focus, reported by each field's `onFocusChanged`. Each field keeps its own
 * flag so a title → description handoff never passes through a transient [TaskEditorField.None],
 * whatever order the two focus callbacks arrive in. Keyboard visibility is deliberately not an input.
 */
@Stable
internal class TaskEditorFocusState {
    private var isTitleFocused by mutableStateOf(false)
    private var isDescriptionFocused by mutableStateOf(false)

    val focusedField: TaskEditorField
        get() =
            when {
                isDescriptionFocused -> TaskEditorField.Description
                isTitleFocused -> TaskEditorField.Title
                else -> TaskEditorField.None
            }

    val ownsBack: Boolean
        get() = focusedField != TaskEditorField.None

    fun onTitleFocusChanged(isFocused: Boolean) {
        isTitleFocused = isFocused
    }

    fun onDescriptionFocusChanged(isFocused: Boolean) {
        isDescriptionFocused = isFocused
    }
}

/**
 * Claims back gestures while a task editor field owns focus. It must be composed inside the sheet
 * content so it registers after the sheet's own handler and therefore takes priority while enabled;
 * it stays composed and only toggles `enabled` so that ordering never changes. Progress is consumed
 * so the sheet does not move, cancellation leaves focus untouched, and completion clears focus once
 * to close the keyboard. With no field focused, back falls through to the sheet's guarded dismissal.
 */
@Composable
internal fun TaskEditorBackHandler(focusState: TaskEditorFocusState) {
    val focusManager = LocalFocusManager.current
    PredictiveBackHandler(enabled = focusState.ownsBack) { progress ->
        progress.collect { }
        focusManager.clearFocus()
    }
}

/**
 * Before Android 13 there is no predictive back: the keyboard consumes the first back key and hides
 * while the field keeps focus, so [TaskEditorBackHandler] would cost an extra back press before the
 * sheet is reached. On those releases the focused field takes the back key before the keyboard and
 * clears focus itself, giving the same one-press keyboard dismissal as newer releases. Newer
 * releases are left alone so a 3-button back key is never handled twice.
 */
internal val TASK_EDITOR_INTERCEPTS_LEGACY_IME_BACK = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

internal fun Modifier.taskTitleFocus(focusConfig: TaskBottomSheetFocusConfig): Modifier =
    focusRequester(focusConfig.titleFocusRequester)
        .onFocusChanged { focusConfig.focusState.onTitleFocusChanged(it.isFocused) }
        .legacyImeBack(focusConfig)

internal fun Modifier.taskDescriptionFocus(focusConfig: TaskBottomSheetFocusConfig): Modifier =
    focusRequester(focusConfig.descriptionFocusRequester)
        .onFocusChanged { focusConfig.focusState.onDescriptionFocusChanged(it.isFocused) }
        .legacyImeBack(focusConfig)

private fun Modifier.legacyImeBack(focusConfig: TaskBottomSheetFocusConfig): Modifier =
    if (focusConfig.interceptsLegacyImeBack) {
        onInterceptKeyBeforeSoftKeyboard { event ->
            if (event.key != Key.Back) return@onInterceptKeyBeforeSoftKeyboard false
            if (event.type == KeyEventType.KeyUp) focusConfig.clearEditorFocus()
            true
        }
    } else {
        this
    }
