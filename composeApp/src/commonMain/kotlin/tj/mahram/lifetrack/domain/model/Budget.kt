package tj.mahram.lifetrack.domain.model

import kotlinx.datetime.Instant

data class Budget(
    val id: String,
    val categoryId: String,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: Instant,
    val endDate: Instant,
    val spent: Double = 0.0
) {
    val progress: Float get() = if (amount > 0) (spent / amount).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget: Boolean get() = spent > amount
    val remaining: Double get() = amount - spent
}

enum class BudgetPeriod(val label: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}
