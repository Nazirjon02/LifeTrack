package tj.mahram.lifetrack.feature.planner

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.model.CategoryType
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.domain.repository.CategoryRepository
import tj.mahram.lifetrack.domain.usecase.task.*

class PlannerScreenModel(
    private val getAllTasks: GetAllTasksUseCase,
    private val createTask: CreateTaskUseCase,
    private val toggleTask: ToggleTaskCompletionUseCase,
    private val deleteTask: DeleteTaskUseCase,
    private val searchTasks: SearchTasksUseCase,
    private val categoryRepository: CategoryRepository
) : ScreenModel {

    private val _state = MutableStateFlow(PlannerState())
    val state: StateFlow<PlannerState> = _state.asStateFlow()

    init {
        observeTasks()
        observeCategories()
        seedDefaultTasksIfEmpty()
    }

    fun handleIntent(intent: PlannerIntent) {
        when (intent) {
            is PlannerIntent.ToggleTask -> screenModelScope.launch {
                toggleTask(intent.taskId, intent.isCompleted)
            }
            is PlannerIntent.DeleteTask -> screenModelScope.launch {
                deleteTask(intent.taskId)
            }
            is PlannerIntent.SelectCategory ->
                _state.update { it.copy(selectedCategoryId = intent.categoryId) }

            is PlannerIntent.Search -> {
                _state.update { it.copy(searchQuery = intent.query) }
                if (intent.query.isNotBlank()) {
                    screenModelScope.launch {
                        searchTasks(intent.query).collectLatest { tasks ->
                            _state.update { it.copy(tasks = tasks) }
                        }
                    }
                } else {
                    observeTasks()
                }
            }

            PlannerIntent.ShowAddTask -> _state.update { it.copy(showAddTaskSheet = true) }
            PlannerIntent.HideAddTask -> _state.update { it.copy(showAddTaskSheet = false) }

            // View switching
            is PlannerIntent.SetView -> _state.update { it.copy(plannerView = intent.view) }

            // Navigate within same view (no view change)
            is PlannerIntent.NavigateYear ->
                _state.update { it.copy(selectedYear = intent.year) }
            is PlannerIntent.NavigateMonth ->
                _state.update { it.copy(selectedYear = intent.year, selectedMonth = intent.month) }
            is PlannerIntent.NavigateDay ->
                _state.update { it.copy(selectedYear = intent.year, selectedMonth = intent.month, selectedDay = intent.day) }

            // Drill-down (switches view)
            is PlannerIntent.SelectMonth ->
                _state.update { it.copy(selectedYear = intent.year, selectedMonth = intent.month, plannerView = PlannerView.MONTH) }
            is PlannerIntent.SelectDay ->
                _state.update { it.copy(selectedYear = intent.year, selectedMonth = intent.month, selectedDay = intent.day, plannerView = PlannerView.DAY) }

            is PlannerIntent.CreateTask -> screenModelScope.launch {
                try {
                    createTask(
                        title = intent.title,
                        description = intent.description,
                        priority = intent.priority,
                        categoryId = intent.categoryId,
                        dueDateMillis = intent.dueDateMillis
                    )
                    _state.update { it.copy(showAddTaskSheet = false) }
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.message) }
                }
            }
        }
    }

    private fun observeTasks() {
        screenModelScope.launch {
            getAllTasks().collectLatest { tasks ->
                _state.update { it.copy(isLoading = false, tasks = tasks) }
            }
        }
    }

    private fun observeCategories() {
        screenModelScope.launch {
            categoryRepository.getCategoriesByType(CategoryType.TASK).collectLatest { cats ->
                _state.update { it.copy(categories = cats) }
            }
        }
    }

    private fun seedDefaultTasksIfEmpty() {
        screenModelScope.launch {
            delay(400L)
            val existing = getAllTasks().first()
            if (existing.isNotEmpty()) return@launch

            val today = Clock.System.now().toEpochMilliseconds()
            val defaultTasks = listOf(
                Triple("Deep work session 🎯",    TaskPriority.CRITICAL, "Focus on your most important project"),
                Triple("Morning workout 💪",       TaskPriority.HIGH,     "Start the day strong and energized"),
                Triple("Plan tomorrow's goals 📋", TaskPriority.HIGH,     "End the day with tomorrow clarified"),
                Triple("Read for 30 minutes 📚",   TaskPriority.MEDIUM,   "Expand your knowledge daily"),
                Triple("Review notes & ideas 📝",  TaskPriority.MEDIUM,   "Keep your projects on track"),
                Triple("Meditate 10 minutes 🧘",   TaskPriority.LOW,      "Clear your mind and reduce stress"),
                Triple("Connect with a friend 🤝", TaskPriority.LOW,      "Relationships are your superpower"),
            )
            defaultTasks.forEach { (title, priority, description) ->
                runCatching {
                    createTask(
                        title = title,
                        description = description,
                        priority = priority,
                        dueDateMillis = today
                    )
                }
            }
        }
    }
}
