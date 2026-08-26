package tj.mahram.lifetrack.feature.goals

import tj.mahram.lifetrack.domain.model.Goal

data class GoalsState(
    val isLoading: Boolean = true,
    val goals: List<Goal> = emptyList(),
    val currency: String = "USD",
    val showAddSheet: Boolean = false,
    val editingGoal: Goal? = null
) {
    val activeGoals: List<Goal> get() = goals.filter { !it.isCompleted }
    val completedGoals: List<Goal> get() = goals.filter { it.isCompleted }
}

sealed class GoalsIntent {
    data object ShowAddSheet : GoalsIntent()
    data object HideAddSheet : GoalsIntent()
    data class ShowUpdate(val goal: Goal) : GoalsIntent()
    data object HideUpdate : GoalsIntent()
    data class Create(
        val title: String,
        val description: String?,
        val icon: String,
        val targetValue: Double,
        val unit: String,
        val color: String,
        val affectsBalance: Boolean
    ) : GoalsIntent()
    data class UpdateProgress(val goalId: String, val newValue: Double) : GoalsIntent()
    /** Buy / un-buy a purchase goal (books or reverses the linked expense). */
    data class SetPurchased(val goal: Goal, val purchased: Boolean) : GoalsIntent()
    data class Delete(val goalId: String) : GoalsIntent()
}
