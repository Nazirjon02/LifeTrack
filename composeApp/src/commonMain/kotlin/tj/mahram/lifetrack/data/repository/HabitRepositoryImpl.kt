package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.data.local.HabitLocalDataSource
import tj.mahram.lifetrack.domain.model.Habit
import tj.mahram.lifetrack.domain.repository.HabitRepository

class HabitRepositoryImpl(private val dataSource: HabitLocalDataSource) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> = dataSource.getAllHabitsWithEntries()

    override suspend fun toggleHabitToday(habitId: String) = dataSource.toggleToday(habitId)

    override suspend fun createHabit(habit: Habit) = dataSource.insert(habit)

    override suspend fun deleteHabit(habitId: String) = dataSource.delete(habitId)

    override suspend fun getStreakForHabit(habitId: String): Int = dataSource.getStreak(habitId)
}
