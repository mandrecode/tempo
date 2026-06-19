package com.mandrecode.tempo.infrastructure.permissions

interface PermissionChecker {
    fun hasNotificationPermissions(): Boolean

    fun canScheduleExactAlarms(): Boolean
}
