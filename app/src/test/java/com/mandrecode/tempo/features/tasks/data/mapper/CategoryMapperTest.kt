package com.mandrecode.tempo.features.tasks.data.mapper

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.CategoryEntity
import com.mandrecode.tempo.features.tasks.domain.model.Category
import org.junit.Test

class CategoryMapperTest {
    @Test
    fun `toDomain maps all fields correctly`() {
        val entity =
            CategoryEntity(
                id = 1,
                name = "Work",
                color = "blue",
                icon = "WORK",
                isDefault = true,
                sortOrder = 2,
            )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.name).isEqualTo("Work")
        assertThat(domain.color).isEqualTo("blue")
        assertThat(domain.icon).isEqualTo("WORK")
        assertThat(domain.isDefault).isTrue()
        assertThat(domain.sortOrder).isEqualTo(2)
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val domain =
            Category(
                id = 2,
                name = "Personal",
                color = "green",
                icon = "HOME",
                isDefault = false,
                sortOrder = 1,
            )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(2)
        assertThat(entity.name).isEqualTo("Personal")
        assertThat(entity.color).isEqualTo("green")
        assertThat(entity.icon).isEqualTo("HOME")
        assertThat(entity.isDefault).isFalse()
        assertThat(entity.sortOrder).isEqualTo(1)
    }

    @Test
    fun `round trip preserves all data`() {
        val original =
            Category(
                id = 5,
                name = "Shopping",
                color = "orange",
                icon = "LIST",
                isDefault = true,
                sortOrder = 3,
            )

        val result = original.toEntity().toDomain()

        assertThat(result).isEqualTo(original)
    }

    @Test
    fun `round trip preserves null optional fields`() {
        val original = Category(id = 6, name = "Simple")

        val result = original.toEntity().toDomain()

        assertThat(result).isEqualTo(original)
        assertThat(result.color).isNull()
        assertThat(result.icon).isNull()
    }

    @Test
    fun `list toDomain maps all elements`() {
        val entities =
            listOf(
                CategoryEntity(id = 1, name = "A"),
                CategoryEntity(id = 2, name = "B"),
                CategoryEntity(id = 3, name = "C"),
            )

        val domains = entities.toDomain()

        assertThat(domains).hasSize(3)
        assertThat(domains.map { it.name }).containsExactly("A", "B", "C")
    }
}
