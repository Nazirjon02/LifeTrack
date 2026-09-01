package tj.mahram.lifetrack.domain.model

import kotlin.time.Instant

/** How urgent a problem is. Drives sort order and the card's colour accent. */
enum class ProblemPriority { LOW, MEDIUM, HIGH }

/** Lifecycle of a problem the user is working through. */
enum class ProblemStatus { ACTIVE, IN_PROGRESS, RESOLVED }

/**
 * A problem the user wants to solve. Beyond a title & description it captures
 * possible [solutions] and a concrete [actionPlan], a [priority] and [status],
 * a free-text [category] (used by analytics), a 0..100 [progress] value and an
 * optional [dueDate] used to place it on the calendar. Every meaningful change
 * is mirrored into [ProblemHistoryEntry] rows so the user keeps an audit trail.
 */
data class Problem(
    val id: String,
    val title: String,
    val description: String?,
    val solutions: String?,
    val actionPlan: String?,
    val priority: ProblemPriority,
    val status: ProblemStatus,
    val category: String,
    val progress: Int,
    val color: String,
    val dueDate: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val resolvedAt: Instant?
) {
    val progressFraction: Float
        get() = (progress / 100f).coerceIn(0f, 1f)

    val isResolved: Boolean get() = status == ProblemStatus.RESOLVED
}

/** One recorded change in a problem's life (creation, status flip, progress…). */
data class ProblemHistoryEntry(
    val id: String,
    val problemId: String,
    val message: String,
    val createdAt: Instant
)

/**
 * Aggregated statistics over the whole problem list, recomputed whenever the
 * list changes. Powers the Analytics view.
 */
data class ProblemAnalytics(
    val totalCreated: Int = 0,
    val totalResolved: Int = 0,
    val totalActive: Int = 0,
    val createdThisWeek: Int = 0,
    val resolvedThisWeek: Int = 0,
    val createdThisMonth: Int = 0,
    val resolvedThisMonth: Int = 0,
    val avgProgress: Int = 0,
    /** Resolved count per day for the last 7 days, oldest → newest. */
    val resolvedPerDay: List<Pair<String, Int>> = emptyList(),
    /** Category → number of problems, most frequent first. */
    val topCategories: List<Pair<String, Int>> = emptyList()
) {
    val overallRate: Float get() = if (totalCreated == 0) 0f else totalResolved.toFloat() / totalCreated
    val weeklyRate: Float get() = if (createdThisWeek == 0) 0f else (resolvedThisWeek.toFloat() / createdThisWeek).coerceIn(0f, 1f)
    val monthlyRate: Float get() = if (createdThisMonth == 0) 0f else (resolvedThisMonth.toFloat() / createdThisMonth).coerceIn(0f, 1f)
    val hasData: Boolean get() = totalCreated > 0
}
