package tj.mahram.lifetrack.feature.dashboard

import tj.mahram.lifetrack.domain.model.FinanceSummary
import tj.mahram.lifetrack.domain.model.Habit
import tj.mahram.lifetrack.domain.model.Task

enum class PomodoroMode { WORK, BREAK }

data class PomodoroState(
    val isRunning: Boolean = false,
    val mode: PomodoroMode = PomodoroMode.WORK,
    val remainingSeconds: Int = 25 * 60,
    val completedPomodoros: Int = 0
) {
    val totalSeconds: Int get() = if (mode == PomodoroMode.WORK) 25 * 60 else 5 * 60
    val progressFraction: Float get() = 1f - remainingSeconds.toFloat() / totalSeconds
    val timeString: String get() {
        val m = remainingSeconds / 60
        val s = remainingSeconds % 60
        return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }
}

data class DashboardState(
    val isLoading: Boolean = true,
    val greeting: String = "",
    val todayTasks: List<Task> = emptyList(),
    val completedTasksToday: Int = 0,
    val totalTasksToday: Int = 0,
    val monthlyFinance: FinanceSummary? = null,
    val currency: String = "USD",
    val habits: List<Habit> = emptyList(),
    val habitStreaks: Map<String, Int> = emptyMap(),
    val pomodoro: PomodoroState = PomodoroState(),
    val error: String? = null
)

sealed class DashboardIntent {
    data object PomodoroToggle : DashboardIntent()
    data object PomodoroReset : DashboardIntent()
    data class ToggleHabit(val habitId: String) : DashboardIntent()
    data object Refresh : DashboardIntent()
}
