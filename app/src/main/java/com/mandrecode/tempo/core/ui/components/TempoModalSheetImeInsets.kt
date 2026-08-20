package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Modifier.stableImePadding(): Modifier {
    val density = LocalDensity.current
    val bottomInset =
        stableImeBottomInset(
            current = WindowInsets.ime.getBottom(density),
            animationSource = WindowInsets.imeAnimationSource.getBottom(density),
            animationTarget = WindowInsets.imeAnimationTarget.getBottom(density),
        )
    return windowInsetsPadding(WindowInsets(bottom = bottomInset))
}

internal fun stableImeBottomInset(
    current: Int,
    animationSource: Int,
    animationTarget: Int,
): Int = maxOf(current, minOf(animationSource, animationTarget))
