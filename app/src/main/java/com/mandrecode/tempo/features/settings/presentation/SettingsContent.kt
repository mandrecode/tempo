package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.ui.components.ExpressiveChip
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.TempoDarkPrimary
import com.mandrecode.tempo.core.ui.theme.TempoLightPrimary
import com.mandrecode.tempo.core.ui.theme.settingsSectionTitle
import com.mandrecode.tempo.util.dynamicColorScheme
import com.mandrecode.tempo.util.supportsDynamicColor

@Composable
fun SettingsContent(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isDarkTheme = LocalIsDarkTheme.current
    val isPreview = LocalInspectionMode.current
    val navigationBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dynamicDotColor =
        remember(context, isDarkTheme, isPreview) {
            if (!isPreview && supportsDynamicColor) {
                dynamicColorScheme(context, isDark = isDarkTheme).primary
            } else {
                if (isDarkTheme) darkColorScheme().primary else lightColorScheme().primary
            }
        }
    val tempoDotColor =
        remember(isDarkTheme) {
            if (isDarkTheme) TempoDarkPrimary else TempoLightPrimary
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 24.dp + navigationBarBottomPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SettingsSection(title = stringResource(R.string.theme)) {
            Column(
                modifier = Modifier.padding(SettingsSectionContentPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SettingsSegmentedRow {
                    uiState.availableThemeModes.forEachIndexed { index, themeMode ->
                        val themeLabel =
                            when (themeMode) {
                                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                            }

                        SettingsSegment(
                            label = themeLabel,
                            isSelected = uiState.selectedThemeMode == themeMode,
                            onClick = { onEvent(SettingsContract.UiEvent.ThemeModeSelected(themeMode)) },
                            isFirst = index == 0,
                            isLast = index == uiState.availableThemeModes.size - 1,
                            modifier = Modifier.weight(1f),
                            icon = {
                                Icon(
                                    painter =
                                        painterResource(
                                            when (themeMode) {
                                                ThemeMode.LIGHT -> R.drawable.ic_light_mode
                                                ThemeMode.DARK -> R.drawable.ic_dark_mode
                                                ThemeMode.SYSTEM -> R.drawable.ic_sync
                                            },
                                        ),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                    }
                }

                if (uiState.selectedThemeMode == ThemeMode.SYSTEM) {
                    Text(
                        text = stringResource(R.string.theme_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }

        SettingsSection(title = stringResource(R.string.settings_color_scheme)) {
            SettingsSegmentedRow(
                modifier = Modifier.padding(SettingsSectionContentPadding),
            ) {
                SettingsSegment(
                    label = stringResource(R.string.settings_color_scheme_dynamic),
                    isSelected = !uiState.useTempoColors,
                    onClick = { onEvent(SettingsContract.UiEvent.TempoColorsToggled(false)) },
                    isFirst = true,
                    isLast = false,
                    modifier = Modifier.weight(1f),
                    icon = {
                        ColorDot(
                            color =
                                if (!uiState.useTempoColors) {
                                    LocalContentColor.current
                                } else {
                                    dynamicDotColor
                                },
                        )
                    },
                )
                SettingsSegment(
                    label = stringResource(R.string.settings_color_scheme_tempo),
                    isSelected = uiState.useTempoColors,
                    onClick = { onEvent(SettingsContract.UiEvent.TempoColorsToggled(true)) },
                    isFirst = false,
                    isLast = true,
                    modifier = Modifier.weight(1f),
                    icon = {
                        ColorDot(
                            color =
                                if (uiState.useTempoColors) {
                                    LocalContentColor.current
                                } else {
                                    tempoDotColor
                                },
                        )
                    },
                )
            }
        }

        SettingsSection(title = stringResource(R.string.settings_notifications)) {
            SettingsActionRow(
                icon = R.drawable.ic_notifications,
                title = stringResource(R.string.settings_notifications_subtitle),
                subtitle = stringResource(R.string.settings_notifications_description),
                trailingIcon = R.drawable.ic_open_in_new,
                onClick = { openNotificationSettings(context) },
            )
        }

        SettingsSection(title = stringResource(R.string.tabs_and_navigation)) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                SettingsSwitchRow(
                    icon = R.drawable.ic_routine_outlined,
                    title = stringResource(R.string.routines_tab),
                    isEnabled = uiState.isRoutinesTabEnabled,
                    onToggle = { onEvent(SettingsContract.UiEvent.RoutinesTabToggled(it)) },
                )
                SettingsItemDivider()
                SettingsSwitchRow(
                    icon = R.drawable.ic_tasks_outlined,
                    title = stringResource(R.string.tasks_tab),
                    isEnabled = uiState.isTasksTabEnabled,
                    onToggle = { onEvent(SettingsContract.UiEvent.TasksTabToggled(it)) },
                )
            }
        }

        if (uiState.isRoutinesTabEnabled && uiState.isTasksTabEnabled) {
            SettingsSection(title = stringResource(R.string.default_tab)) {
                SettingsSegmentedRow(
                    modifier = Modifier.padding(SettingsSectionContentPadding),
                ) {
                    SettingsSegment(
                        label = stringResource(R.string.routines),
                        isSelected = uiState.defaultTab == SettingsContract.DefaultTab.ROUTINES,
                        onClick = {
                            onEvent(
                                SettingsContract.UiEvent.DefaultTabSelected(
                                    SettingsContract.DefaultTab.ROUTINES,
                                ),
                            )
                        },
                        isFirst = true,
                        isLast = false,
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_routine_outlined),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                    SettingsSegment(
                        label = stringResource(R.string.tasks),
                        isSelected = uiState.defaultTab == SettingsContract.DefaultTab.TASKS,
                        onClick = {
                            onEvent(
                                SettingsContract.UiEvent.DefaultTabSelected(
                                    SettingsContract.DefaultTab.TASKS,
                                ),
                            )
                        },
                        isFirst = false,
                        isLast = true,
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_tasks_outlined),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }
        }

        SettingsSection(title = stringResource(R.string.settings_language)) {
            val currentLocale = LocalLocale.current.platformLocale
            val languageName = currentLocale.getDisplayName(currentLocale).replaceFirstChar { it.titlecase(currentLocale) }

            SettingsActionRow(
                icon = R.drawable.ic_language,
                title = stringResource(R.string.settings_language_subtitle),
                subtitle = languageName,
                trailingIcon = R.drawable.ic_open_in_new,
                onClick = { openLanguageSettings(context) },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SettingsSection(title = stringResource(R.string.about)) {
                Column(
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    SettingsActionRow(
                        icon = R.drawable.ic_star,
                        title = stringResource(R.string.review_app),
                        subtitle = stringResource(R.string.review_app_description),
                        trailingIcon = R.drawable.ic_chevron_right,
                        onClick = { openReview(context) },
                    )
                    SettingsItemDivider()
                    SettingsActionRow(
                        icon = R.drawable.ic_feedback,
                        title = stringResource(R.string.send_feedback),
                        subtitle = stringResource(R.string.send_feedback_description),
                        trailingIcon = R.drawable.ic_chevron_right,
                        onClick = { openFeedback(context, uiState.appVersion) },
                    )
                }
            }

            if (uiState.appVersion.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.version_format, uiState.appVersion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.settingsSectionTitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SettingsCard(content = content)
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        content()
    }
}

@Composable
private fun SettingsSegmentedRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        content = content,
    )
}

@Composable
private fun SettingsSegment(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
) {
    ExpressiveChip(
        label = label,
        isSelected = isSelected,
        onClick = onClick,
        isFirst = isFirst,
        isLast = isLast,
        modifier = modifier,
        height = 48.dp,
        horizontalPadding = 8.dp,
        icon = icon,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SettingsActionRow(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: Int? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        SettingsRowFrame(
            icon = icon,
            title = title,
            subtitle = subtitle,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: Int,
    title: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(SettingsItemPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconContainer(icon = icon, contentDescription = title)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
        )
    }
}

@Composable
private fun SettingsRowFrame(
    icon: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingIcon: Int? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(SettingsItemPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconContainer(icon = icon, contentDescription = title)

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        trailingIcon?.let {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                painter = painterResource(id = it),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SettingsIconContainer(
    icon: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier.size(44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
    }
}

private val SettingsSectionContentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)
private val SettingsItemPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)

@Composable
private fun SettingsItemDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = 76.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
    )
}

@Composable
internal fun ColorDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color),
    )
}
