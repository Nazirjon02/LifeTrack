package tj.mahram.lifetrack.domain.usecase.habit

import tj.mahram.lifetrack.domain.repository.HabitRepository

class ToggleHabitEntryUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: String) = repository.toggleHabitToday(habitId)
}
