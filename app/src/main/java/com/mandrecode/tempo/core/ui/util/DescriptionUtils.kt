package com.mandrecode.tempo.core.ui.util

/**
 * Strips trailing whitespace from each line and removes blank lines
 * so descriptions render consistently in card composables.
 */
fun sanitizeDescription(description: String): String =
    description
        .lines()
        .map { it.trimEnd() }
        .filter { it.isNotBlank() }
        .joinToString("\n")
