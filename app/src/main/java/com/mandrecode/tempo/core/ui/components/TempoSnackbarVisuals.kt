package com.mandrecode.tempo.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.text.AnnotatedString

/**
 * One linked entity name (e.g. a category name) to render bold within a snackbar message,
 * composed as [prefixResId] text + bold [word] + [suffixResId] text — the snackbar analogue of
 * the prefix/suffix pattern [com.mandrecode.tempo.features.tasks.presentation.components.dialogs.DeleteCategoryDialog]
 * already uses for a `Text` composable.
 */
data class SnackbarBoldSegment(
    @StringRes val prefixResId: Int,
    val word: String,
    @StringRes val suffixResId: Int,
)

/**
 * [SnackbarVisuals] carrying an [annotatedMessage] with bold spans, since [message] itself is
 * required to stay a plain [String] by the [SnackbarVisuals] interface. [ExpressiveSnackbar]
 * renders [annotatedMessage] when present, falling back to plain [message] for every other
 * caller of `SnackbarHostState.showSnackbar`.
 */
data class TempoSnackbarVisuals(
    val annotatedMessage: AnnotatedString,
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val withDismissAction: Boolean = false,
) : SnackbarVisuals {
    override val message: String get() = annotatedMessage.text
}
