package tj.mahram.lifetrack.feature.finance

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.domain.repository.CategoryRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.debt.ObserveDebtSummaryUseCase
import tj.mahram.lifetrack.domain.usecase.finance.*

class FinanceScreenModel(
    private val getAllTransactions: GetAllTransactionsUseCase,
    private val addTransaction: AddTransactionUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
    private val observeBalance: ObserveBalanceUseCase,
    private val observeDebtSummary: ObserveDebtSummaryUseCase,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(FinanceState())
    val state: StateFlow<FinanceState> = _state.asStateFlow()

    init {
        observeAll()
    }

    fun handleIntent(intent: FinanceIntent) {
        when (intent) {
            is FinanceIntent.DeleteTransaction -> screenModelScope.launch {
                deleteTransaction(intent.id)
            }
            is FinanceIntent.SelectType -> _state.update { it.copy(selectedType = intent.type) }
            FinanceIntent.ShowAddExpense -> _state.update {
                it.copy(showAddSheet = true, addSheetType = TransactionType.EXPENSE)
            }
            FinanceIntent.ShowAddIncome -> _state.update {
                it.copy(showAddSheet = true, addSheetType = TransactionType.INCOME)
            }
            FinanceIntent.HideAddSheet -> _state.update { it.copy(showAddSheet = false) }
            FinanceIntent.ShowAdjustBalance -> _state.update { it.copy(showAdjustBalance = true) }
            FinanceIntent.HideAdjustBalance -> _state.update { it.copy(showAdjustBalance = false) }
            is FinanceIntent.SetCurrentBalance -> screenModelScope.launch {
                // Store the opening balance so that current balance == the value
                // the user entered, given the existing ledger net.
                val overview = _state.value.balance
                val ledgerNet = if (overview != null) overview.totalIncome - overview.totalExpense else 0.0
                settingsRepository.setOpeningBalance(intent.amount - ledgerNet)
                _state.update { it.copy(showAdjustBalance = false) }
            }
            is FinanceIntent.AddTransaction -> screenModelScope.launch {
                try {
                    addTransaction(
                        amount = intent.amount,
                        type = intent.type,
                        categoryId = intent.categoryId,
                        note = intent.note,
                        currency = _state.value.currency
                    )
                    _state.update { it.copy(showAddSheet = false) }
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.message) }
                }
            }
        }
    }

    private fun observeAll() {
        screenModelScope.launch {
            settingsRepository.getSettings().collectLatest { settings ->
                _state.update { it.copy(currency = settings.currency) }
            }
        }
        screenModelScope.launch {
            categoryRepository.getAllCategories().collectLatest { cats ->
                _state.update { it.copy(categories = cats) }
            }
        }
        screenModelScope.launch {
            getAllTransactions().collectLatest { txns ->
                _state.update { it.copy(isLoading = false, transactions = txns) }
            }
        }
        screenModelScope.launch {
            observeBalance().collectLatest { overview ->
                _state.update { it.copy(balance = overview) }
            }
        }
        screenModelScope.launch {
            observeDebtSummary().collectLatest { summary ->
                _state.update { it.copy(debtSummary = summary) }
            }
        }
    }
}
