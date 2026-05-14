package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import tj.mahram.lifetrack.core.util.endOfDay
import tj.mahram.lifetrack.core.util.startOfDay
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.Task as DbTask
import tj.mahram.lifetrack.domain.model.RecurringType
import tj.mahram.lifetrack.domain.model.Task
import tj.mahram.lifetrack.domain.model.TaskPriority

class TaskLocalDataSource(private val db: AppDatabase) {

    fun getAllTasks(): Flow<List<Task>> =
        db.taskQueries.selectAllTasks().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toDomain() }
        }

    fun getTodayTasks(): Flow<List<Task>> {
        val start = startOfDay().toEpochMilliseconds()
        val end = endOfDay().toEpochMilliseconds()
        return db.taskQueries.selectTasksForToday(start, end).asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    fun getTasksByCategory(categoryId: String): Flow<List<Task>> =
        db.taskQueries.selectTasksByCategory(categoryId).asFlow().mapToList(Dispatchers.IO)
            .map { it.map { t -> t.toDomain() } }

    fun searchTasks(query: String): Flow<List<Task>> =
        db.taskQueries.searchTasks(query).asFlow().mapToList(Dispatchers.IO)
            .map { it.map { t -> t.toDomain() } }

    suspend fun getTaskById(id: String): Task? = withContext(Dispatchers.IO) {
        db.taskQueries.selectTaskById(id).executeAsOneOrNull()?.toDomain()
    }

    suspend fun insert(task: Task) {
        withContext(Dispatchers.IO) {
            db.taskQueries.insertTask(
                id = task.id,
                title = task.title,
                description = task.description,
                priority = task.priority.name,
                categoryId = task.categoryId,
                isCompleted = if (task.isCompleted) 1L else 0L,
                dueDate = task.dueDate?.toEpochMilliseconds(),
                createdAt = task.createdAt.toEpochMilliseconds(),
                updatedAt = task.updatedAt.toEpochMilliseconds(),
                isRecurring = if (task.isRecurring) 1L else 0L,
                recurringType = task.recurringType?.name,
                parentTaskId = task.parentTaskId
            )
        }
    }

    suspend fun update(task: Task) {
        withContext(Dispatchers.IO) {
            db.taskQueries.updateTask(
                title = task.title,
                description = task.description,
                priority = task.priority.name,
                categoryId = task.categoryId,
                isCompleted = if (task.isCompleted) 1L else 0L,
                dueDate = task.dueDate?.toEpochMilliseconds(),
                updatedAt = task.updatedAt.toEpochMilliseconds(),
                isRecurring = if (task.isRecurring) 1L else 0L,
                recurringType = task.recurringType?.name,
                id = task.id
            )
        }
    }

    suspend fun toggleCompletion(id: String, isCompleted: Boolean) {
        withContext(Dispatchers.IO) {
            db.taskQueries.updateTaskCompletion(
                isCompleted = if (isCompleted) 1L else 0L,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
                id = id
            )
        }
    }

    suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            db.taskQueries.deleteSubtasks(id)
            db.taskQueries.deleteTask(id)
        }
    }

    suspend fun getCompletedCountToday(): Int = withContext(Dispatchers.IO) {
        val start = startOfDay().toEpochMilliseconds()
        val end = endOfDay().toEpochMilliseconds()
        db.taskQueries.countCompletedToday(start, end).executeAsOne().toInt()
    }

    suspend fun getTotalCountToday(): Int = withContext(Dispatchers.IO) {
        val start = startOfDay().toEpochMilliseconds()
        val end = endOfDay().toEpochMilliseconds()
        db.taskQueries.countTotalToday(start, end).executeAsOne().toInt()
    }

    private fun DbTask.toDomain() = Task(
        id = id,
        title = title,
        description = description,
        priority = TaskPriority.valueOf(priority),
        categoryId = categoryId,
        isCompleted = isCompleted == 1L,
        dueDate = dueDate?.let { Instant.fromEpochMilliseconds(it) },
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        isRecurring = isRecurring == 1L,
        recurringType = recurringType?.let { RecurringType.valueOf(it) },
        parentTaskId = parentTaskId
    )
}
