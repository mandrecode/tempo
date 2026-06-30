package com.mandrecode.tempo.core.ui.components

import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.activity.ExperimentalActivityApi
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Box as LayoutBox

internal const val DISMISS_THRESHOLD_FRACTION = 0.3f
private const val SHEET_SCRIM_ALPHA = 0.32f
private val SHEET_SHADOW_ELEVATION = 1.dp
private val SHEET_HANDLE_TOUCH_TARGET_HEIGHT = 48.dp
private val SHEET_HANDLE_CONTENT_INSET = 24.dp
private val SHEET_HANDLE_EDGE_PADDING = 8.dp

internal enum class TempoModalSheetDirection {
    Top,
    Bottom,
}

@Composable
internal fun TempoModalSheet(
    direction: TempoModalSheetDirection,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnsavedChanges: Boolean = false,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    val state = rememberTempoModalSheetState(direction, onDismissRequest, hasUnsavedChanges)

    if (state.showDiscardDialog.value) {
        DiscardChangesConfirmDialog(
            onCancelDiscard = { state.showDiscardDialog.value = false },
            onConfirmDiscard = {
                state.showDiscardDialog.value = false
                state.forceDismiss.value = true
                state.animateDismiss()
            },
        )
    }

    LaunchedEffect(Unit) {
        state.offsetY.animateTo(0f, animationSpec = tween(TempoMotionTokens.DURATION_SHEET_MILLIS))
    }

    Dialog(
        onDismissRequest = state.onRequestDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    ) {
        TempoModalSheetDialogContent(
            state = state,
            modifier = modifier,
            content = content,
        )
    }
}

@Composable
private fun TempoModalSheetDialogContent(
    state: TempoModalSheetState,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    TempoModalSheetPredictiveBackHandler(
        hasUnsavedChanges = state.currentHasUnsavedChanges,
        forceDismiss = state.forceDismiss.value,
        onProgress = state::handlePredictiveBackProgress,
        onRestore = state::restore,
        onDismiss = state.onRequestDismiss,
    )
    TempoModalSheetWindowEffects()

    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.showDiscardDialog.value) {
        if (state.showDiscardDialog.value) {
            focusManager.clearFocus()
        }
    }
    LaunchedEffect(state.dismissing.value) {
        if (state.dismissing.value) {
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
                    onClick = state.onRequestDismiss,
                ),
    ) {
        TempoModalSheetSurface(
            state = state,
            content = content,
        )
    }
}

@Composable
private fun BoxScope.TempoModalSheetSurface(
    state: TempoModalSheetState,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(max = state.maxSheetHeight)
                .onSizeChanged { size ->
                    state.isExpandedToStatusBar.value =
                        size.height >= state.currentScreenHeightPx.roundToInt()
                }.align(state.direction.alignment)
                .offset { IntOffset(0, state.offsetY.value.roundToInt()) }
                .then(if (state.direction == TempoModalSheetDirection.Bottom) Modifier.imePadding() else Modifier),
        shape = state.direction.shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = SHEET_SHADOW_ELEVATION,
    ) {
        TempoModalSheetColumn(
            state = state,
            content = content,
        )
    }
}

@Composable
private fun TempoModalSheetColumn(
    state: TempoModalSheetState,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    LayoutBox(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(state.statusBarPaddingModifier)
                    .then(state.navigationBarPaddingModifier),
        ) {
            if (state.direction == TempoModalSheetDirection.Bottom) {
                Spacer(modifier = Modifier.height(SHEET_HANDLE_CONTENT_INSET))
            }
            content(state.onRequestDismiss)
            if (state.direction == TempoModalSheetDirection.Top) {
                Spacer(modifier = Modifier.height(SHEET_HANDLE_CONTENT_INSET))
            }
        }
        TempoModalSheetDragHandle(
            state = state,
            modifier =
                Modifier.align(
                    if (state.direction == TempoModalSheetDirection.Bottom) {
                        Alignment.TopCenter
                    } else {
                        Alignment.BottomCenter
                    },
                ),
        )
    }
}

