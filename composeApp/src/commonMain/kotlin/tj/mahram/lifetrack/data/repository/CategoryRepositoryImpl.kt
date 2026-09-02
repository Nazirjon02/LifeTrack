package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.data.local.CategoryLocalDataSource
import tj.mahram.lifetrack.data.sync.SyncCollectionNames
import tj.mahram.lifetrack.data.sync.SyncTracker
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.CategoryType
import tj.mahram.lifetrack.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val local: CategoryLocalDataSource,
    private val sync: SyncTracker
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> = local.getAllCategories()

    override fun getCategoriesByType(type: CategoryType): Flow<List<Category>> =
        local.getCategoriesByType(type)

    override suspend fun createCategory(category: Category) {
        local.insert(category)
        sync.markDirty(SyncCollectionNames.CATEGORIES, category.id)
    }

    override suspend fun updateCategory(category: Category) {
        local.delete(category.id)
        local.insert(category)
        sync.markDirty(SyncCollectionNames.CATEGORIES, category.id)
    }

    override suspend fun deleteCategory(id: String) {
        local.delete(id)
        sync.markDeleted(SyncCollectionNames.CATEGORIES, id)
    }

    // Default categories are seeded here; the sync engine's seed() step will
    // pick them up and push them, so no explicit dirty-mark is needed.
    override suspend fun initDefaultCategories() = local.initDefaultsIfEmpty()

    override suspend fun ensureCategory(category: Category) {
        local.ensure(category)
        sync.markDirty(SyncCollectionNames.CATEGORIES, category.id)
    }
}
