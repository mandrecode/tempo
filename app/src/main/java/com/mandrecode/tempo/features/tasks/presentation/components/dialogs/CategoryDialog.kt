package com.mandrecode.tempo.features.tasks.presentation.components.dialogs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.theme.dialogTitle
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.features.tasks.domain.model.Category

@Composable
fun CategoryDialog(
    title: String,
    onDismiss: () -> Unit,
    onClearError: () -> Unit,
    category: Category? = null,
    error: String? = null,
    onConfirm: (String) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var categoryName by remember { mutableStateOf(category?.name ?: "") }
    var isNameError by remember { mutableStateOf(false) }

    val categoryNameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        categoryNameFocusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            shape = RoundedCornerShape(24.dp), // Consistent with app's rounded design
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat design
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(all = 24.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.dialogTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        isNameError = false
                        onClearError()
                    },
                    label = { Text(stringResource(R.string.category_name_label)) },
                    placeholder = { Text(stringResource(R.string.category_placeholder)) },
                    isError = isNameError || error != null,
                    supportingText =
                        if (isNameError || error != null) {
                            { Text(error ?: stringResource(R.string.msg_category_name_required)) }
                        } else {
                            null
                        },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(categoryNameFocusRequester),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val (cancelInteractionSource, cancelCornerRadius) = rememberPressableButtonAnimation()

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
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

                    val (confirmInteractionSource, confirmCornerRadius) = rememberPressableButtonAnimation()
                    val confirmEnabled = categoryName.isNotBlank() && (category == null || categoryName != category.name)
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
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onConfirm(categoryName)
                            } else {
                                isNameError = true
                            }
                        },
                        shape = RoundedCornerShape(confirmCornerRadius.value),
                        enabled = confirmEnabled,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = confirmContainerColor,
                                contentColor = confirmContentColor,
                                disabledContainerColor = confirmContainerColor,
                                disabledContentColor = confirmContentColor,
                            ),
                        interactionSource = confirmInteractionSource,
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text(
                            if (category != null) {
                                stringResource(R.string.update_category)
                            } else {
                                stringResource(
                                    R.string.category_add_category,
                                )
                            },
                            style = MaterialTheme.typography.dialogAction,
                        )
                    }
                }
            }
        }
    }
}
