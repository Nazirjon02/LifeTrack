package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.Goal

interface GoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    fun getActiveGoals(): Flow<List<Goal>>
    suspend fun createGoal(goal: Goal)
    suspend fun updateGoalProgress(goalId: String, newValue: Double)
    suspend fun deleteGoal(goalId: String)
}
