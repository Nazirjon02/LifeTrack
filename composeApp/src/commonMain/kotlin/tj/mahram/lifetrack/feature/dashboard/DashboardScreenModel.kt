package tj.mahram.lifetrack.feature.dashboard

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import tj.mahram.lifetrack.core.util.endOfMonth
import tj.mahram.lifetrack.core.util.startOfMonth
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.finance.GetFinanceSummaryUseCase
import tj.mahram.lifetrack.domain.usecase.finance.ObserveBalanceUseCase
import tj.mahram.lifetrack.domain.usecase.problem.GetAllProblemsUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetTaskStatsUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetTodayTasksUseCase

class DashboardScreenModel(
    private val getTodayTasks: GetTodayTasksUseCase,
    private val getTaskStats: GetTaskStatsUseCase,
    private val getFinanceSummary: GetFinanceSummaryUseCase,
    private val observeBalance: ObserveBalanceUseCase,
    private val settingsRepository: SettingsRepository,
    private val getAllProblems: GetAllProblemsUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        load()
        observeProblems()
        observeCentralBalance()
    }

    private fun observeCentralBalance() {
        screenModelScope.launch {
            observeBalance().collect { overview ->
                _state.update { it.copy(balance = overview) }
            }
        }
    }

    private fun observeProblems() {
        screenModelScope.launch {
            getAllProblems().collect { problems ->
                _state.update { it.copy(problems = problems) }
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

    fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            DashboardIntent.Refresh -> load()
        }
    }

    fun refresh() = load()
}