@Composable
private fun rememberTempoModalSheetState(
    direction: TempoModalSheetDirection,
    onDismissRequest: () -> Unit,
    hasUnsavedChanges: Boolean,
): TempoModalSheetState {
    val currentHasUnsavedChanges by rememberUpdatedState(hasUnsavedChanges)
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val scope = rememberCoroutineScope()
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightPx = with(LocalDensity.current) { screenHeightDp.toPx() }
    val currentScreenHeightPx by rememberUpdatedState(screenHeightPx)
    val offsetY = remember { Animatable(direction.hiddenOffset(screenHeightPx)) }
    val predictiveBackProgress = remember { mutableFloatStateOf(0f) }
    val dismissing = remember { mutableStateOf(false) }
    val showDiscardDialog = remember { mutableStateOf(false) }
    val forceDismiss = remember { mutableStateOf(false) }
    val isExpandedToStatusBar = remember { mutableStateOf(false) }

    lateinit var state: TempoModalSheetState
    val animateRestore: () -> Unit = {
        scope.launch { state.restore() }
    }
    val animateDismiss: () -> Unit = {
        if (!dismissing.value) {
            dismissing.value = true
            scope.launch {
                try {
                    predictiveBackProgress.floatValue = 0f
                    offsetY.animateTo(
                        direction.hiddenOffset(currentScreenHeightPx),
                        animationSpec = tween(TempoMotionTokens.DURATION_SHEET_MILLIS),
                    )
                } finally {
                    currentOnDismiss()
                }
            }
        }
    }
    val onRequestDismiss: () -> Unit = {
        if (currentHasUnsavedChanges && !forceDismiss.value) {
            animateRestore()
            showDiscardDialog.value = true
        } else {
            animateDismiss()
        }
    }

    state =
        TempoModalSheetState(
            direction = direction,
            maxSheetHeight = remember(direction, screenHeightDp) { direction.maxHeight(screenHeightDp) },
            currentScreenHeightPx = currentScreenHeightPx,
            offsetY = offsetY,
            predictiveBackProgress = predictiveBackProgress,
            dismissing = dismissing,
            showDiscardDialog = showDiscardDialog,
            forceDismiss = forceDismiss,
            isExpandedToStatusBar = isExpandedToStatusBar,
            currentHasUnsavedChanges = currentHasUnsavedChanges,
            animateDismiss = animateDismiss,
            onRequestDismiss = onRequestDismiss,
        )
    return state
}

private val TempoModalSheetState.statusBarPaddingModifier: Modifier
    @Composable
    get() =
        if (direction == TempoModalSheetDirection.Top || isExpandedToStatusBar.value) {
            Modifier.windowInsetsPadding(WindowInsets.statusBars)
        } else {
            Modifier
        }

private val TempoModalSheetState.navigationBarPaddingModifier: Modifier
    @Composable
    get() =
        if (direction == TempoModalSheetDirection.Bottom) {
            Modifier.navigationBarsPadding()
        } else {
            Modifier
        }

private fun Modifier.pointerInputForSheetDrag(
    state: TempoModalSheetState,
    scope: CoroutineScope,
): Modifier =
    pointerInput(state.direction, state.currentScreenHeightPx) {
        detectSheetVerticalDragGestures(
            direction = state.direction,
            onDragEnd = {
                if (
                    state.direction.shouldDismiss(
                        offset = state.offsetY.value,
                        screenHeightPx = state.currentScreenHeightPx,
                    )
                ) {
                    state.onRequestDismiss()
                } else {
                    scope.launch { state.restore() }
                }
            },
            onDragCancel = { scope.launch { state.restore() } },
            onVerticalDrag = { dragAmount ->
                val newOffset =
                    state.direction.coerceDragOffset(
                        offset = state.offsetY.value + dragAmount,
                        screenHeightPx = state.currentScreenHeightPx,
                    )
                scope.launch { state.offsetY.snapTo(newOffset) }
            },
        )
    }

@Composable
private fun TempoModalSheetWindowEffects() {
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
}

@Composable
private fun TempoModalSheetDragHandle(
    state: TempoModalSheetState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val dismissSheetLabel = stringResource(R.string.dismiss_sheet)
    LayoutBox(
        modifier =
            modifier
                .fillMaxWidth()
                .height(SHEET_HANDLE_TOUCH_TARGET_HEIGHT)
                .semantics {
                    contentDescription = dismissSheetLabel
                    dismiss {
                        state.onRequestDismiss()
                        true
                    }
                }.pointerInputForSheetDrag(state, scope),
        contentAlignment =
            if (state.direction == TempoModalSheetDirection.Bottom) {
                Alignment.TopCenter
            } else {
                Alignment.BottomCenter
            },
    ) {
        Surface(
            modifier =
                Modifier
                    .then(
                        if (state.direction == TempoModalSheetDirection.Bottom) {
                            Modifier.padding(top = SHEET_HANDLE_EDGE_PADDING)
                        } else {
                            Modifier.padding(bottom = SHEET_HANDLE_EDGE_PADDING)
                        },
                    ).size(width = 32.dp, height = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            shape = CircleShape,
        ) {}
    }
}

@OptIn(ExperimentalActivityApi::class)
@Composable
private fun TempoModalSheetPredictiveBackHandler(
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
                onProgress(backEvent.progress)
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
