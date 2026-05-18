package tj.mahram.lifetrack.domain.usecase.goal

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.Goal
import tj.mahram.lifetrack.domain.repository.GoalRepository

class GetAllGoalsUseCase(private val repository: GoalRepository) {
    operator fun invoke(): Flow<List<Goal>> = repository.getAllGoals()
}
