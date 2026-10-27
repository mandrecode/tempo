package com.mandrecode.tempo.core.ui.editor

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

/**
 * An editor row's "add" action — a reminder, an attachment — as a button as wide as its label,
 * starting where the other rows' controls start and as tall as they are. It reads as something to
 * do rather than a value already set.
 */
@Composable
internal fun EditorAddButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    quiet: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadius) = rememberPressableButtonAnimation()
    val radius by cornerRadius
    val addShape = RoundedCornerShape(radius)
    if (quiet) {
        TextButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
            enabled = enabled,
            shape = addShape,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            interactionSource = interactionSource,
            modifier = modifier.height(EDITOR_CONTROL_HEIGHT),
        ) {
            AddButtonContent(label)
        }
        return
    }
    FilledTonalButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        shape = addShape,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        interactionSource = interactionSource,
        modifier = modifier.height(EDITOR_CONTROL_HEIGHT),
    ) {
        AddButtonContent(label)
    }
}

@Composable
private fun RowScope.AddButtonContent(label: String) {
    Icon(
        painter = painterResource(R.drawable.ic_add),
        contentDescription = null,
        modifier = Modifier.size(ButtonDefaults.IconSize),
    )
    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
    Text(text = label, style = MaterialTheme.typography.labelLarge)
}
