package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import tj.mahram.lifetrack.core.util.endOfDay
import tj.mahram.lifetrack.core.util.startOfDay
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.domain.model.Habit
import tj.mahram.lifetrack.domain.model.HabitEntry
import tj.mahram.lifetrack.domain.model.HabitFrequency
import tj.mahram.lifetrack.Habit as DbHabit
import tj.mahram.lifetrack.Habit_entry as DbEntry

class HabitLocalDataSource(private val db: AppDatabase) {

    fun getAllHabitsWithEntries(): Flow<List<Habit>> {
        return db.habitQueries.selectAllHabits().asFlow().mapToList(Dispatchers.IO).map { habits ->
            habits.map { dbHabit ->
                val entries = db.habitQueries.selectEntriesForHabit(dbHabit.id)
                    .executeAsList()
                    .map { it.toDomain() }
                dbHabit.toDomain(entries)
            }
        }
    }

    suspend fun insert(habit: Habit): Unit = withContext(Dispatchers.IO) {
        db.habitQueries.insertHabit(
            id = habit.id,
            name = habit.name,
            description = habit.description,
            icon = habit.icon,
            color = habit.color,
            frequency = habit.frequency.name,
            targetDaysPerWeek = habit.targetDaysPerWeek.toLong(),
            createdAt = habit.createdAt.toEpochMilliseconds(),
            isArchived = 0L
        )
        Unit
    }

    suspend fun delete(habitId: String): Unit = withContext(Dispatchers.IO) {
        db.habitQueries.deleteHabit(habitId)
        Unit
    }

    suspend fun toggleToday(habitId: String): Unit = withContext(Dispatchers.IO) {
        val start = startOfDay().toEpochMilliseconds()
        val end = endOfDay().toEpochMilliseconds()
        val existing = db.habitQueries.selectEntryForHabitOnDay(habitId, start, end).executeAsOneOrNull()
        if (existing != null) {
            db.habitQueries.deleteEntryForHabitOnDay(habitId, start, end)
        } else {
            val now = Clock.System.now()
            val entryId = now.toEpochMilliseconds().toString() + habitId
            db.habitQueries.insertHabitEntry(
                id = entryId,
                habitId = habitId,
                completedAt = now.toEpochMilliseconds()
            )
        }
    }

    suspend fun getStreak(habitId: String): Int = withContext(Dispatchers.IO) {
        val entries = db.habitQueries.selectEntriesForHabit(habitId).executeAsList()
        if (entries.isEmpty()) return@withContext 0
        val tz = TimeZone.currentSystemDefault()
        val days = entries
            .map { Instant.fromEpochMilliseconds(it.completedAt).toLocalDateTime(tz).date }
            .distinct()
            .sortedDescending()
        var streak = 0
        var expected = Clock.System.now().toLocalDateTime(tz).date
        for (day in days) {
            if (day == expected) {
                streak++
                expected = expected.minus(1, DateTimeUnit.DAY)
            } else break
        }
        streak
    }

    private fun DbHabit.toDomain(entries: List<HabitEntry> = emptyList()) = Habit(
        id = id,
        name = name,
        description = description,
        icon = icon,
        color = color,
        frequency = runCatching { HabitFrequency.valueOf(frequency) }.getOrDefault(HabitFrequency.DAILY),
        targetDaysPerWeek = targetDaysPerWeek.toInt(),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        isArchived = isArchived == 1L,
        entries = entries
    )

    private fun DbEntry.toDomain() = HabitEntry(
        id = id,
        habitId = habitId,
        completedAt = Instant.fromEpochMilliseconds(completedAt)
    )
}
