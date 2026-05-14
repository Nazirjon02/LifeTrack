package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.data.local.TaskLocalDataSource
import tj.mahram.lifetrack.domain.model.Task
import tj.mahram.lifetrack.domain.repository.TaskRepository

class TaskRepositoryImpl(private val local: TaskLocalDataSource) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> = local.getAllTasks()

    override fun getTodayTasks(): Flow<List<Task>> = local.getTodayTasks()

    override fun getTasksByCategory(categoryId: String): Flow<List<Task>> =
        local.getTasksByCategory(categoryId)

    override fun searchTasks(query: String): Flow<List<Task>> = local.searchTasks(query)

    override suspend fun getTaskById(id: String): Task? = local.getTaskById(id)

    override suspend fun createTask(task: Task) = local.insert(task)

    override suspend fun updateTask(task: Task) = local.update(task)

    override suspend fun deleteTask(id: String) = local.delete(id)

    override suspend fun toggleTaskCompletion(id: String, isCompleted: Boolean) =
        local.toggleCompletion(id, isCompleted)

    override suspend fun getCompletedCountToday(): Int = local.getCompletedCountToday()

    override suspend fun getTotalCountToday(): Int = local.getTotalCountToday()
}
