package tj.mahram.lifetrack.feature.problems

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import tj.mahram.lifetrack.core.i18n.AppStrings
import tj.mahram.lifetrack.core.i18n.EnglishStrings
import tj.mahram.lifetrack.core.i18n.stringsFor
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.domain.model.ProblemAnalytics
import tj.mahram.lifetrack.domain.model.ProblemPriority
import tj.mahram.lifetrack.domain.model.ProblemStatus
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.problem.CreateProblemUseCase
import tj.mahram.lifetrack.domain.usecase.problem.DeleteProblemUseCase
import tj.mahram.lifetrack.domain.usecase.problem.GetAllProblemsUseCase
import tj.mahram.lifetrack.domain.usecase.problem.GetProblemHistoryUseCase
import tj.mahram.lifetrack.domain.usecase.problem.UpdateProblemProgressUseCase
import tj.mahram.lifetrack.domain.usecase.problem.UpdateProblemStatusUseCase
import tj.mahram.lifetrack.domain.usecase.problem.UpdateProblemUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetAllTasksUseCase

class ProblemsScreenModel(
    private val getAllProblems: GetAllProblemsUseCase,
    private val getProblemHistory: GetProblemHistoryUseCase,
    private val createProblem: CreateProblemUseCase,
    private val updateProblem: UpdateProblemUseCase,
    private val updateStatus: UpdateProblemStatusUseCase,
    private val updateProgress: UpdateProblemProgressUseCase,
    private val deleteProblem: DeleteProblemUseCase,
    private val getAllTasks: GetAllTasksUseCase,
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(ProblemsState())
    val state: StateFlow<ProblemsState> = _state.asStateFlow()

    /** Latest strings, kept in sync with the language setting for history logs. */
    private var strings: AppStrings = EnglishStrings
    private var historyJob: Job? = null

    init {
        screenModelScope.launch {
            getAllProblems().collect { problems ->
                _state.update {
                    it.copy(
                        problems = problems,
                        isLoading = false,
                        analytics = computeAnalytics(problems)
                    )
                }
            }
        }
        screenModelScope.launch {
            getAllTasks().collect { tasks -> _state.update { it.copy(tasks = tasks) } }
        }
        screenModelScope.launch {
            settingsRepository.getSettings().collect { strings = stringsFor(it.language) }
        }
    }

    fun handleIntent(intent: ProblemsIntent) {
        when (intent) {
            is ProblemsIntent.SetView -> _state.update { it.copy(view = intent.view) }

            ProblemsIntent.ShowAddSheet -> _state.update { it.copy(showAddSheet = true, editing = null) }
            ProblemsIntent.HideAddSheet -> _state.update { it.copy(showAddSheet = false, editing = null) }
            is ProblemsIntent.ShowEdit -> _state.update { it.copy(editing = intent.problem, showAddSheet = true) }

            is ProblemsIntent.Create -> screenModelScope.launch {
                createProblem(
                    title = intent.title,
                    description = intent.description,
                    solutions = intent.solutions,
                    actionPlan = intent.actionPlan,
                    priority = intent.priority,
                    category = intent.category,
                    color = intent.color,
                    dueDate = intent.dueDateMillis?.let { Instant.fromEpochMilliseconds(it) },
                    historyMessage = strings.historyCreated
                )
                _state.update { it.copy(showAddSheet = false, editing = null) }
            }

            is ProblemsIntent.Update -> screenModelScope.launch {
                val edited = intent.original.copy(
                    title = intent.title.trim(),
                    description = intent.description?.trim()?.ifEmpty { null },
                    solutions = intent.solutions?.trim()?.ifEmpty { null },
                    actionPlan = intent.actionPlan?.trim()?.ifEmpty { null },
                    priority = intent.priority,
                    category = intent.category.trim(),
                    color = intent.color,
                    dueDate = intent.dueDateMillis?.let { Instant.fromEpochMilliseconds(it) }
                )
                updateProblem(edited, strings.historyEdited)
                _state.update { it.copy(showAddSheet = false, editing = null) }
            }

            is ProblemsIntent.ShowDetail -> openDetail(intent.problem)
            ProblemsIntent.HideDetail -> {
                historyJob?.cancel()
                _state.update { it.copy(detail = null, detailHistory = emptyList()) }
            }

            is ProblemsIntent.ShowProgress -> _state.update { it.copy(progressTarget = intent.problem) }
            ProblemsIntent.HideProgress -> _state.update { it.copy(progressTarget = null) }
            is ProblemsIntent.SetProgress -> screenModelScope.launch {
                updateProgress(intent.problemId, intent.progress, strings.historyProgress(intent.progress))
                _state.update { it.copy(progressTarget = null) }
            }

            is ProblemsIntent.SetStatus -> screenModelScope.launch {
                updateStatus(intent.problem.id, intent.status, strings.historyStatusChanged(strings.problemStatusLabel(intent.status)))
            }

            is ProblemsIntent.Delete -> screenModelScope.launch {
                deleteProblem(intent.problemId)
                _state.update { if (it.detail?.id == intent.problemId) it.copy(detail = null) else it }
            }

            is ProblemsIntent.SetCalendarMode -> _state.update { it.copy(calendarMode = intent.mode) }
            is ProblemsIntent.SelectDay -> _state.update {
                it.copy(selectedYear = intent.year, selectedMonth = intent.month, selectedDay = intent.day)
            }
            is ProblemsIntent.NavigateMonth -> _state.update {
                it.copy(selectedYear = intent.year, selectedMonth = intent.month)
            }
        }
    }

    private fun openDetail(problem: Problem) {
        _state.update { it.copy(detail = problem, detailHistory = emptyList()) }
        historyJob?.cancel()
        historyJob = screenModelScope.launch {
            getProblemHistory(problem.id).collect { history ->
                _state.update { st -> if (st.detail?.id == problem.id) st.copy(detailHistory = history) else st }
            }
        }
    }

    private fun computeAnalytics(problems: List<Problem>): ProblemAnalytics {
        if (problems.isEmpty()) return ProblemAnalytics()
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val weekStart = today.minus(6, DateTimeUnit.DAY).atStartOfDayIn(tz)
        val monthStart = today.minus(29, DateTimeUnit.DAY).atStartOfDayIn(tz)

        val resolved = problems.filter { it.isResolved }
        val createdThisWeek = problems.count { it.createdAt >= weekStart }
        val resolvedThisWeek = resolved.count { (it.resolvedAt ?: it.updatedAt) >= weekStart }
        val createdThisMonth = problems.count { it.createdAt >= monthStart }
        val resolvedThisMonth = resolved.count { (it.resolvedAt ?: it.updatedAt) >= monthStart }

        val perDay = (6 downTo 0).map { offset ->
            val day = today.minus(offset, DateTimeUnit.DAY)
            val count = resolved.count { p ->
                (p.resolvedAt ?: p.updatedAt).toLocalDateTime(tz).date == day
            }
            day.day.toString() to count
        }

        val topCategories = problems
            .groupBy { it.category.trim().ifEmpty { strings.analyticsUncategorized } }
            .map { (cat, list) -> cat to list.size }
            .sortedByDescending { it.second }
            .take(5)

        val avgProgress = if (problems.isEmpty()) 0
        else problems.sumOf { it.progress } / problems.size

        return ProblemAnalytics(
            totalCreated = problems.size,
            totalResolved = resolved.size,
            totalActive = problems.count { it.status != ProblemStatus.RESOLVED },
            createdThisWeek = createdThisWeek,
            resolvedThisWeek = resolvedThisWeek,
            createdThisMonth = createdThisMonth,
            resolvedThisMonth = resolvedThisMonth,
            avgProgress = avgProgress,
            resolvedPerDay = perDay,
            topCategories = topCategories
        )
    }
}

/** Default palette used when creating a problem, keyed loosely to priority. */
val ProblemColors = listOf("#7C3AED", "#EF4444", "#F59E0B", "#0EA5E9", "#10B981", "#EC4899", "#6366F1", "#14B8A6")

fun defaultColorForPriority(priority: ProblemPriority): String = when (priority) {
    ProblemPriority.HIGH -> "#EF4444"
    ProblemPriority.MEDIUM -> "#F59E0B"
    ProblemPriority.LOW -> "#10B981"
}
