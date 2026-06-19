package com.mandrecode.tempo.infrastructure.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RequestCodeGeneratorTest {
    @Test
    fun `forTask returns value in task range`() {
        val code = RequestCodeGenerator.forTask(42L)
        assertThat(code).isAtLeast(0)
        assertThat(code).isLessThan(1_000_000)
    }

    @Test
    fun `forHabit returns value in habit range`() {
        val code = RequestCodeGenerator.forHabit(42L)
        assertThat(code).isAtLeast(1_000_000)
        assertThat(code).isLessThan(2_000_000)
    }

    @Test
    fun `forHabitChain returns value in chain range`() {
        val code = RequestCodeGenerator.forHabitChain(42L)
        assertThat(code).isAtLeast(2_000_000)
        assertThat(code).isLessThan(3_000_000)
    }

    @Test
    fun `forLiveActivity returns value in live activity range`() {
        val code = RequestCodeGenerator.forLiveActivity(42L)
        assertThat(code).isAtLeast(3_000_000)
        assertThat(code).isLessThan(4_000_000)
    }

    @Test
    fun `different IDs produce different codes within same type`() {
        assertThat(RequestCodeGenerator.forTask(1L)).isNotEqualTo(RequestCodeGenerator.forTask(2L))
        assertThat(RequestCodeGenerator.forHabit(1L)).isNotEqualTo(RequestCodeGenerator.forHabit(2L))
        assertThat(RequestCodeGenerator.forHabitChain(1L)).isNotEqualTo(RequestCodeGenerator.forHabitChain(2L))
    }

    @Test
    fun `same ID for different types produces different codes`() {
        val id = 42L
        val codes =
            setOf(
                RequestCodeGenerator.forTask(id),
                RequestCodeGenerator.forHabit(id),
                RequestCodeGenerator.forHabitChain(id),
                RequestCodeGenerator.forLiveActivity(id),
            )
        assertThat(codes).hasSize(4)
    }

    @Test
    fun `large Long IDs do not overflow`() {
        val largeId = Long.MAX_VALUE
        val code = RequestCodeGenerator.forTask(largeId)
        assertThat(code).isAtLeast(0)
        assertThat(code).isLessThan(1_000_000)
    }

    @Test
    fun `large Long ID for habit does not overflow`() {
        val largeId = 3_000_000_000L
        val code = RequestCodeGenerator.forHabit(largeId)
        assertThat(code).isAtLeast(1_000_000)
        assertThat(code).isLessThan(2_000_000)
    }

    @Test
    fun `IDs beyond Int MAX_VALUE are handled safely`() {
        val beyondIntMax = Int.MAX_VALUE.toLong() + 1
        val code = RequestCodeGenerator.forTask(beyondIntMax)
        assertThat(code).isAtLeast(0)
        assertThat(code).isLessThan(1_000_000)
    }

    @Test
    fun `zero ID produces valid codes`() {
        assertThat(RequestCodeGenerator.forTask(0L)).isEqualTo(0)
        assertThat(RequestCodeGenerator.forHabit(0L)).isEqualTo(1_000_000)
        assertThat(RequestCodeGenerator.forHabitChain(0L)).isEqualTo(2_000_000)
        assertThat(RequestCodeGenerator.forLiveActivity(0L)).isEqualTo(3_000_000)
    }

    @Test
    fun `consecutive IDs produce consecutive codes`() {
        assertThat(RequestCodeGenerator.forTask(5L)).isEqualTo(5)
        assertThat(RequestCodeGenerator.forTask(6L)).isEqualTo(6)
        assertThat(RequestCodeGenerator.forHabit(5L)).isEqualTo(1_000_005)
        assertThat(RequestCodeGenerator.forHabit(6L)).isEqualTo(1_000_006)
    }
}
