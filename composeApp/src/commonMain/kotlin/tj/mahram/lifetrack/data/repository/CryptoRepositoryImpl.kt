package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import tj.mahram.lifetrack.data.local.CryptoLocalDataSource
import tj.mahram.lifetrack.data.remote.CoinGeckoApi
import tj.mahram.lifetrack.domain.model.CryptoCoin
import tj.mahram.lifetrack.domain.model.CryptoPriceAlert
import tj.mahram.lifetrack.domain.model.CryptoPortfolioItem
import tj.mahram.lifetrack.domain.model.CryptoWatchlistItem
import tj.mahram.lifetrack.domain.model.PortfolioSummary
import tj.mahram.lifetrack.domain.repository.CryptoRepository

class CryptoRepositoryImpl(
    private val api: CoinGeckoApi,
    private val local: CryptoLocalDataSource
) : CryptoRepository {

    override suspend fun getTopCoins(currency: String, page: Int): Result<List<CryptoCoin>> =
        runCatching { api.getTopCoins(currency, page) }

    override suspend fun searchCoins(query: String): Result<List<CryptoCoin>> =
        runCatching { api.searchCoins(query) }

    override suspend fun getCoinById(id: String): Result<CryptoCoin> =
        runCatching { api.getCoinById(id) }

    override suspend fun getCoinPriceHistory(id: String, days: Int): Result<List<Double>> =
        runCatching { api.getCoinPriceHistory(id, days) }

    override fun getPortfolioItems(): Flow<List<CryptoPortfolioItem>> = local.getPortfolioItems()

    override suspend fun addToPortfolio(item: CryptoPortfolioItem) = local.insertPortfolioItem(item)

    override suspend fun updatePortfolioItem(id: String, amount: Double) =
        local.updatePortfolioItemAmount(id, amount)

    override suspend fun removeFromPortfolio(id: String) = local.deletePortfolioItem(id)

    override fun getWatchlist(): Flow<List<CryptoWatchlistItem>> = local.getWatchlist()

    override suspend fun addToWatchlist(item: CryptoWatchlistItem) = local.insertWatchlistItem(item)

    override suspend fun removeFromWatchlist(coinId: String) = local.deleteWatchlistItem(coinId)

    override suspend fun isInWatchlist(coinId: String): Boolean = local.isInWatchlist(coinId)

    override fun getPriceAlerts(): Flow<List<CryptoPriceAlert>> = local.getPriceAlerts()

    override suspend fun addPriceAlert(alert: CryptoPriceAlert) = local.insertPriceAlert(alert)

    override suspend fun deletePriceAlert(id: String) = local.deletePriceAlert(id)

    override suspend fun getPortfolioSummary(currency: String): Result<PortfolioSummary> = runCatching {
        val localItems = local.getPortfolioItems().first()
        if (localItems.isEmpty()) return@runCatching PortfolioSummary(
            totalValue = 0.0,
            totalInvested = 0.0,
            totalPnl = 0.0,
            totalPnlPercent = 0.0,
            items = emptyList()
        )
        val coinIds = localItems.map { it.coinId }.distinct().joinToString(",")
        val prices: List<CryptoCoin> = runCatching {
            val allCoins = api.getTopCoins(currency, 1) + api.getTopCoins(currency, 2)
            allCoins.filter { coin -> localItems.any { it.coinId == coin.id } }
        }.getOrDefault(emptyList())
        val priceMap = prices.associate { it.id to it.currentPrice }
        val enrichedItems = localItems.map { item ->
            item.copy(currentPrice = priceMap[item.coinId] ?: item.purchasePrice)
        }
        val totalValue = enrichedItems.sumOf { it.currentValue }
        val totalInvested = enrichedItems.sumOf { it.investedValue }
        val totalPnl = totalValue - totalInvested
        val totalPnlPercent = if (totalInvested > 0) (totalPnl / totalInvested) * 100 else 0.0
        PortfolioSummary(
            totalValue = totalValue,
            totalInvested = totalInvested,
            totalPnl = totalPnl,
            totalPnlPercent = totalPnlPercent,
            items = enrichedItems
        )
    }
}
