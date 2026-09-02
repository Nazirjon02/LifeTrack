package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.data.local.GoalLocalDataSource
import tj.mahram.lifetrack.data.sync.SyncCollectionNames
import tj.mahram.lifetrack.data.sync.SyncTracker
import tj.mahram.lifetrack.domain.model.Goal
import tj.mahram.lifetrack.domain.repository.GoalRepository

class GoalRepositoryImpl(
    private val dataSource: GoalLocalDataSource,
    private val sync: SyncTracker
) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> = dataSource.getAllGoals()

    override fun getActiveGoals(): Flow<List<Goal>> = dataSource.getActiveGoals()

    override suspend fun createGoal(goal: Goal) {
        dataSource.insert(goal)
        sync.markDirty(SyncCollectionNames.GOALS, goal.id)
    }

    override suspend fun updateGoalProgress(goalId: String, newValue: Double) {
        dataSource.updateProgress(goalId, newValue)
        sync.markDirty(SyncCollectionNames.GOALS, goalId)
    }

    override suspend fun deleteGoal(goalId: String) {
        dataSource.delete(goalId)
        sync.markDeleted(SyncCollectionNames.GOALS, goalId)
    }
}
