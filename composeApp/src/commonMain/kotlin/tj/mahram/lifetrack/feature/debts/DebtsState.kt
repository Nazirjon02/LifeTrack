package tj.mahram.lifetrack.feature.debts

import kotlin.time.Clock
import kotlin.time.Instant
import tj.mahram.lifetrack.domain.model.Debt
import tj.mahram.lifetrack.domain.model.DebtSummary
import tj.mahram.lifetrack.domain.model.DebtType

enum class DebtFilter { ALL, LENT, BORROWED }

data class DebtsState(
    val isLoading: Boolean = true,
    val debts: List<Debt> = emptyList(),
    val currency: String = "USD",
    val filter: DebtFilter = DebtFilter.ALL,
    val showAddSheet: Boolean = false,
    val addPresetType: DebtType = DebtType.LENT,
    val paymentTarget: Debt? = null,
    val error: String? = null
) {
    val summary: DebtSummary
        get() {
            val active = debts.filter { !it.isSettled }
            return DebtSummary(
                totalLent = active.filter { it.type == DebtType.LENT }.sumOf { it.remaining },
                totalBorrowed = active.filter { it.type == DebtType.BORROWED }.sumOf { it.remaining },
                activeLentCount = active.count { it.type == DebtType.LENT },
                activeBorrowedCount = active.count { it.type == DebtType.BORROWED }
            )
        }

    /** Open debts, sorted so the ones that need attention float to the top. */
    val visibleDebts: List<Debt>
        get() {
            val now = Clock.System.now()
            val base = when (filter) {
                DebtFilter.ALL -> debts
                DebtFilter.LENT -> debts.filter { it.type == DebtType.LENT }
                DebtFilter.BORROWED -> debts.filter { it.type == DebtType.BORROWED }
            }
            return base.sortedWith(
                compareBy<Debt> { it.isSettled }
                    .thenByDescending { it.isOverdue(now) }
                    .thenBy { it.dueDate?.toEpochMilliseconds() ?: Long.MAX_VALUE }
                    .thenByDescending { it.createdAt.toEpochMilliseconds() }
            )
        }
}

sealed class DebtsIntent {
    data class SetFilter(val filter: DebtFilter) : DebtsIntent()
    data class ShowAddSheet(val type: DebtType) : DebtsIntent()
    data object HideAddSheet : DebtsIntent()
    data class CreateDebt(
        val type: DebtType,
        val counterparty: String,
        val amount: Double,
        val note: String?,
        val dueDate: Instant?,
        val color: String
    ) : DebtsIntent()
    data class ShowPayment(val debt: Debt) : DebtsIntent()
    data object HidePayment : DebtsIntent()
    data class RecordPayment(val debt: Debt, val amount: Double) : DebtsIntent()
    data class SetSettled(val debt: Debt, val settled: Boolean) : DebtsIntent()
    data class DeleteDebt(val id: String) : DebtsIntent()
}
