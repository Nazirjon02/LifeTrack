package tj.mahram.lifetrack.feature.dashboard

import tj.mahram.lifetrack.domain.model.BalanceOverview
import tj.mahram.lifetrack.domain.model.FinanceSummary
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.domain.model.ProblemStatus
import tj.mahram.lifetrack.domain.model.Task

data class DashboardState(
    val isLoading: Boolean = true,
    val greeting: String = "",
    val todayTasks: List<Task> = emptyList(),
    val completedTasksToday: Int = 0,
    val totalTasksToday: Int = 0,
    val monthlyFinance: FinanceSummary? = null,
    val balance: BalanceOverview? = null,
    val currency: String = "USD",
    val problems: List<Problem> = emptyList(),
    val error: String? = null
) {
    val activeProblems: List<Problem> get() = problems.filter { it.status != ProblemStatus.RESOLVED }
    val activeProblemsCount: Int get() = activeProblems.size
    val totalProblemsCount: Int get() = problems.size
}

sealed class DashboardIntent {
    data object Refresh : DashboardIntent()
}
