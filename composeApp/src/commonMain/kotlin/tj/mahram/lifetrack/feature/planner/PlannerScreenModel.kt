package tj.mahram.lifetrack.feature.planner

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
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
            is PlannerIntent.SelectCategory -> _state.update { it.copy(selectedCategoryId = intent.categoryId) }
            is PlannerIntent.Search -> {
                _state.update { it.copy(searchQuery = intent.query) }
                if (intent.query.isNotBlank()) {
                    screenModelScope.launch {
                        searchTasks(intent.query).collectLatest { tasks ->
                            _state.update { it.copy(tasks = tasks) }
                        }
                    }
                } else observeTasks()
            }
            PlannerIntent.ShowAddTask -> _state.update { it.copy(showAddTaskSheet = true) }
            PlannerIntent.HideAddTask -> _state.update { it.copy(showAddTaskSheet = false) }
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
}
