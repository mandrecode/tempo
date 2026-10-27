package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CardGroupPositionTest {
    @Test
    fun givenOneCard_whenPositioning_thenItStandsAlone() {
        assertThat(CardGroupPosition.of(index = 0, count = 1)).isEqualTo(CardGroupPosition.SINGLE)
    }

    @Test
    fun givenARun_whenPositioning_thenEndsAndMiddleAreTold() {
        assertThat((0 until 4).map { CardGroupPosition.of(it, 4) })
            .containsExactly(
                CardGroupPosition.FIRST,
                CardGroupPosition.MIDDLE,
                CardGroupPosition.MIDDLE,
                CardGroupPosition.LAST,
            ).inOrder()
    }

    @Test
    fun givenTwoCards_whenPositioning_thenThereIsNoMiddle() {
        assertThat((0 until 2).map { CardGroupPosition.of(it, 2) })
            .containsExactly(CardGroupPosition.FIRST, CardGroupPosition.LAST)
            .inOrder()
    }

    @Test
    fun givenTheFirstCard_whenShaping_thenOnlyTheTopIsRound() {
        val shape = CardGroupPosition.FIRST.shape(outer = 32.dp)

        assertThat(shape).isEqualTo(
            RoundedCornerShape(
                topStart = CornerSize(32.dp),
                topEnd = CornerSize(32.dp),
                bottomEnd = CornerSize(ConnectedCardInnerRadius),
                bottomStart = CornerSize(ConnectedCardInnerRadius),
            ),
        )
    }

    @Test
    fun givenTheLastCard_whenShaping_thenOnlyTheBottomIsRound() {
        val shape = CardGroupPosition.LAST.shape(outer = 32.dp)

        assertThat(shape).isEqualTo(
            RoundedCornerShape(
                topStart = CornerSize(ConnectedCardInnerRadius),
                topEnd = CornerSize(ConnectedCardInnerRadius),
                bottomEnd = CornerSize(32.dp),
                bottomStart = CornerSize(32.dp),
            ),
        )
    }

    @Test
    fun givenAMiddleOrSingleCard_whenShaping_thenCornersAreUniform() {
        assertThat(CardGroupPosition.MIDDLE.shape(32.dp)).isEqualTo(RoundedCornerShape(ConnectedCardInnerRadius))
        assertThat(CardGroupPosition.SINGLE.shape(32.dp)).isEqualTo(RoundedCornerShape(32.dp))
    }
}
