package com.mandrecode.tempo.core.ui.components

import androidx.compose.ui.unit.Dp

/**
 * How many [itemSize] items (with at least [minGap] between neighbors) fit across
 * [availableWidth] after subtracting [horizontalPadding], clamped to [minCount]..[maxCount].
 * The clamp is a hard floor/ceiling, not just a hint: on a container narrower than [minCount]
 * items actually need, the result still won't drop below [minCount] (the caller's row may need
 * to wrap or compress rather than shrink further). Shared by [IconPicker] and [ColorPicker] so
 * both rows grow on wider containers instead of always showing a fixed count.
 */
internal fun calculateAdaptiveItemCount(
    availableWidth: Dp,
    itemSize: Dp,
    minGap: Dp,
    horizontalPadding: Dp,
    minCount: Int,
    maxCount: Int,
): Int {
    require(maxCount >= minCount) { "maxCount ($maxCount) must be >= minCount ($minCount)" }
    val usableWidth = availableWidth - horizontalPadding
    val itemSpan = itemSize + minGap
    val fitting = ((usableWidth + minGap).value / itemSpan.value).toInt()
    return fitting.coerceIn(minCount, maxCount)
}
