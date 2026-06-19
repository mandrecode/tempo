package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

@Composable
internal fun TaskBottomSheetFooter(
    isEditingTask: Boolean,
    taskTitle: String,
    autoSaveEnabled: Boolean,
    onDelete: (() -> Unit)?,
    onRequestDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val isEditingWithDelete = isEditingTask && onDelete != null

    Column {
        Spacer(modifier = Modifier.height(if (isEditingWithDelete) 20.dp else 40.dp))

        if (isEditingWithDelete) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                    },
                    shape = RoundedCornerShape(DELETE_BUTTON_CORNER_RADIUS),
                    color = Color.Transparent,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_forever),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.delete_task),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val (cancelInteractionSource, cancelCornerRadius) = rememberPressableButtonAnimation()

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onRequestDismiss()
                    },
                    shape = RoundedCornerShape(cancelCornerRadius.value),
                    modifier = Modifier.height(48.dp),
                    interactionSource = cancelInteractionSource,
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                if (!autoSaveEnabled) {
                    val (confirmInteractionSource, confirmCornerRadius) = rememberPressableButtonAnimation()
                    val confirmEnabled = taskTitle.isNotBlank()
                    val confirmContainerColor by animateColorAsState(
                        targetValue =
                            if (confirmEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            },
                        animationSpec = tween(200),
                        label = "confirm_container_color",
                    )
                    val confirmContentColor by animateColorAsState(
                        targetValue =
                            if (confirmEnabled) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            },
                        animationSpec = tween(200),
                        label = "confirm_content_color",
                    )

                    Button(
                        onClick = onConfirmClick,
                        shape = RoundedCornerShape(confirmCornerRadius.value),
                        enabled = confirmEnabled,
                        modifier = Modifier.height(48.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = confirmContainerColor,
                                contentColor = confirmContentColor,
                                disabledContainerColor = confirmContainerColor,
                                disabledContentColor = confirmContentColor,
                            ),
                        interactionSource = confirmInteractionSource,
                    ) {
                        Text(
                            if (isEditingTask) {
                                stringResource(R.string.update)
                            } else {
                                stringResource(R.string.add_task)
                            },
                            style = MaterialTheme.typography.dialogAction,
                        )
                    }
                }
            }
        }
    }
}
