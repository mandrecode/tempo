package com.mandrecode.tempo.features.routines.presentation.components.dialogs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoConfirmDialog
import com.mandrecode.tempo.features.routines.domain.model.HabitChain

@Composable
fun EmptyHabitChainConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    habitChain: HabitChain?,
    modifier: Modifier = Modifier,
) {
    TempoConfirmDialog(
        title = stringResource(R.string.empty_habit_chain_title),
        confirmLabel = stringResource(R.string.delete),
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier,
        text = {
            val habitChainTitle = habitChain?.title ?: stringResource(R.string.empty_habit_chain_fallback)
            Text(
                buildAnnotatedString {
                    val messagePrefix = stringResource(R.string.empty_habit_chain_message_prefix).trimEnd()
                    append(messagePrefix)
                    append(" ")
                    withStyle(
                        style =
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                            ),
                    ) {
                        append(habitChainTitle)
                    }
                    append(stringResource(R.string.empty_habit_chain_message_suffix).trimStart())
                },
            )
        },
    )
}
