package tj.mahram.lifetrack.domain.usecase.debt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tj.mahram.lifetrack.domain.model.DebtSummary
import tj.mahram.lifetrack.domain.model.DebtType
import tj.mahram.lifetrack.domain.repository.DebtRepository

/** Live aggregate of outstanding debts, derived from the debts list. */
class ObserveDebtSummaryUseCase(private val repository: DebtRepository) {
    operator fun invoke(): Flow<DebtSummary> = repository.getAllDebts().map { debts ->
        val active = debts.filter { !it.isSettled }
        DebtSummary(
            totalLent = active.filter { it.type == DebtType.LENT }.sumOf { it.remaining },
            totalBorrowed = active.filter { it.type == DebtType.BORROWED }.sumOf { it.remaining },
            activeLentCount = active.count { it.type == DebtType.LENT },
            activeBorrowedCount = active.count { it.type == DebtType.BORROWED }
        )
    }
}
