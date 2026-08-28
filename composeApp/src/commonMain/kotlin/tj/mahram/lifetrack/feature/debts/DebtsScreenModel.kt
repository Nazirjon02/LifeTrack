package tj.mahram.lifetrack.feature.debts

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.debt.CreateDebtUseCase
import tj.mahram.lifetrack.domain.usecase.debt.DeleteDebtUseCase
import tj.mahram.lifetrack.domain.usecase.debt.GetAllDebtsUseCase
import tj.mahram.lifetrack.domain.usecase.debt.RecordDebtPaymentUseCase
import tj.mahram.lifetrack.domain.usecase.debt.SetDebtSettledUseCase

class DebtsScreenModel(
    private val getAllDebts: GetAllDebtsUseCase,
    private val createDebt: CreateDebtUseCase,
    private val recordPayment: RecordDebtPaymentUseCase,
    private val setSettled: SetDebtSettledUseCase,
    private val deleteDebt: DeleteDebtUseCase,
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(DebtsState())
    val state: StateFlow<DebtsState> = _state.asStateFlow()

    init {
        observe()
    }

    fun handleIntent(intent: DebtsIntent) {
        when (intent) {
            is DebtsIntent.SetFilter -> _state.update { it.copy(filter = intent.filter) }
            is DebtsIntent.ShowAddSheet -> _state.update {
                it.copy(showAddSheet = true, addPresetType = intent.type)
            }
            DebtsIntent.HideAddSheet -> _state.update { it.copy(showAddSheet = false) }
            is DebtsIntent.CreateDebt -> screenModelScope.launch {
                try {
                    createDebt(
                        type = intent.type,
                        counterparty = intent.counterparty,
                        amount = intent.amount,
                        currency = _state.value.currency,
                        note = intent.note,
                        dueDate = intent.dueDate,
                        color = intent.color
                    )
                    _state.update { it.copy(showAddSheet = false) }
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.message) }
                }
            }
            is DebtsIntent.ShowPayment -> _state.update { it.copy(paymentTarget = intent.debt) }
            DebtsIntent.HidePayment -> _state.update { it.copy(paymentTarget = null) }
            is DebtsIntent.RecordPayment -> screenModelScope.launch {
                recordPayment(intent.debt, intent.amount)
                _state.update { it.copy(paymentTarget = null) }
            }
            is DebtsIntent.SetSettled -> screenModelScope.launch {
                setSettled(intent.debt, intent.settled)
                _state.update { it.copy(paymentTarget = null) }
            }
            is DebtsIntent.DeleteDebt -> screenModelScope.launch {
                deleteDebt(intent.id)
            }
        }
    }

    private fun observe() {
        screenModelScope.launch {
            settingsRepository.getSettings().collectLatest { settings ->
                _state.update { it.copy(currency = settings.currency) }
            }
        }
        screenModelScope.launch {
            getAllDebts().collectLatest { debts ->
                _state.update { it.copy(isLoading = false, debts = debts) }
            }
        }
    }
}
