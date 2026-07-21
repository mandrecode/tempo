package com.mandrecode.tempo.features.whatsnew.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoModalBottomSheet
import com.mandrecode.tempo.core.ui.theme.sheetTitle
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.features.whatsnew.presentation.model.WhatsNewEntry

@Composable
fun WhatsNewBottomSheet(
    entry: WhatsNewEntry,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TempoModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) { onRequestDismiss ->
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 32.dp),
        ) {
            Text(
                text =
                    stringResource(
                        R.string.whats_new_legend,
                        entry.versionName,
                        stringResource(entry.titleRes),
                    ),
                style = MaterialTheme.typography.sheetTitle,
            )
            Text(
                text = stringResource(entry.descriptionRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            GotItButton(
                onClick = onRequestDismiss,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
            )
        }
    }
}

@Composable
private fun GotItButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadius) = rememberPressableButtonAnimation()

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius.value),
        modifier = modifier.height(48.dp),
    ) {
        Text(stringResource(R.string.whats_new_got_it))
    }
}
