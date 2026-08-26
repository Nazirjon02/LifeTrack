package tj.mahram.lifetrack.feature.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.repository.HabitRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.finance.ObserveBalanceUseCase
import tj.mahram.lifetrack.domain.usecase.goal.GetAllGoalsUseCase
import tj.mahram.lifetrack.domain.usecase.habit.GetAllHabitsUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetAllTasksUseCase

class ProfileScreenModel(
    private val settingsRepository: SettingsRepository,
    private val observeBalance: ObserveBalanceUseCase,
    private val getAllTasks: GetAllTasksUseCase,
    private val getAllHabits: GetAllHabitsUseCase,
    private val getAllGoals: GetAllGoalsUseCase,
    private val habitRepository: HabitRepository
) : ScreenModel {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        screenModelScope.launch {
            combine(
                settingsRepository.getSettings(),
                observeBalance(),
                getAllTasks(),
                getAllHabits(),
                getAllGoals()
            ) { settings, balance, tasks, habits, goals ->
                val bestStreak = habits.maxOfOrNull { habitRepository.getStreakForHabit(it.id) } ?: 0
                ProfileState(
                    isLoading = false,
                    settings = settings,
                    balance = balance,
                    tasksDone = tasks.count { it.isCompleted },
                    tasksTotal = tasks.size,
                    habitsCount = habits.size,
                    goalsAchieved = goals.count { it.isCompleted },
                    goalsTotal = goals.size,
                    bestStreak = bestStreak
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
