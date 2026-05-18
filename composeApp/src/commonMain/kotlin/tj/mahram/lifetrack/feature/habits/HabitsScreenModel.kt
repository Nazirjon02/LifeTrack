package tj.mahram.lifetrack.feature.habits

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.usecase.habit.CreateHabitUseCase
import tj.mahram.lifetrack.domain.usecase.habit.DeleteHabitUseCase
import tj.mahram.lifetrack.domain.usecase.habit.GetAllHabitsUseCase
import tj.mahram.lifetrack.domain.usecase.habit.ToggleHabitEntryUseCase
import tj.mahram.lifetrack.domain.repository.HabitRepository

class HabitsScreenModel(
    private val getAllHabits: GetAllHabitsUseCase,
    private val toggleHabit: ToggleHabitEntryUseCase,
    private val createHabit: CreateHabitUseCase,
    private val deleteHabit: DeleteHabitUseCase,
    private val habitRepository: HabitRepository
) : ScreenModel {

    private val _state = MutableStateFlow(HabitsState())
    val state = _state.asStateFlow()

    init {
        observeHabits()
    }

    private fun observeHabits() {
        screenModelScope.launch {
            getAllHabits().collect { habits ->
                val streaks = habits.associate { habit ->
                    habit.id to habitRepository.getStreakForHabit(habit.id)
                }
                _state.update { it.copy(isLoading = false, habits = habits, streaks = streaks) }
            }
        }
    }

    fun handleIntent(intent: HabitsIntent) {
        when (intent) {
            is HabitsIntent.ToggleHabit -> screenModelScope.launch {
                toggleHabit(intent.habitId)
            }
            is HabitsIntent.DeleteHabit -> screenModelScope.launch {
                deleteHabit(intent.habitId)
            }
            is HabitsIntent.CreateHabit -> screenModelScope.launch {
                createHabit(
                    name = intent.name,
                    description = null,
                    icon = intent.icon,
                    color = intent.color,
                    frequency = intent.frequency,
                    targetDaysPerWeek = 7
                )
                _state.update { it.copy(showAddSheet = false) }
            }
            HabitsIntent.ShowAddSheet -> _state.update { it.copy(showAddSheet = true) }
            HabitsIntent.HideAddSheet -> _state.update { it.copy(showAddSheet = false) }
        }
    }
}
