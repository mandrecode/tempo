package com.mandrecode.tempo.core.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Spacing class to hold all standard spacing values
data class Spacing(
    val extraSmall: Dp = 2.dp,
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val default: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 64.dp,
)

// CompositionLocal to provide spacing throughout the app
@Suppress("ktlint:compose:compositionlocal-allowlist")
val LocalSpacing = compositionLocalOf { Spacing() }

// Extension property to easily access spacing from MaterialTheme
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

// Common padding values
object TempoSpacing {
    // Standard content padding
    val contentPadding =
        PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp,
        )

    // Standard list item padding
    val listItemPadding =
        PaddingValues(
            horizontal = 16.dp,
            vertical = 4.dp,
        )

    // Card content padding
    val cardContentPadding = 16.dp

    // Dialog content padding
    val dialogContentPadding = 16.dp

    // Bottom FAB padding
    val fabPadding = 16.dp

    // Bottom navigation height
    val bottomNavHeight = 80.dp

    // Standard floating action button spacing from bottom
    val fabBottomSpacing = 88.dp
}
