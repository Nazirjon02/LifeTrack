package tj.mahram.lifetrack.feature.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.model.ProblemStatus
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.finance.ObserveBalanceUseCase
import tj.mahram.lifetrack.domain.usecase.goal.GetAllGoalsUseCase
import tj.mahram.lifetrack.domain.usecase.problem.GetAllProblemsUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetAllTasksUseCase

class ProfileScreenModel(
    private val settingsRepository: SettingsRepository,
    private val observeBalance: ObserveBalanceUseCase,
    private val getAllTasks: GetAllTasksUseCase,
    private val getAllProblems: GetAllProblemsUseCase,
    private val getAllGoals: GetAllGoalsUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        screenModelScope.launch {
            combine(
                settingsRepository.getSettings(),
                observeBalance(),
                getAllTasks(),
                getAllProblems(),
                getAllGoals()
            ) { settings, balance, tasks, problems, goals ->
                ProfileState(
                    isLoading = false,
                    settings = settings,
                    balance = balance,
                    tasksDone = tasks.count { it.isCompleted },
                    tasksTotal = tasks.size,
                    problemsResolved = problems.count { it.status == ProblemStatus.RESOLVED },
                    problemsTotal = problems.size,
                    goalsAchieved = goals.count { it.isCompleted },
                    goalsTotal = goals.size
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun handleIntent(intent: ProfileIntent) {
        screenModelScope.launch {
            when (intent) {
                is ProfileIntent.SetTheme -> settingsRepository.setTheme(intent.theme)
                is ProfileIntent.SetCurrency -> settingsRepository.setCurrency(intent.currency)
                is ProfileIntent.SetLanguage -> settingsRepository.setLanguage(intent.language)
                is ProfileIntent.SetNotifications -> settingsRepository.setNotificationsEnabled(intent.enabled)
                is ProfileIntent.SetTaskNotifications -> settingsRepository.setTaskNotificationsEnabled(intent.enabled)
                is ProfileIntent.SetFinanceNotifications -> settingsRepository.setFinanceNotificationsEnabled(intent.enabled)
                is ProfileIntent.SetDisplayName -> settingsRepository.setDisplayName(intent.name.trim())
                is ProfileIntent.SetAvatar -> settingsRepository.setAvatarEmoji(intent.emoji)
            }
        }
    }
}
