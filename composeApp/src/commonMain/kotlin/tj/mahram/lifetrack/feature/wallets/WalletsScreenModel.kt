package tj.mahram.lifetrack.feature.wallets

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tj.mahram.lifetrack.domain.model.Wallet
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.finance.GetAllTransactionsUseCase
import tj.mahram.lifetrack.domain.usecase.wallet.CreateWalletUseCase
import tj.mahram.lifetrack.domain.usecase.wallet.DeleteWalletUseCase
import tj.mahram.lifetrack.domain.usecase.wallet.ObserveWalletsOverviewUseCase
import tj.mahram.lifetrack.domain.usecase.wallet.UpdateWalletUseCase

class WalletsScreenModel(
    private val observeOverview: ObserveWalletsOverviewUseCase,
    private val getAllTransactions: GetAllTransactionsUseCase,
    private val createWallet: CreateWalletUseCase,
    private val updateWallet: UpdateWalletUseCase,
    private val deleteWallet: DeleteWalletUseCase,
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(WalletsState())
    val state: StateFlow<WalletsState> = _state.asStateFlow()

    init {
        observe()
    }

    fun handleIntent(intent: WalletsIntent) {
        when (intent) {
            WalletsIntent.ShowAddSheet -> _state.update { it.copy(showSheet = true, editing = null) }
            is WalletsIntent.ShowEditSheet -> _state.update { it.copy(showSheet = true, editing = intent.wallet) }
            WalletsIntent.HideSheet -> _state.update { it.copy(showSheet = false, editing = null) }
            is WalletsIntent.SaveWallet -> screenModelScope.launch {
                try {
                    val d = intent.draft
                    val currency = _state.value.currency
                    if (d.id == null) {
                        createWallet(
                            name = d.name,
                            type = d.type,
                            balance = d.balance,
                            currency = currency,
                            icon = d.icon,
                            color = d.color,
                            interestRate = d.interestRate,
                            startDate = d.startDate,
                            endDate = d.endDate
                        )
                    } else {
                        updateWallet(
                            Wallet(
                                id = d.id,
                                name = d.name,
                                type = d.type,
                                balance = d.balance,
                                currency = currency,
                                icon = d.icon,
                                color = d.color,
                                createdAt = d.createdAt ?: Clock.System.now(),
                                updatedAt = Clock.System.now(),
                                isArchived = _state.value.editing?.isArchived ?: false,
                                interestRate = d.interestRate,
                                startDate = d.startDate,
                                endDate = d.endDate
                            )
                        )
                    }
                    _state.update { it.copy(showSheet = false, editing = null) }
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.message) }
                }
            }
            is WalletsIntent.RequestDelete -> _state.update { it.copy(deleteTarget = intent.wallet) }
            WalletsIntent.CancelDelete -> _state.update { it.copy(deleteTarget = null) }
            is WalletsIntent.ConfirmDelete -> screenModelScope.launch {
                deleteWallet(intent.id)
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
        screenModelScope.launch {
            getAllTransactions().collectLatest { txns ->
                _state.update { it.copy(transactions = txns) }
            }
        }
    }
}
