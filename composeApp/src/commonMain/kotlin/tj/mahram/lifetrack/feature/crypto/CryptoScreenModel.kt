package tj.mahram.lifetrack.feature.crypto

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.usecase.crypto.*

class CryptoScreenModel(
    private val getTopCoins: GetTopCoinsUseCase,
    private val searchCoins: SearchCoinsUseCase,
    private val getPortfolioItems: GetPortfolioItemsUseCase,
    private val getPortfolioSummary: GetPortfolioSummaryUseCase,
    private val addToPortfolio: AddToPortfolioUseCase,
    private val removeFromPortfolio: RemoveFromPortfolioUseCase,
    private val getWatchlist: GetWatchlistUseCase,
    private val toggleWatchlist: ToggleWatchlistUseCase,
    private val getCoinHistory: GetCoinPriceHistoryUseCase,
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(CryptoState())
    val state: StateFlow<CryptoState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var refreshJob: Job? = null

    init {
        observeSettings()
        observePortfolio()
        observeWatchlist()
        loadTopCoins()
        startAutoRefresh()
    }

    fun handleIntent(intent: CryptoIntent) {
        when (intent) {
            is CryptoIntent.Search -> {
                _state.update { it.copy(searchQuery = intent.query, isSearching = intent.query.isNotBlank()) }
                if (intent.query.isNotBlank()) {
                    searchJob?.cancel()
                    searchJob = screenModelScope.launch {
                        delay(400)
                        searchCoins(intent.query).onSuccess { coins ->
                            _state.update { it.copy(searchResults = coins) }
                        }
                    }
                }
            }
            CryptoIntent.ClearSearch -> _state.update { it.copy(searchQuery = "", isSearching = false, searchResults = emptyList()) }
            is CryptoIntent.SelectTab -> _state.update { it.copy(selectedTab = intent.tab) }
            is CryptoIntent.SelectCoin -> {
                _state.update { it.copy(selectedCoin = intent.coin) }
                loadCoinHistory(intent.coin.id, _state.value.selectedHistoryDays)
            }
            CryptoIntent.ClearSelectedCoin -> _state.update { it.copy(selectedCoin = null, selectedCoinHistory = emptyList()) }
            is CryptoIntent.AddToPortfolio -> screenModelScope.launch {
                addToPortfolio(
                    coinId = intent.coin.id,
                    coinSymbol = intent.coin.symbol,
                    coinName = intent.coin.name,
                    amount = intent.amount,
                    purchasePrice = intent.purchasePrice
                )
                _state.update { it.copy(showAddToPortfolioFor = null) }
            }
            is CryptoIntent.RemoveFromPortfolio -> screenModelScope.launch {
                removeFromPortfolio(intent.id)
            }
            is CryptoIntent.ToggleWatchlist -> screenModelScope.launch {
                toggleWatchlist(intent.coin)
            }
            CryptoIntent.Refresh -> loadTopCoins()
            is CryptoIntent.ShowAddPortfolioFor -> _state.update { it.copy(showAddToPortfolioFor = intent.coin) }
            CryptoIntent.HideAddPortfolio -> _state.update { it.copy(showAddToPortfolioFor = null) }
            is CryptoIntent.SelectHistoryRange -> {
                _state.update { it.copy(selectedHistoryDays = intent.days) }
                _state.value.selectedCoin?.let { loadCoinHistory(it.id, intent.days) }
            }
        }
    }

    private fun loadTopCoins() {
        screenModelScope.launch {
            _state.update { it.copy(isLoadingCoins = true, error = null) }
            getTopCoins(_state.value.currency.lowercase())
                .onSuccess { coins -> _state.update { it.copy(topCoins = coins, isLoadingCoins = false) } }
                .onFailure { e -> _state.update { it.copy(isLoadingCoins = false, error = e.message) } }
        }
    }

    private fun loadCoinHistory(coinId: String, days: Int) {
        screenModelScope.launch {
            getCoinHistory(coinId, days).onSuccess { history ->
                _state.update { it.copy(selectedCoinHistory = history) }
            }
        }
    }

    private fun observePortfolio() {
        screenModelScope.launch {
            getPortfolioItems().collectLatest { items ->
                _state.update { it.copy(portfolioItems = items) }
                val summary = getPortfolioSummary(_state.value.currency.lowercase()).getOrNull()
                _state.update { it.copy(portfolioSummary = summary) }
            }
        }
    }

    private fun observeWatchlist() {
        screenModelScope.launch {
            getWatchlist().collectLatest { list ->
                _state.update { it.copy(watchlist = list) }
            }
        }
    }

    private fun observeSettings() {
        screenModelScope.launch {
            settingsRepository.getSettings().collectLatest { settings ->
                _state.update { it.copy(currency = settings.currency) }
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob = screenModelScope.launch {
            while (isActive) {
                delay(60_000)
                loadTopCoins()
            }
        }
    }

    override fun onDispose() {
        refreshJob?.cancel()
        super.onDispose()
    }
}
