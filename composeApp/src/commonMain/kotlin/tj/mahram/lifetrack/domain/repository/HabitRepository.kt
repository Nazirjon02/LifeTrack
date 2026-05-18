package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.Habit
import tj.mahram.lifetrack.domain.model.HabitEntry

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun toggleHabitToday(habitId: String)
    suspend fun createHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)
    suspend fun getStreakForHabit(habitId: String): Int
}
