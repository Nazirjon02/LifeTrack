package tj.mahram.lifetrack.domain.model

import kotlin.time.Instant

data class Goal(
    val id: String,
    val title: String,
    val description: String?,
    val icon: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val color: String,
    val deadline: Instant?,
    val isCompleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    /**
     * When true this goal is a "purchase": its [targetValue] is a price in the
     * app currency, and completing it books an expense against the central
     * balance (reversed when it is un-completed or deleted).
     */
    val affectsBalance: Boolean = false
) {
    val progressFraction: Float
        get() = if (targetValue > 0) (currentValue / targetValue).toFloat().coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()

    /** Deterministic id of the ledger entry generated for a purchase goal. */
    val linkedTransactionId: String
        get() = linkedTransactionIdFor(id)

    companion object {
        fun linkedTransactionIdFor(goalId: String): String = "goal_$goalId"
    }
}
