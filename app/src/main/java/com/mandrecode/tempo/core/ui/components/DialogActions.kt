package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.mandrecode.tempo.core.ui.glass.glassButtonContainer
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

/**
 * The confirming action of a dialog: a filled button whose corners pull in while held, with a haptic
 * on the tap, the same as the app's alert dialogs. Dialogs that are not an `AlertDialog` (the date
 * and time pickers) use it so they read as part of the same family.
 */
@Composable
fun DialogConfirmButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadius) = rememberPressableButtonAnimation()
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius.value),
        colors = colors,
    ) {
        Text(text = label, style = MaterialTheme.typography.dialogAction)
    }
}

/** The dismissing action of a dialog: the glass button beside [DialogConfirmButton]. */
@Composable
fun DialogCancelButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadius) = rememberPressableButtonAnimation()
    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = glassButtonContainer()),
        border = null,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius.value),
    ) {
        Text(text = label)
    }
}
