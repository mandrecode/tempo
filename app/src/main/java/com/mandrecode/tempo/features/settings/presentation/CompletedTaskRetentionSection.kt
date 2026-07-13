package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(SettingsSectionContentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.settings_remove_completed_after),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { onDaysChange(days - 1) },
            enabled = days > CompletedTaskRetentionPreferences.MIN_RETENTION_DAYS,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_remove),
                contentDescription = stringResource(R.string.settings_decrease_retention_days),
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = pluralStringResource(R.plurals.settings_retention_days, days, days),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(72.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
            onClick = { onDaysChange(days + 1) },
            enabled = days < CompletedTaskRetentionPreferences.MAX_RETENTION_DAYS,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.settings_increase_retention_days),
            )
        }
    }
}
