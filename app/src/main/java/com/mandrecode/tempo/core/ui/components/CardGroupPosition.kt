package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The corner where two cards of a connected group meet: tight, so the run reads as one piece. */
val ConnectedCardInnerRadius: Dp = 8.dp

private const val CONNECTED_CARD_MORPH_MILLIS = 200

/** The gap between cards of a connected group: the same narrow seam the connected button groups use. */
val ConnectedCardGap: Dp = 3.dp

/**
 * Where a card sits in a run of connected cards, the list's take on the Material 3 Expressive
 * connected button group: the run's two ends keep the card's own round corners, and every join
 * inside it is tight, so a list of tasks reads as one block cut into rows.
 */
enum class CardGroupPosition {
    SINGLE,
    FIRST,
    MIDDLE,
    LAST,
    ;

    /** The radius of the card's two top corners: [outer] at the start of a run, tight inside it. */
    fun topRadius(outer: Dp): Dp = if (this == SINGLE || this == FIRST) outer else ConnectedCardInnerRadius

    /** The radius of the card's two bottom corners: [outer] at the end of a run, tight inside it. */
    fun bottomRadius(outer: Dp): Dp = if (this == SINGLE || this == LAST) outer else ConnectedCardInnerRadius

    /** The card's shape: [outer] on the corners at the ends of the run, [ConnectedCardInnerRadius] at the joins. */
    fun shape(outer: Dp): RoundedCornerShape =
        RoundedCornerShape(topRadius(outer), topRadius(outer), bottomRadius(outer), bottomRadius(outer))

    /** [shape], with the corners easing when the card's place in its run changes. */
    @Composable
    fun animatedShape(outer: Dp): RoundedCornerShape {
        val spec = tween<Dp>(CONNECTED_CARD_MORPH_MILLIS)
        val top by animateDpAsState(topRadius(outer), spec, label = "groupTopRadius")
        val bottom by animateDpAsState(bottomRadius(outer), spec, label = "groupBottomRadius")
        return RoundedCornerShape(top, top, bottom, bottom)
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
