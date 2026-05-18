package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.domain.model.Goal
import tj.mahram.lifetrack.Goal as DbGoal

class GoalLocalDataSource(private val db: AppDatabase) {

    fun getAllGoals(): Flow<List<Goal>> =
        db.goalQueries.selectAllGoals().asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    fun getActiveGoals(): Flow<List<Goal>> =
        db.goalQueries.selectActiveGoals().asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    suspend fun insert(goal: Goal): Unit = withContext(Dispatchers.IO) {
        db.goalQueries.insertGoal(
            id = goal.id,
            title = goal.title,
            description = goal.description,
            icon = goal.icon,
            targetValue = goal.targetValue,
            currentValue = goal.currentValue,
            unit = goal.unit,
            color = goal.color,
            deadline = goal.deadline?.toEpochMilliseconds(),
            isCompleted = if (goal.isCompleted) 1L else 0L,
            createdAt = goal.createdAt.toEpochMilliseconds(),
            updatedAt = goal.updatedAt.toEpochMilliseconds()
        )
        Unit
    }

    suspend fun updateProgress(goalId: String, newValue: Double): Unit = withContext(Dispatchers.IO) {
        val goal = db.goalQueries.selectGoalById(goalId).executeAsOneOrNull() ?: return@withContext
        val completed = if (newValue >= goal.targetValue) 1L else 0L
        db.goalQueries.updateGoalProgress(
            currentValue = newValue,
            isCompleted = completed,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
            id = goalId
        )
        Unit
    }

    suspend fun delete(goalId: String): Unit = withContext(Dispatchers.IO) {
        db.goalQueries.deleteGoal(goalId)
        Unit
    }

    private fun DbGoal.toDomain() = Goal(
        id = id,
        title = title,
        description = description,
        icon = icon,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        color = color,
        deadline = deadline?.let { Instant.fromEpochMilliseconds(it) },
        isCompleted = isCompleted == 1L,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
}
