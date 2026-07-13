package com.mandrecode.tempo.features.tasks.data.repository

import androidx.room.withTransaction
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.CategoryDao
import com.mandrecode.tempo.core.data.local.dao.TaskDao
import com.mandrecode.tempo.features.tasks.data.mapper.toDomain
import com.mandrecode.tempo.features.tasks.data.mapper.toEntity
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.CategoryDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl
    @Inject
    constructor(
        private val categoryDao: CategoryDao,
        private val taskDao: TaskDao,
        private val database: TempoDatabase,
    ) : CategoryRepository {
        override fun getAllCategories() = categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }

        override suspend fun getCategoryById(id: Long) = categoryDao.getCategoryById(id)?.toDomain()

        override suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category.toEntity())

        override suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category.toEntity())

        override suspend fun updateCategories(categories: List<Category>) {
            val entities = categories.map { it.toEntity() }
            categoryDao.updateCategories(entities)
        }

        override suspend fun getCategoryCount() = categoryDao.getCategoryCount()

        override suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category.toEntity())

        override suspend fun deleteCategoryWithSnapshot(category: Category): CategoryDeletionSnapshot =
            database.withTransaction {
                val persistedCategory = requireNotNull(categoryDao.getCategoryById(category.id))
                val tasks = taskDao.getTasksByCategoryId(category.id)
                taskDao.deleteTasksByCategoryId(category.id)
                categoryDao.deleteCategoryById(category.id)
                CategoryDeletionSnapshot(
                    category = persistedCategory.toDomain(),
                    tasks = tasks.map { it.toDomain() },
                )
            }

        override suspend fun restoreDeletedCategory(snapshot: CategoryDeletionSnapshot) {
            database.withTransaction {
                val categoryEntity = snapshot.category.toEntity()
                if (categoryDao.getCategoryById(snapshot.category.id) == null) {
                    categoryDao.insertCategory(categoryEntity)
                } else {
                    categoryDao.updateCategory(categoryEntity)
                }
                snapshot.tasks
                    .sortedBy { it.parentTaskId != null }
                    .forEach { task ->
                        val entity = task.toEntity()
                        if (taskDao.getTaskById(task.id) == null) {
                            taskDao.insertTask(entity)
                        } else {
                            taskDao.updateTask(entity)
                        }
                    }
            }
        }

        override suspend fun deleteCategoryById(id: Long) = categoryDao.deleteCategoryById(id)

        override suspend fun getCategoryByName(name: String) = categoryDao.getCategoryByName(name)?.toDomain()

        override suspend fun clearAllDefaults() = categoryDao.clearAllDefaults()

        override suspend fun setDefaultCategory(id: Long) = categoryDao.setDefaultCategory(id)

        override suspend fun getMaxSortOrder(): Int = categoryDao.getMaxSortOrder() ?: 0
    }
