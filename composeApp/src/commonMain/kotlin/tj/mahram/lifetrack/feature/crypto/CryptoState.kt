package tj.mahram.lifetrack.feature.crypto

import tj.mahram.lifetrack.domain.model.CryptoCoin
import tj.mahram.lifetrack.domain.model.CryptoPortfolioItem
import tj.mahram.lifetrack.domain.model.CryptoWatchlistItem
import tj.mahram.lifetrack.domain.model.PortfolioSummary

data class CryptoState(
    val isLoadingCoins: Boolean = true,
    val isLoadingPortfolio: Boolean = false,
    val topCoins: List<CryptoCoin> = emptyList(),
    val searchResults: List<CryptoCoin> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val portfolioItems: List<CryptoPortfolioItem> = emptyList(),
    val portfolioSummary: PortfolioSummary? = null,
    val watchlist: List<CryptoWatchlistItem> = emptyList(),
    val selectedTab: CryptoTab = CryptoTab.MARKET,
    val currency: String = "USD",
    val error: String? = null,
    val showAddToPortfolioFor: CryptoCoin? = null,
    val selectedCoin: CryptoCoin? = null,
    val selectedCoinHistory: List<Double> = emptyList(),
    val selectedHistoryDays: Int = 7
) {
    val displayedCoins: List<CryptoCoin>
        get() = if (isSearching && searchQuery.isNotBlank()) searchResults else topCoins
}

enum class CryptoTab(val label: String) {
    MARKET("Market"),
    PORTFOLIO("Portfolio"),
    WATCHLIST("Watchlist")
}

sealed class CryptoIntent {
    data class Search(val query: String) : CryptoIntent()
    data object ClearSearch : CryptoIntent()
    data class SelectTab(val tab: CryptoTab) : CryptoIntent()
    data class SelectCoin(val coin: CryptoCoin) : CryptoIntent()
    data object ClearSelectedCoin : CryptoIntent()
    data class AddToPortfolio(val coin: CryptoCoin, val amount: Double, val purchasePrice: Double) : CryptoIntent()
    data class RemoveFromPortfolio(val id: String) : CryptoIntent()
    data class ToggleWatchlist(val coin: CryptoCoin) : CryptoIntent()
    data object Refresh : CryptoIntent()
    data class ShowAddPortfolioFor(val coin: CryptoCoin) : CryptoIntent()
    data object HideAddPortfolio : CryptoIntent()
    data class SelectHistoryRange(val days: Int) : CryptoIntent()
}
