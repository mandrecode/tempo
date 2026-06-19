package com.mandrecode.tempo.features.tasks.data.repository

import com.mandrecode.tempo.core.data.local.dao.CategoryDao
import com.mandrecode.tempo.features.tasks.data.mapper.toDomain
import com.mandrecode.tempo.features.tasks.data.mapper.toEntity
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl
    @Inject
    constructor(
        private val categoryDao: CategoryDao,
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

        override suspend fun deleteCategoryById(id: Long) = categoryDao.deleteCategoryById(id)

        override suspend fun getCategoryByName(name: String) = categoryDao.getCategoryByName(name)?.toDomain()

        override suspend fun clearAllDefaults() = categoryDao.clearAllDefaults()

        override suspend fun setDefaultCategory(id: Long) = categoryDao.setDefaultCategory(id)

        override suspend fun getMaxSortOrder(): Int = categoryDao.getMaxSortOrder() ?: 0
    }
