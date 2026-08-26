package tj.mahram.lifetrack.feature.goals

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.goal.CreateGoalUseCase
import tj.mahram.lifetrack.domain.usecase.goal.DeleteGoalUseCase
import tj.mahram.lifetrack.domain.usecase.goal.GetAllGoalsUseCase
import tj.mahram.lifetrack.domain.usecase.goal.SetGoalPurchasedUseCase
import tj.mahram.lifetrack.domain.usecase.goal.UpdateGoalProgressUseCase

class GoalsScreenModel(
    private val getAllGoals: GetAllGoalsUseCase,
    private val createGoal: CreateGoalUseCase,
    private val updateGoalProgress: UpdateGoalProgressUseCase,
    private val setGoalPurchased: SetGoalPurchasedUseCase,
    private val deleteGoal: DeleteGoalUseCase,
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(GoalsState())
    val state: StateFlow<GoalsState> = _state.asStateFlow()

    init {
        screenModelScope.launch {
            getAllGoals().collect { goals ->
                _state.update { it.copy(goals = goals, isLoading = false) }
            }
        }
        screenModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _state.update { it.copy(currency = settings.currency) }
            }
        }
    }

    fun handleIntent(intent: GoalsIntent) {
        when (intent) {
            GoalsIntent.ShowAddSheet -> _state.update { it.copy(showAddSheet = true) }
            GoalsIntent.HideAddSheet -> _state.update { it.copy(showAddSheet = false) }
            is GoalsIntent.ShowUpdate -> _state.update { it.copy(editingGoal = intent.goal) }
            GoalsIntent.HideUpdate   -> _state.update { it.copy(editingGoal = null) }

            is GoalsIntent.Create -> screenModelScope.launch {
                createGoal(
                    title          = intent.title,
                    description    = intent.description,
                    icon           = intent.icon,
                    targetValue    = intent.targetValue,
                    unit           = intent.unit,
                    color          = intent.color,
                    affectsBalance = intent.affectsBalance
                )
                _state.update { it.copy(showAddSheet = false) }
            }

            is GoalsIntent.UpdateProgress -> screenModelScope.launch {
                updateGoalProgress(intent.goalId, intent.newValue)
                _state.update { it.copy(editingGoal = null) }
            }

            is GoalsIntent.SetPurchased -> screenModelScope.launch {
                setGoalPurchased(intent.goal, intent.purchased)
            }

            is GoalsIntent.Delete -> screenModelScope.launch {
                deleteGoal(intent.goalId)
            }
        }
    }
}
