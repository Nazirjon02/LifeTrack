package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.data.local.CategoryLocalDataSource
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.CategoryType
import tj.mahram.lifetrack.domain.repository.CategoryRepository

class CategoryRepositoryImpl(private val local: CategoryLocalDataSource) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> = local.getAllCategories()

    override fun getCategoriesByType(type: CategoryType): Flow<List<Category>> =
        local.getCategoriesByType(type)

    override suspend fun createCategory(category: Category) = local.insert(category)

    override suspend fun updateCategory(category: Category) {
        local.delete(category.id)
        local.insert(category)
    }

    override suspend fun deleteCategory(id: String) = local.delete(id)

    override suspend fun initDefaultCategories() = local.initDefaultsIfEmpty()
}
