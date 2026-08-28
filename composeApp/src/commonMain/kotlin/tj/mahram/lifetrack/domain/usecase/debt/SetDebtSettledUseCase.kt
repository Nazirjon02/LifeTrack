package tj.mahram.lifetrack.domain.usecase.debt

import tj.mahram.lifetrack.domain.model.Debt
import tj.mahram.lifetrack.domain.repository.DebtRepository

/**
 * Closes a debt outright (marks it fully repaid) or re-opens it. Closing sets
 * the paid amount to the full principal; re-opening keeps whatever was recorded.
 */
class SetDebtSettledUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(debt: Debt, settled: Boolean) {
        val paid = if (settled) debt.amount else debt.paidAmount.coerceIn(0.0, debt.amount)
        repository.setSettled(debt.id, settled, paid)
    }
}
