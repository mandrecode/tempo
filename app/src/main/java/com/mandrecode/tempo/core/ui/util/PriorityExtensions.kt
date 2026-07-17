package com.mandrecode.tempo.core.ui.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.PastelGreenDark
import com.mandrecode.tempo.core.ui.theme.PastelGreenLight
import com.mandrecode.tempo.core.ui.theme.PastelRedDark
import com.mandrecode.tempo.core.ui.theme.PastelRedLight
import com.mandrecode.tempo.core.ui.theme.PastelYellowDark
import com.mandrecode.tempo.core.ui.theme.PastelYellowLight

@get:StringRes
val Priority.titleResId: Int
    get() =
        when (this) {
            Priority.HIGH -> R.string.priority_high
            Priority.MEDIUM -> R.string.priority_medium
            Priority.LOW -> R.string.priority_low
        }

/**
 * Theme-aware priority accent color. Reuses the same tone 40 (light) / tone 80 (dark) pastel
 * palette as habit colors instead of fixed hex, so contrast holds in both themes.
 */
val Priority.color: Color
    @Composable
    @ReadOnlyComposable
    get() = priorityColor(this, LocalIsDarkTheme.current)

/** Pure lookup so priority color resolution is unit-testable without a composition. */
internal fun priorityColor(
    priority: Priority,
    isDarkTheme: Boolean,
): Color =
    when (priority) {
        Priority.HIGH -> if (isDarkTheme) PastelRedDark else PastelRedLight
        Priority.MEDIUM -> if (isDarkTheme) PastelYellowDark else PastelYellowLight
        Priority.LOW -> if (isDarkTheme) PastelGreenDark else PastelGreenLight
    }
