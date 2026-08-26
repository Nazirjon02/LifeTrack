package tj.mahram.lifetrack.domain.model

/**
 * The app's single central balance.
 *
 * It is derived from one source of truth — the transaction ledger — plus an
 * optional opening balance the user sets once. Every module that touches money
 * (finance transactions today; purchase goals, and anything added later) does
 * so by writing entries into that ledger, so they all flow into this number
 * automatically.
 */
data class BalanceOverview(
    val openingBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val monthIncome: Double,
    val monthExpense: Double,
    val currency: String
) {
    /** Money the user has right now. */
    val currentBalance: Double
        get() = openingBalance + totalIncome - totalExpense

    /** Net change over the current month. */
    val monthNet: Double
        get() = monthIncome - monthExpense
}
