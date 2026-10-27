package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The corner where two cards of a connected group meet: tight, so the run reads as one piece. */
val ConnectedCardInnerRadius: Dp = 8.dp

/** The gap between cards of a connected group: the same narrow seam the connected button groups use. */
val ConnectedCardGap: Dp = 3.dp

/**
 * Where a card sits in a run of connected cards, the list's take on the Material 3 Expressive
 * connected button group: the run's two ends keep the card's own round corners, and every join
 * inside it is tight, so a list of tasks reads as one glass block cut into rows.
 */
enum class CardGroupPosition {
    SINGLE,
    FIRST,
    MIDDLE,
    LAST,
    ;

    /** The card's shape: [outer] on the corners at the ends of the run, [ConnectedCardInnerRadius] at the joins. */
    fun shape(outer: Dp): RoundedCornerShape =
        when (this) {
            SINGLE -> RoundedCornerShape(outer)
            FIRST -> RoundedCornerShape(outer, outer, ConnectedCardInnerRadius, ConnectedCardInnerRadius)
            MIDDLE -> RoundedCornerShape(ConnectedCardInnerRadius)
            LAST -> RoundedCornerShape(ConnectedCardInnerRadius, ConnectedCardInnerRadius, outer, outer)
        }

    companion object {
        /** The position of the card at [index] in a run of [count]. */
        fun of(
            index: Int,
            count: Int,
        ): CardGroupPosition =
            when {
                count <= 1 -> SINGLE
                index == 0 -> FIRST
                index == count - 1 -> LAST
                else -> MIDDLE
            }
    }
}
