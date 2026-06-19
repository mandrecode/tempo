package com.mandrecode.tempo.core.ui.util

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.Priority

@get:StringRes
val Priority.titleResId: Int
    get() =
        when (this) {
            Priority.HIGH -> R.string.priority_high
            Priority.MEDIUM -> R.string.priority_medium
            Priority.LOW -> R.string.priority_low
        }

val Priority.color: Color
    get() =
        when (this) {
            Priority.HIGH -> Color(0xFFFF6961) // Pastel Red
            Priority.MEDIUM -> Color(0xFFFDFD96) // Pastel Yellow
            Priority.LOW -> Color(0xFF77DD77) // Pastel Green
        }
