package tj.mahram.lifetrack.feature.habits

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import tj.mahram.lifetrack.domain.model.Habit
import tj.mahram.lifetrack.domain.model.HabitFrequency

data class HabitsState(
    val isLoading: Boolean = true,
    val habits: List<Habit> = emptyList(),
    val streaks: Map<String, Int> = emptyMap(),
    val showAddSheet: Boolean = false,
    val error: String? = null
) {
    val completedTodayCount: Int
        get() {
            val tz = TimeZone.currentSystemDefault()
            val today = Clock.System.now().toLocalDateTime(tz).date
            return habits.count { habit ->
                habit.entries.any { it.completedAt.toLocalDateTime(tz).date == today }
            }
        }
}

sealed class HabitsIntent {
    data class ToggleHabit(val habitId: String) : HabitsIntent()
    data class DeleteHabit(val habitId: String) : HabitsIntent()
    data class CreateHabit(
        val name: String,
        val icon: String,
        val color: String,
        val frequency: HabitFrequency
    ) : HabitsIntent()
    data object ShowAddSheet : HabitsIntent()
    data object HideAddSheet : HabitsIntent()
}
