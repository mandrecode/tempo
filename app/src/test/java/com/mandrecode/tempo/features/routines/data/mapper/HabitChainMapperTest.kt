package com.mandrecode.tempo.features.routines.data.mapper

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.HabitChainEntity
import com.mandrecode.tempo.core.data.entity.HabitChainMemberEntity
import com.mandrecode.tempo.core.data.entity.HabitChainWithMembers
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class HabitChainMapperTest {
    private val createdDate = LocalDateTime(2024, 1, 1, 0, 0)

    @Test
    fun `toDomain maps all fields correctly`() {
        val entity =
            HabitChainEntity(
                id = 1,
                title = "Morning Routine",
                description = "Wake up tasks",
                colorKey = "green",
                icon = "☀️",
                periodicReminder = LocalDateTime(2024, 6, 15, 7, 0),
                createdDate = createdDate,
                completionHistory = "2024-06-14",
                repeatDays =
                    setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.WEDNESDAY,
                    ),
            )
        val members =
            listOf(
                HabitChainMemberEntity(chainId = 1, habitId = 1, sortOrder = 0),
                HabitChainMemberEntity(chainId = 1, habitId = 2, sortOrder = 1),
                HabitChainMemberEntity(chainId = 1, habitId = 3, sortOrder = 2),
            )
        val withMembers = HabitChainWithMembers(chain = entity, members = members)

        val domain = withMembers.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.title).isEqualTo("Morning Routine")
        assertThat(domain.habitIds).isEqualTo(listOf(1L, 2L, 3L))
        assertThat(domain.repeatDays).containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val domain =
            HabitChain(
                id = 2,
                title = "Night Routine",
                createdDate = createdDate,
                repeatDays = setOf(DayOfWeek.SUNDAY),
            )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(2)
        assertThat(entity.repeatDays).containsExactly(DayOfWeek.SUNDAY)
    }

    @Test
    fun `toDomain handles null optional fields`() {
        val entity =
            HabitChainEntity(
                id = 1,
                title = "T",
                createdDate = createdDate,
                repeatDays = null,
                colorKey = null,
                icon = null,
                periodicReminder = null,
            )
        val withMembers = HabitChainWithMembers(chain = entity, members = emptyList())

        val domain = withMembers.toDomain()

        assertThat(domain.repeatDays).isNull()
        assertThat(domain.colorKey).isNull()
        assertThat(domain.icon).isNull()
        assertThat(domain.periodicReminder).isNull()
    }

    @Test
    fun `toMemberEntities creates correct member entities`() {
        val original =
            HabitChain(
                id = 5,
                title = "Workout Chain",
                description = "Full body",
                colorKey = "red",
                icon = "🏋️",
                habitIds = listOf(10L, 20L),
                createdDate = createdDate,
                completionHistory = "2024-01-01,2024-01-02",
                repeatDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
            )

        val members = original.toMemberEntities()

        assertThat(members).hasSize(2)
        assertThat(members[0]).isEqualTo(HabitChainMemberEntity(chainId = 5, habitId = 10, sortOrder = 0))
        assertThat(members[1]).isEqualTo(HabitChainMemberEntity(chainId = 5, habitId = 20, sortOrder = 1))
    }

    @Test
    fun `list toDomain maps all elements`() {
        val withMembersList =
            listOf(
                HabitChainWithMembers(
                    chain = HabitChainEntity(id = 1, title = "A", createdDate = createdDate),
                    members = emptyList(),
                ),
                HabitChainWithMembers(
                    chain = HabitChainEntity(id = 2, title = "B", createdDate = createdDate),
                    members = emptyList(),
                ),
            )

        assertThat(withMembersList.toDomain()).hasSize(2)
    }
}
