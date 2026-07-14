package com.mandrecode.tempo.features.tasks.domain.repository

import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.CategoryDeletionSnapshot
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>

    suspend fun getCategoryById(id: Long): Category?

    suspend fun insertCategory(category: Category): Long

    suspend fun updateCategory(category: Category)

    suspend fun updateCategories(categories: List<Category>)

    suspend fun getCategoryCount(): Int

    suspend fun deleteCategory(category: Category)

    suspend fun deleteCategoryWithSnapshot(category: Category): CategoryDeletionSnapshot

    suspend fun restoreDeletedCategory(snapshot: CategoryDeletionSnapshot)

    suspend fun deleteCategoryById(id: Long)

    suspend fun getCategoryByName(name: String): Category?

    suspend fun clearAllDefaults()

    suspend fun setDefaultCategory(id: Long)

    suspend fun getMaxSortOrder(): Int
}
