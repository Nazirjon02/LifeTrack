package tj.mahram.lifetrack.domain.usecase.goal

import kotlin.time.Clock
import tj.mahram.lifetrack.domain.model.Goal
import tj.mahram.lifetrack.domain.repository.GoalRepository

class CreateGoalUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(
        title: String,
        description: String?,
        icon: String,
        targetValue: Double,
        unit: String,
        color: String,
        affectsBalance: Boolean = false
    ) {
        val now = Clock.System.now()
        val goal = Goal(
            id = now.toEpochMilliseconds().toString(),
            title = title.trim(),
            description = description?.trim()?.ifEmpty { null },
            icon = icon,
            targetValue = targetValue,
            currentValue = 0.0,
            unit = unit,
            color = color,
            deadline = null,
            isCompleted = false,
            createdAt = now,
            updatedAt = now,
            affectsBalance = affectsBalance
        )
        repository.createGoal(goal)
    }
}
