package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.Category as DbCategory
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.CategoryType
import tj.mahram.lifetrack.domain.model.DefaultCategories

class CategoryLocalDataSource(private val db: AppDatabase) {

    fun getAllCategories(): Flow<List<Category>> =
        db.categoryQueries.selectAllCategories().asFlow().mapToList(Dispatchers.Default)
            .map { it.map { c -> c.toDomain() } }

    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> =
        db.categoryQueries.selectCategoriesByType(type.name).asFlow().mapToList(Dispatchers.Default)
            .map { it.map { c -> c.toDomain() } }

    suspend fun insert(category: Category) {
        withContext(Dispatchers.Default) {
            db.categoryQueries.insertCategory(
                id = category.id,
                name = category.name,
                type = category.type.name,
                icon = category.icon,
                color = category.color,
                isCustom = if (category.isCustom) 1L else 0L
            )
        }
    }

    suspend fun delete(id: String) {
        withContext(Dispatchers.Default) {
            db.categoryQueries.deleteCategory(id)
        }
    }

    suspend fun ensure(category: Category) {
        withContext(Dispatchers.Default) {
            db.categoryQueries.insertCategoryOrIgnore(
                id = category.id,
                name = category.name,
                type = category.type.name,
                icon = category.icon,
                color = category.color,
                isCustom = if (category.isCustom) 1L else 0L
            )
        }
    }

    suspend fun initDefaultsIfEmpty() = withContext(Dispatchers.Default) {
        val count = db.categoryQueries.countCategories().executeAsOne()
        if (count == 0L) {
            DefaultCategories.all.forEach { category ->
                db.categoryQueries.insertCategory(
                    id = category.id,
                    name = category.name,
                    type = category.type.name,
                    icon = category.icon,
                    color = category.color,
                    isCustom = if (category.isCustom) 1L else 0L
                )
            }
        }
    }

    private fun DbCategory.toDomain() = Category(
        id = id,
        name = name,
        type = CategoryType.valueOf(type),
        icon = icon,
        color = color,
        isCustom = isCustom == 1L
    )
}
