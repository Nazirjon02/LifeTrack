package tj.mahram.lifetrack.domain.usecase.habit

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.Habit
import tj.mahram.lifetrack.domain.repository.HabitRepository

class GetAllHabitsUseCase(private val repository: HabitRepository) {
    operator fun invoke(): Flow<List<Habit>> = repository.getAllHabits()
}
