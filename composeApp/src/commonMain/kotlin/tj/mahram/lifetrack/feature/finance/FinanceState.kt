package tj.mahram.lifetrack.feature.finance

import tj.mahram.lifetrack.domain.model.BalanceOverview
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.Transaction
import tj.mahram.lifetrack.domain.model.TransactionType

data class FinanceState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val balance: BalanceOverview? = null,
    val currency: String = "USD",
    val selectedType: TransactionType? = null,
    val showAddSheet: Boolean = false,
    val addSheetType: TransactionType = TransactionType.EXPENSE,
    val showAdjustBalance: Boolean = false,
    val error: String? = null
) {
    val filteredTransactions: List<Transaction>
        get() = if (selectedType == null) transactions else transactions.filter { it.type == selectedType }

    fun categoryById(id: String): Category? = categories.find { it.id == id }
}

sealed class FinanceIntent {
    data class DeleteTransaction(val id: String) : FinanceIntent()
    data class SelectType(val type: TransactionType?) : FinanceIntent()
    data object ShowAddExpense : FinanceIntent()
    data object ShowAddIncome : FinanceIntent()
    data object HideAddSheet : FinanceIntent()
    data object ShowAdjustBalance : FinanceIntent()
    data object HideAdjustBalance : FinanceIntent()
    /** The balance the user says they actually have right now. */
    data class SetCurrentBalance(val amount: Double) : FinanceIntent()
    data class AddTransaction(
        val amount: Double,
        val type: TransactionType,
        val categoryId: String,
        val note: String?
    ) : FinanceIntent()
}
