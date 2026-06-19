package com.mandrecode.tempo.infrastructure.permissions

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionCheckerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : PermissionChecker {
        override fun hasNotificationPermissions(): Boolean = context.hasNotificationPermissions()

        override fun canScheduleExactAlarms(): Boolean = context.canScheduleExactAlarms()
    }
