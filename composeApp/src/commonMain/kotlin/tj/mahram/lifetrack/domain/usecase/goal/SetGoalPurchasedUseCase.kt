package tj.mahram.lifetrack.domain.usecase.goal

import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import tj.mahram.lifetrack.domain.model.Goal
import tj.mahram.lifetrack.domain.model.GoalPurchaseCategory
import tj.mahram.lifetrack.domain.model.Transaction
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.domain.repository.CategoryRepository
import tj.mahram.lifetrack.domain.repository.GoalRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.repository.TransactionRepository

/**
 * Marks a "purchase" goal as bought (or reverts it).
 *
 * Buying it completes the goal and books an expense of its price into the
 * ledger, which lowers the central balance. Reverting removes that expense.
 * The ledger entry is the single mechanism through which the goal touches the
 * balance — nothing here computes the balance itself.
 */
class SetGoalPurchasedUseCase(
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(goal: Goal, purchased: Boolean) {
        if (purchased) {
            // Completing the goal: currentValue == targetValue marks it done.
            goalRepository.updateGoalProgress(goal.id, goal.targetValue)
            if (goal.affectsBalance && goal.targetValue > 0) {
                categoryRepository.ensureCategory(GoalPurchaseCategory)
                val currency = settingsRepository.getSettings().first().currency
                // Delete-then-insert keeps the deterministic id idempotent.
                transactionRepository.deleteTransaction(goal.linkedTransactionId)
                transactionRepository.addTransaction(
                    Transaction(
                        id = goal.linkedTransactionId,
                        amount = goal.targetValue,
                        type = TransactionType.EXPENSE,
                        categoryId = GoalPurchaseCategory.id,
                        note = goal.title,
                        date = Clock.System.now(),
                        currency = currency,
                        isRecurring = false,
                        recurringType = null,
                        cryptoCoinId = null
                    )
                )
            }
        } else {
            // Reverting: reset progress and remove the generated expense.
            goalRepository.updateGoalProgress(goal.id, 0.0)
            if (goal.affectsBalance) {
                transactionRepository.deleteTransaction(goal.linkedTransactionId)
            }
        }
    }
}
