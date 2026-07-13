package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences

@Composable
internal fun CompletedTasksSection(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_completed_tasks)) {
        Column {
            SettingsSwitchItem(
                icon = R.drawable.ic_remove_done,
                title = stringResource(R.string.settings_auto_remove_completed_tasks),
                checked = uiState.autoRemoveCompletedTasksEnabled,
                onCheckedChange = {
                    onEvent(SettingsContract.UiEvent.AutoRemoveCompletedTasksToggled(it))
                },
            )
            AnimatedVisibility(
                visible = uiState.autoRemoveCompletedTasksEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    SettingsItemDivider()
                    RetentionDaysStepper(
                        days = uiState.completedTaskRetentionDays,
                        onDaysChange = {
                            onEvent(SettingsContract.UiEvent.CompletedTaskRetentionDaysChanged(it))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RetentionDaysStepper(
    days: Int,
    onDaysChange: (Int) -> Unit,
) {
    val presets = CompletedTaskRetentionPreferences.supportedRetentionDays
    val selectedDays = CompletedTaskRetentionPreferences.normalizeRetentionDays(days)
    val selectedIndex = presets.indexOf(selectedDays)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(SettingsSectionContentPadding)
                .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.settings_remove_completed_after),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { onDaysChange(presets[selectedIndex - 1]) },
            enabled = selectedIndex > 0,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_remove),
                contentDescription = stringResource(R.string.settings_decrease_retention_days),
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        AnimatedContent(
            targetState = selectedDays,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally { width -> direction * width } + fadeIn()) togetherWith
                    (slideOutHorizontally { width -> -direction * width } + fadeOut()) using
                    SizeTransform(clip = false)
            },
            contentAlignment = Alignment.Center,
            label = "retentionDays",
            modifier = Modifier.width(72.dp),
        ) { animatedDays ->
            Text(
                text = pluralStringResource(R.plurals.settings_retention_days, animatedDays, animatedDays),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
            onClick = { onDaysChange(presets[selectedIndex + 1]) },
            enabled = selectedIndex < presets.lastIndex,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.settings_increase_retention_days),
            )
        }
    }
}
