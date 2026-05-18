package tj.mahram.lifetrack.domain.usecase.goal

import tj.mahram.lifetrack.domain.repository.GoalRepository

class UpdateGoalProgressUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(goalId: String, newValue: Double) =
        repository.updateGoalProgress(goalId, newValue)
}
