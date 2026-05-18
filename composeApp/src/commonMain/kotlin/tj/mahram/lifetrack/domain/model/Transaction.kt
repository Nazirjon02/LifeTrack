package tj.mahram.lifetrack.domain.model

import kotlin.time.Instant

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val note: String?,
    val date: Instant,
    val currency: String,
    val isRecurring: Boolean,
    val recurringType: RecurringType?,
    val cryptoCoinId: String?
)

enum class TransactionType(val label: String) {
    INCOME("Income"),
    EXPENSE("Expense")
}

data class FinanceSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val currency: String
)
