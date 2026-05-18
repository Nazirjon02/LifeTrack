package tj.mahram.lifetrack.feature.planner

import kotlin.time.Clock
import kotlinx.datetime.*
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.Task

enum class PlannerView { YEAR, MONTH, DAY }

private fun currentDate(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

data class PlannerState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val showAddTaskSheet: Boolean = false,
    val error: String? = null,
    val plannerView: PlannerView = PlannerView.DAY,
    val selectedYear: Int = currentDate().year,
    val selectedMonth: Int = currentDate().month.number,
    val selectedDay: Int = currentDate().day,
) {
    val todayDate: LocalDate get() = currentDate()

    val selectedDate: LocalDate
        get() = try {
            LocalDate(selectedYear, selectedMonth, selectedDay)
        } catch (e: Exception) {
            currentDate()
        }

    private val filteredTasks: List<Task>
        get() = tasks.filter { task ->
            (selectedCategoryId == null || task.categoryId == selectedCategoryId) &&
            (searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true))
        }

    val pendingTasks: List<Task>   get() = filteredTasks.filter { !it.isCompleted }
    val completedTasks: List<Task> get() = filteredTasks.filter { it.isCompleted }

    val progressPercent: Float
        get() = if (tasks.isEmpty()) 0f else tasks.count { it.isCompleted }.toFloat() / tasks.size

    fun tasksForMonth(year: Int, month: Int): List<Task> = tasks.filter { task ->
        task.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date?.let {
            it.year == year && it.month.number == month
        } ?: false
    }

    fun tasksForDay(year: Int, month: Int, day: Int): List<Task> = tasks.filter { task ->
        task.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date?.let {
            it.year == year && it.month.number == month && it.day == day
        } ?: false
    }

    val dayTasks: List<Task>          get() = tasksForDay(selectedYear, selectedMonth, selectedDay)
    val dayPendingTasks: List<Task>   get() = dayTasks.filter { !it.isCompleted }
    val dayCompletedTasks: List<Task> get() = dayTasks.filter { it.isCompleted }
}

sealed class PlannerIntent {
    data class ToggleTask(val taskId: String, val isCompleted: Boolean) : PlannerIntent()
    data class DeleteTask(val taskId: String) : PlannerIntent()
    data class SelectCategory(val categoryId: String?) : PlannerIntent()
    data class Search(val query: String) : PlannerIntent()
    data object ShowAddTask : PlannerIntent()
    data object HideAddTask : PlannerIntent()

    // View switching
    data class SetView(val view: PlannerView) : PlannerIntent()

    // Navigation within a view (keeps current view)
    data class NavigateYear(val year: Int) : PlannerIntent()
    data class NavigateMonth(val year: Int, val month: Int) : PlannerIntent()
    data class NavigateDay(val year: Int, val month: Int, val day: Int) : PlannerIntent()

    // Drill-down (switches view)
    data class SelectMonth(val year: Int, val month: Int) : PlannerIntent()
    data class SelectDay(val year: Int, val month: Int, val day: Int) : PlannerIntent()

    data class CreateTask(
        val title: String,
        val description: String?,
        val priority: tj.mahram.lifetrack.domain.model.TaskPriority,
        val categoryId: String?,
        val dueDateMillis: Long?
    ) : PlannerIntent()
}
