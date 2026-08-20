package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Modifier.stableImePadding(): Modifier {
    val current = WindowInsets.ime
    val animationSource = WindowInsets.imeAnimationSource
    val animationTarget = WindowInsets.imeAnimationTarget
    val stableInsets =
        remember(current, animationSource, animationTarget) {
            StableImeInsets(current, animationSource, animationTarget)
        }
    return this.windowInsetsPadding(stableInsets)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun rememberSheetPredictiveBackEnabled(): Boolean {
    val isImeVisible = WindowInsets.isImeVisible
    val imeAnimationTarget = WindowInsets.imeAnimationTarget
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val wasImeVisible = remember { mutableStateOf(isImeVisible) }

    LaunchedEffect(isImeVisible, imeAnimationTarget, density, focusManager) {
        if (isImeVisible) {
            wasImeVisible.value = true
        } else if (wasImeVisible.value) {
            snapshotFlow { imeAnimationTarget.getBottom(density) }
                .first { targetBottom ->
                    shouldClearSheetFocusAfterImeChange(
                        wasImeVisible = wasImeVisible.value,
                        isImeVisible = false,
                        animationTarget = targetBottom,
                    )
                }
            focusManager.clearFocus(force = true)
            wasImeVisible.value = false
        }
    }

    return shouldHandleSheetPredictiveBack(
        isImeVisible = isImeVisible,
        wasImeVisible = wasImeVisible.value,
    )
}

internal fun shouldHandleSheetPredictiveBack(
    isImeVisible: Boolean,
    wasImeVisible: Boolean,
): Boolean = !isImeVisible && !wasImeVisible

internal fun shouldClearSheetFocusAfterImeChange(
    wasImeVisible: Boolean,
    isImeVisible: Boolean,
    animationTarget: Int,
): Boolean = wasImeVisible && !isImeVisible && animationTarget == 0

internal fun stableImeInset(
    current: Int,
    animationSource: Int,
    animationTarget: Int,
): Int = maxOf(current, minOf(animationSource, animationTarget))

/**
 * Keeps the overlap between IME animation endpoints during editor-to-editor handoffs. Delegating
 * reads to layout avoids recomposing the sheet for every frame of a normal IME animation.
 */
@Stable
private class StableImeInsets(
    private val current: WindowInsets,
    private val animationSource: WindowInsets,
    private val animationTarget: WindowInsets,
) : WindowInsets {
    override fun getLeft(
        density: Density,
        layoutDirection: LayoutDirection,
    ): Int =
        stableImeInset(
            current = current.getLeft(density, layoutDirection),
            animationSource = animationSource.getLeft(density, layoutDirection),
            animationTarget = animationTarget.getLeft(density, layoutDirection),
        )

    override fun getTop(density: Density): Int =
        stableImeInset(
            current = current.getTop(density),
            animationSource = animationSource.getTop(density),
            animationTarget = animationTarget.getTop(density),
        )

    override fun getRight(
        density: Density,
        layoutDirection: LayoutDirection,
    ): Int =
        stableImeInset(
            current = current.getRight(density, layoutDirection),
            animationSource = animationSource.getRight(density, layoutDirection),
            animationTarget = animationTarget.getRight(density, layoutDirection),
        )

    override fun getBottom(density: Density): Int =
        stableImeInset(
            current = current.getBottom(density),
            animationSource = animationSource.getBottom(density),
            animationTarget = animationTarget.getBottom(density),
        )
}
