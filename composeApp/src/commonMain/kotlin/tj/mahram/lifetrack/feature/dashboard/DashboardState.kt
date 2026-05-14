package tj.mahram.lifetrack.feature.dashboard

import tj.mahram.lifetrack.domain.model.FinanceSummary
import tj.mahram.lifetrack.domain.model.PortfolioSummary
import tj.mahram.lifetrack.domain.model.Task

data class DashboardState(
    val isLoading: Boolean = true,
    val greeting: String = "",
    val todayTasks: List<Task> = emptyList(),
    val completedTasksToday: Int = 0,
    val totalTasksToday: Int = 0,
    val monthlyFinance: FinanceSummary? = null,
    val portfolioSummary: PortfolioSummary? = null,
    val currency: String = "USD",
    val error: String? = null
)
