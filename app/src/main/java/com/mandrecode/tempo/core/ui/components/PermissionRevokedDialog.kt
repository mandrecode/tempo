package com.mandrecode.tempo.core.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.model.PermissionInfo
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.theme.dialogTitle

@Composable
fun PermissionRevokedDialog(
    permissionInfo: PermissionInfo,
    context: Context,
    dismissPermissionRevokedDialog: () -> Unit,
    confirmClearAllReminders: () -> Unit,
    @StringRes notificationPrefixRes: Int = R.string.permission_revoked_notifications_prefix,
    @StringRes fallbackRes: Int = R.string.permission_revoked_fallback,
) {
    val (dialogText, intentAction) =
        remember(permissionInfo, notificationPrefixRes, fallbackRes) {
            when {
                // Case 1: Notification permission is missing
                !permissionInfo.hasNotifications && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    val text =
                        buildAnnotatedString {
                            append(context.getString(notificationPrefixRes))
                            withStyle(
                                style =
                                    SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            ) {
                                append(context.getString(R.string.permission_revoked_notifications_bold))
                            }
                            append(context.getString(R.string.permission_revoked_notifications_suffix))
                        }
                    val action =
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                    Pair(text, action)
                }

                // Case 2: Alarms permission is missing
                !permissionInfo.canScheduleAlarms && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val text =
                        buildAnnotatedString {
                            append(context.getString(R.string.permission_revoked_alarms_prefix))
                            withStyle(
                                style =
                                    SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            ) {
                                append(context.getString(R.string.permission_revoked_alarms_bold))
                            }
                            append(context.getString(R.string.permission_revoked_notifications_suffix))
                        }
                    val action =
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    Pair(text, action)
                }

                // Fallback
                else -> {
                    val text =
                        buildAnnotatedString {
                            append(context.getString(fallbackRes))
                        }
                    val action =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    Pair(text, action)
                }
            }
        }

    val confirmInteractionSource = remember { MutableInteractionSource() }
    val isConfirmPressed by confirmInteractionSource.collectIsPressedAsState()
    val confirmCornerRadius by animateDpAsState(
        targetValue = if (isConfirmPressed) 12.dp else 24.dp,
        animationSpec = tween(150),
        label = "confirm_corner_radius",
    )
    val dismissInteractionSource = remember { MutableInteractionSource() }
    val isDismissPressed by dismissInteractionSource.collectIsPressedAsState()
    val dismissCornerRadius by animateDpAsState(
        targetValue = if (isDismissPressed) 12.dp else 24.dp,
        animationSpec = tween(150),
        label = "dismiss_corner_radius",
    )

    AlertDialog(
        onDismissRequest = {},
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        title = {
            Text(
                text = stringResource(R.string.permissions_needed),
                style = MaterialTheme.typography.dialogTitle,
            )
        },
        text = { Text(dialogText) },
        confirmButton = {
            Button(
                onClick = {
                    context.startActivity(intentAction)
                    dismissPermissionRevokedDialog()
                },
                interactionSource = confirmInteractionSource,
                shape = RoundedCornerShape(confirmCornerRadius),
            ) {
                Text(
                    text = stringResource(R.string.go_to_settings),
                    style = MaterialTheme.typography.dialogAction,
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = confirmClearAllReminders,
                interactionSource = dismissInteractionSource,
                shape = RoundedCornerShape(dismissCornerRadius),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Text(stringResource(R.string.clear_all_reminders))
            }
        },
    )
}
