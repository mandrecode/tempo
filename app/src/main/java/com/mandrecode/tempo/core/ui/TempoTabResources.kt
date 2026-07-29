package com.mandrecode.tempo.core.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.TempoTab

/**
 * The label and icons for each tab, in one place, so the navigation bar, the navigation rail and
 * the Settings tab list all read from the same source instead of restating it three times.
 */
@get:StringRes
val TempoTab.titleRes: Int
    get() =
        when (this) {
            TempoTab.FOCUS -> R.string.focus
            TempoTab.ROUTINES -> R.string.routines
            TempoTab.TASKS -> R.string.tasks
        }

@get:DrawableRes
val TempoTab.selectedIconRes: Int
    get() =
        when (this) {
            TempoTab.FOCUS -> R.drawable.ic_focus
            TempoTab.ROUTINES -> R.drawable.ic_routine
            TempoTab.TASKS -> R.drawable.ic_tasks
        }

@get:DrawableRes
val TempoTab.outlinedIconRes: Int
    get() =
        when (this) {
            TempoTab.FOCUS -> R.drawable.ic_focus_outlined
            TempoTab.ROUTINES -> R.drawable.ic_routine_outlined
            TempoTab.TASKS -> R.drawable.ic_tasks_outlined
        }
