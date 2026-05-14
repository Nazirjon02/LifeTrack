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
import tj.mahram.lifetrack.domain.usecase.crypto.GetPortfolioSummaryUseCase
import tj.mahram.lifetrack.domain.usecase.finance.GetFinanceSummaryUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetTaskStatsUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetTodayTasksUseCase

class DashboardScreenModel(
    private val getTodayTasks: GetTodayTasksUseCase,
    private val getTaskStats: GetTaskStatsUseCase,
    private val getFinanceSummary: GetFinanceSummaryUseCase,
    private val getPortfolioSummary: GetPortfolioSummaryUseCase,
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    private fun load() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val tz = TimeZone.currentSystemDefault()
            val now = Clock.System.now()
            val hour = now.toLocalDateTime(tz).hour
            val greeting = when {
                hour < 12 -> "Good morning"
                hour < 17 -> "Good afternoon"
                else -> "Good evening"
            }

            settingsRepository.getSettings().collectLatest { settings ->
                getTodayTasks().collectLatest { tasks ->
                    val (completed, total) = getTaskStats()
                    val finance = try {
                        getFinanceSummary(startOfMonth(), endOfMonth())
                    } catch (e: Exception) { null }
                    val portfolio = try {
                        getPortfolioSummary(settings.currency.lowercase()).getOrNull()
                    } catch (e: Exception) { null }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            greeting = greeting,
                            todayTasks = tasks,
                            completedTasksToday = completed,
                            totalTasksToday = total,
                            monthlyFinance = finance,
                            portfolioSummary = portfolio,
                            currency = settings.currency
                        )
                    }
                }
            }
        }
    }
}
