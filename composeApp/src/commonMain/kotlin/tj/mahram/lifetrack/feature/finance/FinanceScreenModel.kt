package tj.mahram.lifetrack.feature.finance

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.model.CategoryType
import tj.mahram.lifetrack.domain.repository.CategoryRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.finance.*

class FinanceScreenModel(
    private val getAllTransactions: GetAllTransactionsUseCase,
    private val addTransaction: AddTransactionUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
    private val getFinanceSummary: GetFinanceSummaryUseCase,
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
                it.copy(showAddSheet = true, addSheetType = tj.mahram.lifetrack.domain.model.TransactionType.EXPENSE)
            }
            FinanceIntent.ShowAddIncome -> _state.update {
                it.copy(showAddSheet = true, addSheetType = tj.mahram.lifetrack.domain.model.TransactionType.INCOME)
            }
            FinanceIntent.HideAddSheet -> _state.update { it.copy(showAddSheet = false) }
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
                val start = tj.mahram.lifetrack.core.util.startOfMonth()
                val end = tj.mahram.lifetrack.core.util.endOfMonth()
                val summary = try { getFinanceSummary(start, end) } catch (e: Exception) { null }
                _state.update { it.copy(isLoading = false, transactions = txns, summary = summary) }
            }
        }
    }
}
