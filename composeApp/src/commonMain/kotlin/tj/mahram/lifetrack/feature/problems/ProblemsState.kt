package tj.mahram.lifetrack.feature.problems

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.domain.model.ProblemAnalytics
import tj.mahram.lifetrack.domain.model.ProblemHistoryEntry
import tj.mahram.lifetrack.domain.model.ProblemPriority
import tj.mahram.lifetrack.domain.model.ProblemStatus
import tj.mahram.lifetrack.domain.model.Task

/** Which of the three sub-views of the Problems tab is showing. */
enum class ProblemsView { LIST, CALENDAR, ANALYTICS }

/** Granularity of the calendar view. */
enum class CalendarMode { MONTH, WEEK, DAY }

private fun today(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

data class ProblemsState(
    val isLoading: Boolean = true,
    val problems: List<Problem> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val view: ProblemsView = ProblemsView.LIST,
    val analytics: ProblemAnalytics = ProblemAnalytics(),

    // Sheets / dialogs
    val showAddSheet: Boolean = false,
    val editing: Problem? = null,
    val detail: Problem? = null,
    val detailHistory: List<ProblemHistoryEntry> = emptyList(),
    val progressTarget: Problem? = null,

    // Calendar selection
    val calendarMode: CalendarMode = CalendarMode.MONTH,
    val selectedYear: Int = today().year,
    val selectedMonth: Int = today().month.number,
    val selectedDay: Int = today().day,
) {
    val activeProblems: List<Problem> get() = problems.filter { it.status == ProblemStatus.ACTIVE }
    val inProgressProblems: List<Problem> get() = problems.filter { it.status == ProblemStatus.IN_PROGRESS }
    val resolvedProblems: List<Problem> get() = problems.filter { it.status == ProblemStatus.RESOLVED }
    val unresolvedCount: Int get() = problems.count { it.status != ProblemStatus.RESOLVED }

    val selectedDate: LocalDate
        get() = runCatching { LocalDate(selectedYear, selectedMonth, selectedDay) }.getOrElse { today() }

    val todayDate: LocalDate get() = today()

    private val tz get() = TimeZone.currentSystemDefault()

    fun problemsForDay(date: LocalDate): List<Problem> = problems.filter { p ->
        p.dueDate?.toLocalDateTime(tz)?.date == date
    }

    fun tasksForDay(date: LocalDate): List<Task> = tasks.filter { t ->
        t.dueDate?.toLocalDateTime(tz)?.date == date
    }

    fun problemCountForDay(date: LocalDate): Int = problemsForDay(date).size
    fun taskCountForDay(date: LocalDate): Int = tasksForDay(date).size
}

sealed class ProblemsIntent {
    data class SetView(val view: ProblemsView) : ProblemsIntent()

    data object ShowAddSheet : ProblemsIntent()
    data object HideAddSheet : ProblemsIntent()
    data class ShowEdit(val problem: Problem) : ProblemsIntent()

    data class Create(
        val title: String,
        val description: String?,
        val solutions: String?,
        val actionPlan: String?,
        val priority: ProblemPriority,
        val category: String,
        val color: String,
        val dueDateMillis: Long?
    ) : ProblemsIntent()

    data class Update(
        val original: Problem,
        val title: String,
        val description: String?,
        val solutions: String?,
        val actionPlan: String?,
        val priority: ProblemPriority,
        val category: String,
        val color: String,
        val dueDateMillis: Long?
    ) : ProblemsIntent()

    data class ShowDetail(val problem: Problem) : ProblemsIntent()
    data object HideDetail : ProblemsIntent()

    data class ShowProgress(val problem: Problem) : ProblemsIntent()
    data object HideProgress : ProblemsIntent()
    data class SetProgress(val problemId: String, val progress: Int) : ProblemsIntent()

    data class SetStatus(val problem: Problem, val status: ProblemStatus) : ProblemsIntent()
    data class Delete(val problemId: String) : ProblemsIntent()

    // Calendar
    data class SetCalendarMode(val mode: CalendarMode) : ProblemsIntent()
    data class SelectDay(val year: Int, val month: Int, val day: Int) : ProblemsIntent()
    data class NavigateMonth(val year: Int, val month: Int) : ProblemsIntent()
}
