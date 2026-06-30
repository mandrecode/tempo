package com.mandrecode.tempo.core.ui.components

import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.activity.ExperimentalActivityApi
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val DISMISS_THRESHOLD_FRACTION = 0.3f
private const val SHEET_SCRIM_ALPHA = 0.32f
private val SHEET_SHADOW_ELEVATION = 1.dp

/**
 * A modal sheet that slides up from the bottom of the screen.
 * Keeps IME movement tied to the sheet surface while supporting guarded dismissal.
 */
@Composable
fun TempoModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnsavedChanges: Boolean = false,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    val showDiscardDialogState = remember { mutableStateOf(false) }
    val forceDismissState = remember { mutableStateOf(false) }
    val currentHasUnsavedChanges by rememberUpdatedState(hasUnsavedChanges)
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val scope = rememberCoroutineScope()

    val screenHeightPx =
        with(LocalDensity.current) {
            LocalConfiguration.current.screenHeightDp.dp
                .toPx()
        }
    val maxSheetHeight =
        with(LocalDensity.current) {
            LocalConfiguration.current.screenHeightDp.dp
        }
    val currentScreenHeightPx by rememberUpdatedState(screenHeightPx)
    val isExpandedToStatusBar = remember { mutableStateOf(false) }

    val offsetY = remember { Animatable(screenHeightPx) }
    val dismissing = remember { mutableStateOf(false) }

    val animateRestore: () -> Unit =
        remember {
            {
                scope.launch {
                    offsetY.animateTo(
                        0f,
                        animationSpec = tween(TempoMotionTokens.DURATION_SHEET_MILLIS),
                    )
                }
            }
        }

    val animateDismiss: () -> Unit =
        remember {
            {
                if (!dismissing.value) {
                    dismissing.value = true
                    scope.launch {
                        try {
                            offsetY.animateTo(
                                currentScreenHeightPx,
                                animationSpec = tween(TempoMotionTokens.DURATION_SHEET_MILLIS),
                            )
                        } finally {
                            currentOnDismiss()
                        }
                    }
                }
            }
        }

    val onRequestDismiss: () -> Unit =
        remember {
            {
                if (currentHasUnsavedChanges && !forceDismissState.value) {
                    animateRestore()
                    showDiscardDialogState.value = true
                } else {
                    animateDismiss()
                }
            }
        }

    if (showDiscardDialogState.value) {
        DiscardChangesConfirmDialog(
            onCancelDiscard = { showDiscardDialogState.value = false },
            onConfirmDiscard = {
                showDiscardDialogState.value = false
                forceDismissState.value = true
                animateDismiss()
            },
        )
    }

    LaunchedEffect(Unit) {
        offsetY.animateTo(0f, animationSpec = tween(TempoMotionTokens.DURATION_SHEET_MILLIS))
    }

    Dialog(
        onDismissRequest = onRequestDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    ) {
        TempoModalBottomSheetPredictiveBackHandler(
            currentScreenHeightPx = currentScreenHeightPx,
            hasUnsavedChanges = currentHasUnsavedChanges,
            forceDismiss = forceDismissState.value,
            onProgress = { offsetY.snapTo(it.coerceIn(0f, currentScreenHeightPx)) },
            onRestore = {
                offsetY.animateTo(
                    0f,
                    animationSpec = tween(TempoMotionTokens.DURATION_SHEET_MILLIS),
                )
            },
            onDismiss = onRequestDismiss,
        )

        val isDarkTheme = LocalIsDarkTheme.current
        val view = LocalView.current
        val dialogWindow = (view.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { window ->
                window.setBackgroundDrawable(ColorDrawable(0))
                window.setDimAmount(0f)
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDarkTheme
                    isAppearanceLightNavigationBars = !isDarkTheme
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
            }
        }

        val focusManager = LocalFocusManager.current
        LaunchedEffect(dismissing.value) {
            if (dismissing.value) {
                focusManager.clearFocus()
            }
        }

        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = SHEET_SCRIM_ALPHA))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRequestDismiss,
                    ),
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxSheetHeight)
                        .onSizeChanged { size ->
                            isExpandedToStatusBar.value =
                                size.height >= currentScreenHeightPx.roundToInt()
                        }.align(Alignment.BottomCenter)
                        .offset { IntOffset(0, offsetY.value.roundToInt()) }
                        .imePadding()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ).pointerInput(currentScreenHeightPx) {
                            detectVerticalDragGestures(
                                onDragCancel = animateRestore,
                                onDragEnd = {
                                    if (offsetY.value > currentScreenHeightPx * DISMISS_THRESHOLD_FRACTION) {
                                        onRequestDismiss()
                                    } else {
                                        animateRestore()
                                    }
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    val newOffset =
                                        (offsetY.value + dragAmount)
                                            .coerceIn(0f, currentScreenHeightPx)
                                    scope.launch { offsetY.snapTo(newOffset) }
                                },
                            )
                        },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = SHEET_SHADOW_ELEVATION,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .then(
                                if (isExpandedToStatusBar.value) {
                                    Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                } else {
                                    Modifier
                                },
                            ).navigationBarsPadding(),
                ) {
                    TempoModalBottomSheetDragHandle()
                    content(onRequestDismiss)
                }
            }
        }
    }
}

@Composable
private fun TempoModalBottomSheetDragHandle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.size(width = 32.dp, height = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            shape = CircleShape,
        ) {}
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalActivityApi::class)
@Composable
private fun TempoModalBottomSheetPredictiveBackHandler(
    currentScreenHeightPx: Float,
    hasUnsavedChanges: Boolean,
    forceDismiss: Boolean,
    onProgress: suspend (Float) -> Unit,
    onRestore: suspend () -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    PredictiveBackHandler {
        try {
            it.collect { backEvent ->
                onProgress(currentScreenHeightPx * backEvent.progress)
            }
            if (hasUnsavedChanges && !forceDismiss) {
                onRestore()
            }
            onDismiss()
        } catch (exception: CancellationException) {
            scope.launch { onRestore() }
            throw exception
        }
    }
}
