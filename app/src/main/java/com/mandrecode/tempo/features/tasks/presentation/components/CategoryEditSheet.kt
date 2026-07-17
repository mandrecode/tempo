package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.core.ui.components.ColorPicker
import com.mandrecode.tempo.core.ui.components.IconPicker
import com.mandrecode.tempo.core.ui.components.TempoModalTopSheet
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.TempoIcon
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.theme.getMaterialYouColors
import com.mandrecode.tempo.core.ui.theme.getMonochromeColor
import com.mandrecode.tempo.core.ui.theme.getPastelColors
import com.mandrecode.tempo.core.ui.theme.inputTitle
import com.mandrecode.tempo.core.ui.theme.resolveColor
import com.mandrecode.tempo.core.ui.theme.resolveColorToKey
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.core.ui.util.rememberPressableIconButtonAnimation
import com.mandrecode.tempo.core.ui.util.selectRandomColor
import com.mandrecode.tempo.features.tasks.domain.model.Category

internal const val CATEGORY_EDIT_SHEET_NAME_FIELD_TEST_TAG = "category_edit_sheet_name_field"

@Composable
fun CategoryEditSheet(
    category: Category?,
    categories: List<Category>,
    nameError: Int?,
    onDismiss: () -> Unit,
    onSave: (name: String, color: String?, icon: String?, isDefault: Boolean) -> Unit,
    onDelete: (() -> Unit)?,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = LocalIsDarkTheme.current
    val currentOnSave by rememberUpdatedState(onSave)
    val currentOnDelete by rememberUpdatedState(onDelete)
    val currentOnClearError by rememberUpdatedState(onClearError)

    val isEditing = category != null

    var nameField by remember(category?.id) {
        val text = category?.name ?: ""
        mutableStateOf(TextFieldValue(text, selection = TextRange(text.length)))
    }
    var selectedColorKey by remember(category?.id) { mutableStateOf(category?.color) }
    var selectedIconName by remember(category?.id) { mutableStateOf(category?.icon) }
    var selectedIsDefault by remember(category?.id) { mutableStateOf(category?.isDefault ?: false) }

    // Track auto-selected values to exclude from unsaved-changes detection
    var autoSelectedColor by remember(category?.id) { mutableStateOf<String?>(null) }
    var autoSelectedIcon by remember(category?.id) { mutableStateOf<String?>(null) }
    var userManuallySelectedIcon by remember(category?.id) { mutableStateOf(false) }

    // Auto-select random color for new categories
    LaunchedEffect(category?.id) {
        if (!isEditing && selectedColorKey == null) {
            val existingColors =
                categories.mapNotNull { cat ->
                    cat.color?.let { key -> resolveColor(key, colorScheme, isDarkTheme) }
                }
            val availableOptions = getMaterialYouColors(colorScheme) + getMonochromeColor() + getPastelColors()
            if (availableOptions.isNotEmpty()) {
                val selectedColor =
                    selectRandomColor(
                        availableOptions = availableOptions,
                        isDarkTheme = isDarkTheme,
                        existingColors = existingColors,
                    )
                resolveColorToKey(selectedColor, colorScheme, isDarkTheme)?.let {
                    autoSelectedColor = it
                    selectedColorKey = it
                }
            }
        }
    }

    // Pre-select "category" icon for new categories
    LaunchedEffect(category?.id) {
        if (!isEditing && selectedIconName == null) {
            autoSelectedIcon = TempoIcon.CATEGORY.iconName
            selectedIconName = TempoIcon.CATEGORY.iconName
        }
    }

    // Predictive icon selection based on name — only when icon is still auto-selected
    LaunchedEffect(nameField.text) {
        if (!isEditing && !userManuallySelectedIcon && nameField.text.isNotBlank()) {
            val suggestedIcon = TempoIcon.suggestIcon(nameField.text, context)
            if (suggestedIcon != null) {
                autoSelectedIcon = suggestedIcon.iconName
                selectedIconName = suggestedIcon.iconName
            }
        }
    }

    val hasChanges =
        remember(
            nameField.text,
            selectedColorKey,
            selectedIconName,
            selectedIsDefault,
            autoSelectedColor,
            autoSelectedIcon,
        ) {
            if (isEditing) {
                nameField.text != (category?.name ?: "") ||
                    selectedColorKey != category?.color ||
                    selectedIconName != category?.icon ||
                    selectedIsDefault != (category?.isDefault ?: false)
            } else {
                nameField.text.isNotBlank() ||
                    (selectedColorKey != null && selectedColorKey != autoSelectedColor) ||
                    (selectedIconName != null && selectedIconName != autoSelectedIcon)
            }
        }

    val saveEnabled = nameField.text.isNotBlank() && (hasChanges || !isEditing)

    val nameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        nameFocusRequester.requestFocus()
    }

    TempoModalTopSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        hasUnsavedChanges = hasChanges,
    ) { onRequestDismiss ->
        val focusManager = LocalFocusManager.current
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 32.dp),
        ) {
            TextField(
                value = nameField,
                onValueChange = {
                    if (it.text.length <= ValidationUtils.MAX_CATEGORY_NAME_LENGTH) {
                        nameField = it
                    }
                    currentOnClearError()
                },
                placeholder = {
                    Text(
                        if (isEditing) {
                            stringResource(R.string.edit_category)
                        } else {
                            stringResource(R.string.new_category)
                        },
                        style = MaterialTheme.typography.inputTitle,
                    )
                },
                textStyle = MaterialTheme.typography.inputTitle,
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(CATEGORY_EDIT_SHEET_NAME_FIELD_TEST_TAG)
                        .focusRequester(nameFocusRequester),
                singleLine = true,
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
                isError = nameError != null,
                supportingText =
                    if (nameError != null) {
                        { Text(stringResource(nameError)) }
                    } else {
                        null
                    },
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        contentDescription = stringResource(R.string.icon_label),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconPicker(
                    selectedIconName = selectedIconName,
                    onSelectIcon = {
                        selectedIconName = it
                        userManuallySelectedIcon = true
                    },
                    onClearIcon = {
                        selectedIconName = null
                        userManuallySelectedIcon = false
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                        contentDescription = stringResource(R.string.color_label),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ColorPicker(
                    selectedColorKey = selectedColorKey,
                    onSelectColorKey = { selectedColorKey = it },
                    onClearColor = { selectedColorKey = null },
                    modifier = Modifier.weight(1f),
                )
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(24.dp))

                if (selectedIsDefault) {
                    TextButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.category_is_default),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                } else {
                    val (defaultInteractionSource, defaultCornerRadius) =
                        rememberPressableButtonAnimation()

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIsDefault = true
                        },
                        modifier =
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .height(48.dp),
                        shape = RoundedCornerShape(defaultCornerRadius.value),
                        interactionSource = defaultInteractionSource,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.set_as_default),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isEditing && onDelete != null) {
                    val (deleteInteractionSource, deleteCornerRadius) =
                        rememberPressableIconButtonAnimation()

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentOnDelete?.invoke()
                        },
                        shape = RoundedCornerShape(deleteCornerRadius.value),
                        modifier = Modifier.height(48.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        interactionSource = deleteInteractionSource,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_forever),
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(0.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val (cancelInteractionSource, cancelCornerRadius) =
                        rememberPressableButtonAnimation()

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

                    val (saveInteractionSource, saveCornerRadius) =
                        rememberPressableButtonAnimation()

                    val saveContainerColor by animateColorAsState(
                        targetValue =
                            if (saveEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            },
                        animationSpec = tween(200),
                        label = "save_container_color",
                    )
                    val saveContentColor by animateColorAsState(
                        targetValue =
                            if (saveEnabled) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            },
                        animationSpec = tween(200),
                        label = "save_content_color",
                    )

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentOnSave(nameField.text.trim(), selectedColorKey, selectedIconName, selectedIsDefault)
                        },
                        shape = RoundedCornerShape(saveCornerRadius.value),
                        enabled = saveEnabled,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = saveContainerColor,
                                contentColor = saveContentColor,
                                disabledContainerColor = saveContainerColor,
                                disabledContentColor = saveContentColor,
                            ),
                        interactionSource = saveInteractionSource,
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text(
                            stringResource(R.string.save),
                            style = MaterialTheme.typography.dialogAction,
                        )
                    }
                }
            }
        }
    }
}
