package tj.mahram.lifetrack.feature.dashboard

import tj.mahram.lifetrack.domain.model.FinanceSummary
import tj.mahram.lifetrack.domain.model.Habit
import tj.mahram.lifetrack.domain.model.Task

data class DashboardState(
    val isLoading: Boolean = true,
    val greeting: String = "",
    val todayTasks: List<Task> = emptyList(),
    val completedTasksToday: Int = 0,
    val totalTasksToday: Int = 0,
    val monthlyFinance: FinanceSummary? = null,
    val currency: String = "USD",
    val habits: List<Habit> = emptyList(),
    val habitStreaks: Map<String, Int> = emptyMap(),
    val error: String? = null
)

sealed class DashboardIntent {
    data class ToggleHabit(val habitId: String) : DashboardIntent()
    data object Refresh : DashboardIntent()
}
