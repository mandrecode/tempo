package com.mandrecode.tempo.features.tasks.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.local.InMemoryTempoDatabaseRule
import com.mandrecode.tempo.features.tasks.domain.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryRepositoryRoomIntegrationTest {
    @get:Rule
    val databaseRule = InMemoryTempoDatabaseRule()

    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        repository = CategoryRepositoryImpl(databaseRule.database.categoryDao())
    }

    @Test
    fun insertAndFetchCategory_roundTripsThroughRoom() =
        runTest {
            val insertedId =
                repository.insertCategory(
                    Category(
                        name = "Work",
                        color = "blue",
                        icon = "work",
                        sortOrder = 3,
                    ),
                )

            val fetched = repository.getCategoryById(insertedId)

            assertThat(fetched).isNotNull()
            assertThat(fetched!!.name).isEqualTo("Work")
            assertThat(fetched.color).isEqualTo("blue")
            assertThat(fetched.icon).isEqualTo("work")
            assertThat(fetched.sortOrder).isEqualTo(3)
        }

    @Test
    fun updateAndSetDefaultCategory_persistsChanges() =
        runTest {
            val firstId = repository.insertCategory(Category(name = "Inbox", sortOrder = 1))
            val secondId = repository.insertCategory(Category(name = "Personal", sortOrder = 2))

            repository.updateCategory(Category(id = secondId, name = "Personal+", color = "green", isDefault = false, sortOrder = 5))
            repository.setDefaultCategory(secondId)

            val all = repository.getAllCategories().first()
            val updatedSecond = all.first { it.id == secondId }
            val updatedFirst = all.first { it.id == firstId }

            assertThat(updatedSecond.name).isEqualTo("Personal+")
            assertThat(updatedSecond.color).isEqualTo("green")
            assertThat(updatedSecond.isDefault).isTrue()
            assertThat(updatedFirst.isDefault).isFalse()
            assertThat(repository.getMaxSortOrder()).isEqualTo(5)
        }
}
