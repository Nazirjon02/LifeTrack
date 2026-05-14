package tj.mahram.lifetrack.feature.planner

import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.Task

data class PlannerState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val showAddTaskSheet: Boolean = false,
    val error: String? = null
) {
    val filteredTasks: List<Task>
        get() = tasks.filter { task ->
            (selectedCategoryId == null || task.categoryId == selectedCategoryId) &&
            (searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true))
        }

    val pendingTasks: List<Task> get() = filteredTasks.filter { !it.isCompleted }
    val completedTasks: List<Task> get() = filteredTasks.filter { it.isCompleted }
}

sealed class PlannerIntent {
    data class ToggleTask(val taskId: String, val isCompleted: Boolean) : PlannerIntent()
    data class DeleteTask(val taskId: String) : PlannerIntent()
    data class SelectCategory(val categoryId: String?) : PlannerIntent()
    data class Search(val query: String) : PlannerIntent()
    data object ShowAddTask : PlannerIntent()
    data object HideAddTask : PlannerIntent()
    data class CreateTask(
        val title: String,
        val description: String?,
        val priority: tj.mahram.lifetrack.domain.model.TaskPriority,
        val categoryId: String?,
        val dueDateMillis: Long?
    ) : PlannerIntent()
}
