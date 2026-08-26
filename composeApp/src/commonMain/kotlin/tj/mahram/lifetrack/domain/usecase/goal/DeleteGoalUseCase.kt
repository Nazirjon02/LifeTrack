package tj.mahram.lifetrack.domain.usecase.goal

import tj.mahram.lifetrack.domain.model.Goal
import tj.mahram.lifetrack.domain.repository.GoalRepository
import tj.mahram.lifetrack.domain.repository.TransactionRepository

class DeleteGoalUseCase(
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(goalId: String) {
        // Remove any ledger entry a purchase goal generated so the central
        // balance is restored. Harmless no-op for goals that never had one.
        transactionRepository.deleteTransaction(Goal.linkedTransactionIdFor(goalId))
        goalRepository.deleteGoal(goalId)
    }
}
