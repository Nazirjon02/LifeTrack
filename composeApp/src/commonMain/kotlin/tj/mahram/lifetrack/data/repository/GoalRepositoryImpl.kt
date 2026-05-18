package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.data.local.GoalLocalDataSource
import tj.mahram.lifetrack.domain.model.Goal
import tj.mahram.lifetrack.domain.repository.GoalRepository

class GoalRepositoryImpl(private val dataSource: GoalLocalDataSource) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> = dataSource.getAllGoals()

    override fun getActiveGoals(): Flow<List<Goal>> = dataSource.getActiveGoals()

    override suspend fun createGoal(goal: Goal) = dataSource.insert(goal)

    override suspend fun updateGoalProgress(goalId: String, newValue: Double) =
        dataSource.updateProgress(goalId, newValue)

    override suspend fun deleteGoal(goalId: String) = dataSource.delete(goalId)
}
