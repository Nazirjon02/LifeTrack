package tj.mahram.lifetrack.domain.usecase.goal

import tj.mahram.lifetrack.domain.repository.GoalRepository

class DeleteGoalUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(goalId: String) = repository.deleteGoal(goalId)
}
