package tj.mahram.lifetrack.domain.model

import kotlin.time.Instant

data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val priority: TaskPriority,
    val categoryId: String?,
    val isCompleted: Boolean,
    val dueDate: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isRecurring: Boolean,
    val recurringType: RecurringType?,
    val parentTaskId: String?,
    val subtasks: List<Task> = emptyList()
)

enum class TaskPriority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical")
}

enum class RecurringType {
    DAILY, WEEKLY, MONTHLY, CUSTOM
}
