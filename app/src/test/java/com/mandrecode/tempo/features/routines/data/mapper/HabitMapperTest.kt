package com.mandrecode.tempo.features.routines.data.mapper

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.HabitEntity
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class HabitMapperTest {
    private val createdDate = LocalDateTime(2024, 1, 1, 0, 0)

    @Test
    fun `toDomain maps all fields correctly`() {
        val entity =
            HabitEntity(
                id = 1,
                title = "Meditate",
                description = "10 min",
                icon = "🧘",
                colorKey = "blue",
                reminderDate = LocalDateTime(2024, 6, 15, 8, 0),
                isCompleted = true,
                createdDate = createdDate,
                completionHistory = "2024-06-14,2024-06-15",
                repeatDays =
                    setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.FRIDAY,
                    ),
            )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.title).isEqualTo("Meditate")
        assertThat(domain.icon).isEqualTo("🧘")
        assertThat(domain.colorKey).isEqualTo("blue")
        assertThat(domain.isCompleted).isTrue()
        assertThat(domain.repeatDays).containsExactly(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val domain =
            Habit(
                id = 2,
                title = "Read",
                description = "30 pages",
                createdDate = createdDate,
                repeatDays = setOf(DayOfWeek.SATURDAY),
            )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(2)
        assertThat(entity.title).isEqualTo("Read")
        assertThat(entity.repeatDays).containsExactly(DayOfWeek.SATURDAY)
    }

    @Test
    fun `toDomain handles null repeat days`() {
        val entity =
            HabitEntity(
                id = 1,
                title = "T",
                description = "D",
                createdDate = createdDate,
                repeatDays = null,
            )

        assertThat(entity.toDomain().repeatDays).isNull()
    }

    @Test
    fun `round trip preserves all data`() {
        val original =
            Habit(
                id = 3,
                title = "Exercise",
                description = "Gym",
                icon = "💪",
                colorKey = "red",
                createdDate = createdDate,
                completionHistory = "2024-01-01",
                repeatDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            )

        val result = original.toEntity().toDomain()

        assertThat(result).isEqualTo(original)
    }

    @Test
    fun `list toDomain maps all elements`() {
        val entities =
            listOf(
                HabitEntity(id = 1, title = "A", description = "", createdDate = createdDate),
                HabitEntity(id = 2, title = "B", description = "", createdDate = createdDate),
            )

        assertThat(entities.toDomain()).hasSize(2)
    }

    // --- habitType mapping ---

    @Test
    fun `toDomain maps BUILD habitType string`() {
        val entity =
            HabitEntity(
                id = 1,
                title = "T",
                description = "",
                createdDate = createdDate,
                habitType = "BUILD",
            )

        assertThat(entity.toDomain().habitType).isEqualTo(HabitType.BUILD)
    }

    @Test
    fun `toDomain maps QUIT habitType string`() {
        val entity =
            HabitEntity(
                id = 1,
                title = "T",
                description = "",
                createdDate = createdDate,
                habitType = "QUIT",
            )

        assertThat(entity.toDomain().habitType).isEqualTo(HabitType.QUIT)
    }

    @Test
    fun `toDomain falls back to BUILD when habitType is unknown`() {
        val entity =
            HabitEntity(
                id = 42,
                title = "T",
                description = "",
                createdDate = createdDate,
                habitType = "GIBBERISH",
            )

        // Contract: unknown serialized values fall back silently to BUILD so the
        // app stays usable on schema drift; see HabitMapper.parseHabitType.
        assertThat(entity.toDomain().habitType).isEqualTo(HabitType.BUILD)
    }

    @Test
    fun `toEntity serializes habitType to its enum name`() {
        val build =
            Habit(
                title = "B",
                description = "",
                createdDate = createdDate,
                habitType = HabitType.BUILD,
            ).toEntity()
        val quit =
            Habit(
                title = "Q",
                description = "",
                createdDate = createdDate,
                habitType = HabitType.QUIT,
            ).toEntity()

        assertThat(build.habitType).isEqualTo("BUILD")
        assertThat(quit.habitType).isEqualTo("QUIT")
    }

    @Test
    fun `round trip preserves QUIT habitType`() {
        val original =
            Habit(
                id = 7,
                title = "Quit Smoking",
                description = "",
                createdDate = createdDate,
                habitType = HabitType.QUIT,
            )

        val result = original.toEntity().toDomain()

        assertThat(result.habitType).isEqualTo(HabitType.QUIT)
        assertThat(result).isEqualTo(original)
    }
}
