package com.mandrecode.tempo.core.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** WCAG contrast ratio between two opaque colours, from 1 (identical) to 21 (black on white). */
fun contrastRatio(
    first: Color,
    second: Color,
): Float {
    val a = first.luminance()
    val b = second.luminance()
    return (maxOf(a, b) + CONTRAST_OFFSET) / (minOf(a, b) + CONTRAST_OFFSET)
}

/**
 * This colour, kept recognisable but readable as text on [background]: it is nudged toward
 * [toward] (the theme's text colour) in small steps until it reaches [minRatio], and returned as
 * it is when it already does. A user-picked colour (a habit's) can be a pastel that is fine as a
 * fill and unreadable as small text on a light panel.
 */
fun Color.readableOn(
    background: Color,
    toward: Color,
    minRatio: Float = TEXT_CONTRAST_RATIO,
): Color {
    val opaqueBackground = background.compositeOver(Color.White)
    var candidate = compositeOver(opaqueBackground)
    var step = 0
    while (contrastRatio(candidate, opaqueBackground) < minRatio && step < READABLE_STEPS) {
        step++
        candidate = lerp(this, toward, step / READABLE_STEPS.toFloat()).compositeOver(opaqueBackground)
    }
    return candidate
}

/** WCAG AA for normal-size text. */
const val TEXT_CONTRAST_RATIO = 4.5f
private const val CONTRAST_OFFSET = 0.05f
private const val READABLE_STEPS = 10
