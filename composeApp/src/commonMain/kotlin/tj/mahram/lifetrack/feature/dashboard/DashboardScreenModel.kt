package tj.mahram.lifetrack.feature.dashboard

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import tj.mahram.lifetrack.core.util.endOfMonth
import tj.mahram.lifetrack.core.util.startOfMonth
import tj.mahram.lifetrack.domain.repository.HabitRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.finance.GetFinanceSummaryUseCase
import tj.mahram.lifetrack.domain.usecase.finance.ObserveBalanceUseCase
import tj.mahram.lifetrack.domain.usecase.habit.GetAllHabitsUseCase
import tj.mahram.lifetrack.domain.usecase.habit.ToggleHabitEntryUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetTaskStatsUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetTodayTasksUseCase

class DashboardScreenModel(
    private val getTodayTasks: GetTodayTasksUseCase,
    private val getTaskStats: GetTaskStatsUseCase,
    private val getFinanceSummary: GetFinanceSummaryUseCase,
    private val observeBalance: ObserveBalanceUseCase,
    private val settingsRepository: SettingsRepository,
    private val getAllHabits: GetAllHabitsUseCase,
    private val toggleHabit: ToggleHabitEntryUseCase,
    private val habitRepository: HabitRepository
) : ScreenModel {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        load()
        observeHabits()
        observeCentralBalance()
    }

    private fun observeCentralBalance() {
        screenModelScope.launch {
            observeBalance().collect { overview ->
                _state.update { it.copy(balance = overview) }
            }
        }
    }

    private fun load() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val tz = TimeZone.currentSystemDefault()
            val now = Clock.System.now()
            val hour = now.toLocalDateTime(tz).hour
            val greeting = when {
                hour < 6  -> "Good night"
                hour < 12 -> "Good morning"
                hour < 17 -> "Good afternoon"
                hour < 21 -> "Good evening"
                else      -> "Good night"
            }

            settingsRepository.getSettings().collectLatest { settings ->
                getTodayTasks().collectLatest { tasks ->
                    val (completed, total) = getTaskStats()
                    val finance = try {
                        getFinanceSummary(startOfMonth(), endOfMonth())
                    } catch (_: Exception) { null }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            greeting = greeting,
                            todayTasks = tasks,
                            completedTasksToday = completed,
                            totalTasksToday = total,
                            monthlyFinance = finance,
                            currency = settings.currency
                        )
                    }
                }
            }
        }
    }

    private fun observeHabits() {
        screenModelScope.launch {
            getAllHabits().collect { habits ->
                val streaks = habits.associate { habit ->
                    habit.id to habitRepository.getStreakForHabit(habit.id)
                }
                _state.update { it.copy(habits = habits, habitStreaks = streaks) }
            }
        }
    }

    fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.ToggleHabit -> screenModelScope.launch {
                toggleHabit(intent.habitId)
            }
            DashboardIntent.Refresh -> load()
        }
    }

    fun refresh() = load()
}
