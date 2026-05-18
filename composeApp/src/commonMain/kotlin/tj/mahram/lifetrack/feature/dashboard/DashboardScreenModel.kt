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
import tj.mahram.lifetrack.domain.usecase.habit.GetAllHabitsUseCase
import tj.mahram.lifetrack.domain.usecase.habit.ToggleHabitEntryUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetTaskStatsUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetTodayTasksUseCase

class DashboardScreenModel(
    private val getTodayTasks: GetTodayTasksUseCase,
    private val getTaskStats: GetTaskStatsUseCase,
    private val getFinanceSummary: GetFinanceSummaryUseCase,
    private val settingsRepository: SettingsRepository,
    private val getAllHabits: GetAllHabitsUseCase,
    private val toggleHabit: ToggleHabitEntryUseCase,
    private val habitRepository: HabitRepository
) : ScreenModel {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private var pomodoroJob: Job? = null

    init {
        load()
        observeHabits()
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
            DashboardIntent.PomodoroToggle -> togglePomodoro()
            DashboardIntent.PomodoroReset -> resetPomodoro()
            is DashboardIntent.ToggleHabit -> screenModelScope.launch {
                toggleHabit(intent.habitId)
            }
            DashboardIntent.Refresh -> load()
        }
    }

    fun refresh() = load()

    private fun togglePomodoro() {
        val pomodoro = _state.value.pomodoro
        if (pomodoro.isRunning) {
            pomodoroJob?.cancel()
            _state.update { it.copy(pomodoro = it.pomodoro.copy(isRunning = false)) }
        } else {
            _state.update { it.copy(pomodoro = it.pomodoro.copy(isRunning = true)) }
            pomodoroJob = screenModelScope.launch {
                while (_state.value.pomodoro.remainingSeconds > 0) {
                    delay(1000)
                    val current = _state.value.pomodoro
                    if (!current.isRunning) break
                    val newRemaining = current.remainingSeconds - 1
                    if (newRemaining <= 0) {
                        // Switch mode
                        val newMode = if (current.mode == PomodoroMode.WORK) PomodoroMode.BREAK else PomodoroMode.WORK
                        val newPomodoros = if (current.mode == PomodoroMode.WORK) current.completedPomodoros + 1 else current.completedPomodoros
                        _state.update {
                            it.copy(pomodoro = PomodoroState(
                                isRunning = false,
                                mode = newMode,
                                remainingSeconds = if (newMode == PomodoroMode.WORK) 25 * 60 else 5 * 60,
                                completedPomodoros = newPomodoros
                            ))
                        }
                    } else {
                        _state.update { it.copy(pomodoro = current.copy(remainingSeconds = newRemaining)) }
                    }
                }
            }
        }
    }

    private fun resetPomodoro() {
        pomodoroJob?.cancel()
        _state.update {
            it.copy(pomodoro = PomodoroState(
                isRunning = false,
                mode = it.pomodoro.mode,
                remainingSeconds = if (it.pomodoro.mode == PomodoroMode.WORK) 25 * 60 else 5 * 60,
                completedPomodoros = it.pomodoro.completedPomodoros
            ))
        }
    }

    override fun onDispose() {
        pomodoroJob?.cancel()
        super.onDispose()
    }
}
