package com.mandrecode.tempo.features.settings.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.mandrecode.tempo.BuildConfig
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

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        // Theme Section
        SettingsSection(
            title = stringResource(R.string.theme),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    uiState.availableThemeModes.forEachIndexed { index, themeMode ->
                        val themeLabel =
                            when (themeMode) {
                                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                            }

                        ExpressiveChip(
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }

        // Color Scheme Section
        val isDarkTheme = LocalIsDarkTheme.current
        val dynamicDotColor =
            remember(context, isDarkTheme) {
                if (supportsDynamicColor) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_color_scheme),
                style = MaterialTheme.typography.settingsSectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                ExpressiveChip(
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
                ExpressiveChip(
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

        // Notifications Section
        SettingsSection(
            title = stringResource(R.string.settings_notifications),
        ) {
            SettingsItem(
                icon = R.drawable.ic_notifications,
                title = stringResource(R.string.settings_notifications_subtitle),
                subtitle = stringResource(R.string.settings_notifications_description),
                trailingIcon = R.drawable.ic_open_in_new,
                onClick = {
                    val intent =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        } else {
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        }
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.e("SettingsScreen", "Unable to open notification settings", e)
                    }
                },
            )
        }

        // Tabs & Navigation Section
        SettingsSection(
            title = stringResource(R.string.tabs_and_navigation),
        ) {
            Column {
                // Routines Toggle
                TabToggleItem(
                    icon = R.drawable.ic_routine_outlined,
                    label = stringResource(R.string.routines_tab),
                    isEnabled = uiState.isRoutinesTabEnabled,
                    onToggle = { onEvent(SettingsContract.UiEvent.RoutinesTabToggled(it)) },
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )

                // Tasks Toggle
                TabToggleItem(
                    icon = R.drawable.ic_tasks_outlined,
                    label = stringResource(R.string.tasks_tab),
                    isEnabled = uiState.isTasksTabEnabled,
                    onToggle = { onEvent(SettingsContract.UiEvent.TasksTabToggled(it)) },
                )
            }
        }

        // Default Tab — only shown when both tabs are enabled
        if (uiState.isRoutinesTabEnabled && uiState.isTasksTabEnabled) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.default_tab),
                    style = MaterialTheme.typography.settingsSectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    ExpressiveChip(
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
                    ExpressiveChip(
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

        // Language Section
        SettingsSection(
            title = stringResource(R.string.settings_language),
        ) {
            val currentLocale = LocalLocale.current.platformLocale
            val languageName = currentLocale.getDisplayName(currentLocale).replaceFirstChar { it.titlecase(currentLocale) }

            SettingsItem(
                icon = R.drawable.ic_language,
                title = stringResource(R.string.settings_language_subtitle),
                subtitle = languageName,
                trailingIcon = R.drawable.ic_open_in_new,
                onClick = {
                    val intent =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        } else {
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        }
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.e("SettingsScreen", "Unable to open language settings", e)
                    }
                },
            )
        }

        // About Section + Version
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsSection(
                title = stringResource(R.string.about),
            ) {
                Column {
                    // Review App
                    SettingsItem(
                        icon = R.drawable.ic_star,
                        title = stringResource(R.string.review_app),
                        subtitle = stringResource(R.string.review_app_description),
                        trailingIcon = R.drawable.ic_chevron_right,
                        onClick = {
                            val intent =
                                Intent(
                                    Intent.ACTION_VIEW,
                                    "market://details?id=${context.packageName}".toUri(),
                                )
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                // Play Store app not installed, try browser
                                val webIntent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "https://play.google.com/store/apps/details?id=${context.packageName}".toUri(),
                                    )
                                try {
                                    context.startActivity(webIntent)
                                } catch (e: ActivityNotFoundException) {
                                    Log.e("SettingsScreen", "Unable to open Play Store", e)
                                }
                            }
                        },
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 88.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    // Send Feedback
                    SettingsItem(
                        icon = R.drawable.ic_feedback,
                        title = stringResource(R.string.send_feedback),
                        subtitle = stringResource(R.string.send_feedback_description),
                        trailingIcon = R.drawable.ic_chevron_right,
                        onClick = {
                            val version = uiState.appVersion
                            val url =
                                "${BuildConfig.FEEDBACK_FORM_URL}&${BuildConfig.FEEDBACK_VERSION_ENTRY}=${Uri.encode(version)}"
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Log.e("SettingsScreen", "Unable to open feedback form", e)
                                @Suppress("LocalContextGetResourceValueCall")
                                Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.no_browser_app),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        },
                    )
                }
            }

            // Version info
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.settingsSectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: Int? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon container
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Trailing icon
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun TabToggleItem(
    icon: Int,
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
        )
    }
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
