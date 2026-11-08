package tj.mahram.lifetrack.feature.planner

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.model.CategoryType
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

    // Only one task subscription at a time. Each search used to start another
    // collector without stopping the previous ones, so on any database change
    // they all re-emitted and an old query's results could overwrite the list.
    // (Declared before init: initializers run in order, and init starts it.)
    private var tasksJob: Job? = null

    // Sample tasks are added once, on a fresh install's first launch, by
    // InitializeAppDataUseCase — not here (that re-created them whenever the
    // list became empty).
    init {
        observeTasks()
        observeCategories()
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
                observeTasks(intent.query)
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

    private fun observeTasks(query: String = "") {
        tasksJob?.cancel()
        tasksJob = screenModelScope.launch {
            val source = if (query.isBlank()) getAllTasks() else searchTasks(query)
            source.collectLatest { tasks ->
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

}
