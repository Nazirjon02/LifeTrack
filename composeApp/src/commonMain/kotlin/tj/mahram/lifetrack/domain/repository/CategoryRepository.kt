package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.CategoryType

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: CategoryType): Flow<List<Category>>
    suspend fun createCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: String)
    suspend fun initDefaultCategories()
    /** Insert [category] only if a row with its id does not already exist. */
    suspend fun ensureCategory(category: Category)
}
