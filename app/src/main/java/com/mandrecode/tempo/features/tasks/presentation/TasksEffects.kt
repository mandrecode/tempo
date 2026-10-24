package com.mandrecode.tempo.features.tasks.presentation

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.mandrecode.tempo.core.ui.components.TempoSnackbarVisuals

/**
 * Shows one [TasksViewModel] effect on [snackbarHostState] and reports the undo outcome back.
 *
 * Shared by Tasks and by Focus, which hosts its own Tasks view model for the task editor: a save
 * that fails after the form has closed still has to say so wherever the user is (#434).
 */
internal suspend fun showTasksEffect(
    effect: TasksContract.UiEffect,
    context: Context,
    snackbarHostState: SnackbarHostState,
    onEvent: (TasksContract.UiEvent) -> Unit,
) {
    when (effect) {
        is TasksContract.UiEffect.ShowSnackbar -> {
            val actionLabel = effect.actionResId?.let(context::getString)
            val result =
                snackbarHostState.showSnackbar(
                    visuals =
                        TempoSnackbarVisuals(
                            annotatedMessage = effect.toAnnotatedMessage(context),
                            actionLabel = actionLabel,
                            duration =
                                if (actionLabel == null) {
                                    SnackbarDuration.Short
                                } else {
                                    SnackbarDuration.Long
                                },
                        ),
                )
            effect.deletionToken?.let { token ->
                onEvent(
                    if (result == SnackbarResult.ActionPerformed) {
                        TasksContract.UiEvent.UndoDeletion(token)
                    } else {
                        TasksContract.UiEvent.DismissDeletionUndo(token)
                    },
                )
            }
        }
    }
}

@Suppress("LocalContextGetResourceValueCall")
private fun TasksContract.UiEffect.ShowSnackbar.toAnnotatedMessage(context: Context): AnnotatedString {
    val segment = boldSegment
    if (segment != null) {
        return buildAnnotatedString {
            append(context.getString(segment.prefixResId).trimEnd())
            append(" ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(segment.word)
            }
            append(context.getString(segment.suffixResId))
        }
    }
    val message =
        if (formatArgs.isNotEmpty()) {
            context.getString(requireNotNull(messageResId), *formatArgs.toTypedArray())
        } else {
            context.getString(requireNotNull(messageResId))
        }
    return AnnotatedString(message)
}
