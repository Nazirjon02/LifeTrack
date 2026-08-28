package tj.mahram.lifetrack.domain.usecase.debt

import tj.mahram.lifetrack.domain.model.Debt
import tj.mahram.lifetrack.domain.repository.DebtRepository

/**
 * Applies a repayment toward a debt. The new paid total is clamped to the
 * principal; once it reaches the principal the debt auto-settles.
 */
class RecordDebtPaymentUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(debt: Debt, payment: Double) {
        if (payment <= 0.0) return
        val newPaid = (debt.paidAmount + payment).coerceIn(0.0, debt.amount)
        val settled = newPaid >= debt.amount && debt.amount > 0
        repository.recordPayment(debt.id, newPaid, settled)
    }
}
