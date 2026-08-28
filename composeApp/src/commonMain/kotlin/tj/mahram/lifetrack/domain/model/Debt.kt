package tj.mahram.lifetrack.domain.model

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Direction of a personal debt.
 *
 * [LENT]     — money I gave out; the counterparty owes me (a receivable / asset).
 * [BORROWED] — money I took; I owe the counterparty (a payable / liability).
 */
enum class DebtType { LENT, BORROWED }

/**
 * A single personal debt with someone.
 *
 * Debts are tracked independently of the transaction ledger: they keep their own
 * outstanding totals and never mutate the central cash balance, so lending or
 * borrowing money is not mistaken for spending or income. Partial repayments are
 * supported through [paidAmount].
 */
data class Debt(
    val id: String,
    val type: DebtType,
    val counterparty: String,
    val amount: Double,
    val paidAmount: Double,
    val currency: String,
    val note: String?,
    val dueDate: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isSettled: Boolean,
    val color: String
) {
    /** Money still outstanding on this debt. */
    val remaining: Double
        get() = (amount - paidAmount).coerceAtLeast(0.0)

    /** Fraction repaid, in 0f..1f. */
    val progress: Float
        get() = if (amount > 0) (paidAmount / amount).toFloat().coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progress * 100).toInt()

    /** Past its due date and still open. */
    fun isOverdue(now: Instant = Clock.System.now()): Boolean =
        !isSettled && dueDate != null && dueDate < now
}

/**
 * Aggregate outstanding position across all open debts. Used by the debts screen
 * and by the small entry card on the finance screen.
 */
data class DebtSummary(
    val totalLent: Double = 0.0,       // outstanding money owed to me
    val totalBorrowed: Double = 0.0,   // outstanding money I owe
    val activeLentCount: Int = 0,
    val activeBorrowedCount: Int = 0
) {
    /** Positive → net creditor; negative → net debtor. */
    val net: Double
        get() = totalLent - totalBorrowed

    val hasAny: Boolean
        get() = activeLentCount > 0 || activeBorrowedCount > 0
}
