package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.Task

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTodayTasks(): Flow<List<Task>>
    fun getTasksByCategory(categoryId: String): Flow<List<Task>>
    fun searchTasks(query: String): Flow<List<Task>>
    suspend fun getTaskById(id: String): Task?
    suspend fun createTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: String)
    suspend fun toggleTaskCompletion(id: String, isCompleted: Boolean)
    suspend fun getCompletedCountToday(): Int
    suspend fun getTotalCountToday(): Int
}
