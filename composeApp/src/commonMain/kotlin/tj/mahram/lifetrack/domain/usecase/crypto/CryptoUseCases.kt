package tj.mahram.lifetrack.domain.usecase.crypto

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.CryptoCoin
import tj.mahram.lifetrack.domain.model.CryptoPriceAlert
import tj.mahram.lifetrack.domain.model.CryptoPortfolioItem
import tj.mahram.lifetrack.domain.model.CryptoWatchlistItem
import tj.mahram.lifetrack.domain.model.PortfolioSummary
import tj.mahram.lifetrack.domain.repository.CryptoRepository

class GetTopCoinsUseCase(private val repository: CryptoRepository) {
    suspend operator fun invoke(currency: String = "usd", page: Int = 1): Result<List<CryptoCoin>> =
        repository.getTopCoins(currency, page)
}

class SearchCoinsUseCase(private val repository: CryptoRepository) {
    suspend operator fun invoke(query: String): Result<List<CryptoCoin>> =
        repository.searchCoins(query)
}

class GetCoinDetailUseCase(private val repository: CryptoRepository) {
    suspend operator fun invoke(coinId: String): Result<CryptoCoin> =
        repository.getCoinById(coinId)
}

class GetCoinPriceHistoryUseCase(private val repository: CryptoRepository) {
    suspend operator fun invoke(coinId: String, days: Int): Result<List<Double>> =
        repository.getCoinPriceHistory(coinId, days)
}

class GetPortfolioItemsUseCase(private val repository: CryptoRepository) {
    operator fun invoke(): Flow<List<CryptoPortfolioItem>> = repository.getPortfolioItems()
}

class AddToPortfolioUseCase(private val repository: CryptoRepository) {
    suspend operator fun invoke(
        coinId: String,
        coinSymbol: String,
        coinName: String,
        amount: Double,
        purchasePrice: Double
    ) {
        require(amount > 0) { "Amount must be positive" }
        val item = CryptoPortfolioItem(
            id = generateUuid(),
            coinId = coinId,
            coinSymbol = coinSymbol,
            coinName = coinName,
            amount = amount,
            purchasePrice = purchasePrice
        )
        repository.addToPortfolio(item)
    }
}

class RemoveFromPortfolioUseCase(private val repository: CryptoRepository) {
    suspend operator fun invoke(id: String) = repository.removeFromPortfolio(id)
}

class GetWatchlistUseCase(private val repository: CryptoRepository) {
    operator fun invoke(): Flow<List<CryptoWatchlistItem>> = repository.getWatchlist()
}

class ToggleWatchlistUseCase(private val repository: CryptoRepository) {
    suspend operator fun invoke(coin: CryptoCoin) {
        val isWatching = repository.isInWatchlist(coin.id)
        if (isWatching) {
            repository.removeFromWatchlist(coin.id)
        } else {
            repository.addToWatchlist(CryptoWatchlistItem(coin.id, coin.symbol, coin.name))
        }
    }
}

class GetPortfolioSummaryUseCase(private val repository: CryptoRepository) {
    suspend operator fun invoke(currency: String = "usd"): Result<PortfolioSummary> =
        repository.getPortfolioSummary(currency)
}

class AddPriceAlertUseCase(private val repository: CryptoRepository) {
    suspend operator fun invoke(
        coinId: String,
        coinSymbol: String,
        targetPrice: Double,
        isAbove: Boolean
    ) {
        val alert = CryptoPriceAlert(
            id = generateUuid(),
            coinId = coinId,
            coinSymbol = coinSymbol,
            targetPrice = targetPrice,
            isAbove = isAbove,
            isTriggered = false
        )
        repository.addPriceAlert(alert)
    }
}

class GetPriceAlertsUseCase(private val repository: CryptoRepository) {
    operator fun invoke(): Flow<List<CryptoPriceAlert>> = repository.getPriceAlerts()
}

private fun generateUuid(): String {
    val chars = "0123456789abcdef"
    fun randomHex(length: Int) = (1..length).map { chars.random() }.joinToString("")
    return "${randomHex(8)}-${randomHex(4)}-4${randomHex(3)}-${listOf("8","9","a","b").random()}${randomHex(3)}-${randomHex(12)}"
}
