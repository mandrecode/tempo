package com.mandrecode.tempo.core.ui.util

internal fun Throwable.toUserFacingMessage(): String =
    localizedMessage?.takeIf { it.isNotBlank() }
        ?: message?.takeIf { it.isNotBlank() }
        ?: javaClass.simpleName.takeIf { it.isNotBlank() }
        ?: javaClass.name
