package tj.mahram.lifetrack.domain.usecase.task

import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlinx.datetime.Instant
import tj.mahram.lifetrack.domain.model.Task
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.domain.repository.TaskRepository

class GetAllTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.getAllTasks()
}

class GetTodayTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.getTodayTasks()
}

class SearchTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(query: String): Flow<List<Task>> = repository.searchTasks(query)
}

class CreateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(
        title: String,
        description: String? = null,
        priority: TaskPriority = TaskPriority.MEDIUM,
        categoryId: String? = null,
        dueDateMillis: Long? = null,
        isRecurring: Boolean = false,
        parentTaskId: String? = null
    ) {
        require(title.isNotBlank()) { "Title cannot be blank" }
        val now = Clock.System.now().toEpochMilliseconds().let { Instant.fromEpochMilliseconds(it) }
        val task = Task(
            id = generateUuid(),
            title = title.trim(),
            description = description?.trim(),
            priority = priority,
            categoryId = categoryId,
            isCompleted = false,
            dueDate = dueDateMillis?.let { Instant.fromEpochMilliseconds(it) },
            createdAt = now,
            updatedAt = now,
            isRecurring = isRecurring,
            recurringType = null,
            parentTaskId = parentTaskId
        )
        repository.createTask(task)
    }
}

class UpdateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        require(task.title.isNotBlank()) { "Title cannot be blank" }
        val now = Clock.System.now().toEpochMilliseconds().let { Instant.fromEpochMilliseconds(it) }
        repository.updateTask(task.copy(updatedAt = now))
    }
}

class ToggleTaskCompletionUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String, isCompleted: Boolean) {
        repository.toggleTaskCompletion(taskId, isCompleted)
    }
}

class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String) {
        repository.deleteTask(taskId)
    }
}

class GetTaskStatsUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(): Pair<Int, Int> {
        val completed = repository.getCompletedCountToday()
        val total = repository.getTotalCountToday()
        return completed to total
    }
}

private fun generateUuid(): String {
    val chars = "0123456789abcdef"
    fun randomHex(length: Int) = (1..length).map { chars.random() }.joinToString("")
    return "${randomHex(8)}-${randomHex(4)}-4${randomHex(3)}-${listOf("8","9","a","b").random()}${randomHex(3)}-${randomHex(12)}"
}
