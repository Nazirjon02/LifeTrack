package tj.mahram.lifetrack.domain.model

import kotlin.time.Instant

data class Habit(
    val id: String,
    val name: String,
    val description: String?,
    val icon: String,
    val color: String,
    val frequency: HabitFrequency,
    val targetDaysPerWeek: Int,
    val createdAt: Instant,
    val isArchived: Boolean,
    val entries: List<HabitEntry> = emptyList()
)

data class HabitEntry(
    val id: String,
    val habitId: String,
    val completedAt: Instant
)

enum class HabitFrequency(val label: String) {
    DAILY("Daily"),
    WEEKDAYS("Weekdays"),
    WEEKENDS("Weekends"),
    CUSTOM("Custom")
}
