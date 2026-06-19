package com.mandrecode.tempo.core.ui.model

data class PermissionInfo(
    val hasNotifications: Boolean = true,
    val canScheduleAlarms: Boolean = true,
) {
    val areAllGranted: Boolean
        get() = hasNotifications && canScheduleAlarms
}
