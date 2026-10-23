package tj.mahram.lifetrack.feature.investments

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.model.Investment
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.investment.CreateInvestmentUseCase
import tj.mahram.lifetrack.domain.usecase.investment.DeleteInvestmentUseCase
import tj.mahram.lifetrack.domain.usecase.investment.ObserveInvestmentsOverviewUseCase
import tj.mahram.lifetrack.domain.usecase.investment.UpdateInvestmentUseCase

class InvestmentsScreenModel(
    private val observeOverview: ObserveInvestmentsOverviewUseCase,
    private val createInvestment: CreateInvestmentUseCase,
    private val updateInvestment: UpdateInvestmentUseCase,
    private val deleteInvestment: DeleteInvestmentUseCase,
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(InvestmentsState())
    val state: StateFlow<InvestmentsState> = _state.asStateFlow()

    init {
        observe()
    }

    fun handleIntent(intent: InvestmentsIntent) {
        when (intent) {
            InvestmentsIntent.ShowAddSheet -> _state.update { it.copy(showSheet = true, editing = null) }
            is InvestmentsIntent.ShowEditSheet -> _state.update { it.copy(showSheet = true, editing = intent.investment) }
            InvestmentsIntent.HideSheet -> _state.update { it.copy(showSheet = false, editing = null) }
            is InvestmentsIntent.SaveInvestment -> screenModelScope.launch {
                try {
                    val d = intent.draft
                    val currency = _state.value.currency
                    if (d.id == null) {
                        createInvestment(
                            name = d.name,
                            type = d.type,
                            amountInvested = d.amountInvested,
                            currentValue = d.currentValue,
                            currency = currency,
                            note = d.note,
                            icon = d.icon,
                            color = d.color
                        )
                    } else {
                        updateInvestment(
                            Investment(
                                id = d.id,
                                name = d.name,
                                type = d.type,
                                amountInvested = d.amountInvested,
                                currentValue = d.currentValue,
                                currency = currency,
                                note = d.note,
                                icon = d.icon,
                                color = d.color,
                                createdAt = d.createdAt ?: Clock.System.now(),
                                updatedAt = Clock.System.now()
                            )
                        )
                    }
                    _state.update { it.copy(showSheet = false, editing = null) }
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.message) }
                }
            }
            is InvestmentsIntent.RequestDelete -> _state.update { it.copy(deleteTarget = intent.investment) }
            InvestmentsIntent.CancelDelete -> _state.update { it.copy(deleteTarget = null) }
            is InvestmentsIntent.ConfirmDelete -> screenModelScope.launch {
                deleteInvestment(intent.id)
                _state.update { it.copy(deleteTarget = null) }
            }
        }
    }

    private fun observe() {
        screenModelScope.launch {
            settingsRepository.getSettings().collectLatest { settings ->
                _state.update { it.copy(currency = settings.currency, language = settings.language) }
            }
        }
        screenModelScope.launch {
            observeOverview().collectLatest { overview ->
                _state.update { it.copy(isLoading = false, overview = overview) }
            }
        }
    }
}
