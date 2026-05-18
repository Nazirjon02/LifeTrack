package tj.mahram.lifetrack.domain.usecase.habit

import kotlin.time.Clock
import tj.mahram.lifetrack.domain.model.Habit
import tj.mahram.lifetrack.domain.model.HabitFrequency
import tj.mahram.lifetrack.domain.repository.HabitRepository

class CreateHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(
        name: String,
        description: String?,
        icon: String,
        color: String,
        frequency: HabitFrequency,
        targetDaysPerWeek: Int
    ) {
        val now = Clock.System.now()
        val habit = Habit(
            id = now.toEpochMilliseconds().toString(),
            name = name.trim(),
            description = description?.trim()?.ifEmpty { null },
            icon = icon,
            color = color,
            frequency = frequency,
            targetDaysPerWeek = targetDaysPerWeek,
            createdAt = now,
            isArchived = false
        )
        repository.createHabit(habit)
    }
}
