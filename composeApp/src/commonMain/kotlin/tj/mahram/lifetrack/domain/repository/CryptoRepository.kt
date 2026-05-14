package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.CryptoCoin
import tj.mahram.lifetrack.domain.model.CryptoPriceAlert
import tj.mahram.lifetrack.domain.model.CryptoPortfolioItem
import tj.mahram.lifetrack.domain.model.CryptoWatchlistItem
import tj.mahram.lifetrack.domain.model.PortfolioSummary

interface CryptoRepository {
    suspend fun getTopCoins(currency: String = "usd", page: Int = 1): Result<List<CryptoCoin>>
    suspend fun searchCoins(query: String): Result<List<CryptoCoin>>
    suspend fun getCoinById(id: String): Result<CryptoCoin>
    suspend fun getCoinPriceHistory(id: String, days: Int): Result<List<Double>>
    fun getPortfolioItems(): Flow<List<CryptoPortfolioItem>>
    suspend fun addToPortfolio(item: CryptoPortfolioItem)
    suspend fun updatePortfolioItem(id: String, amount: Double)
    suspend fun removeFromPortfolio(id: String)
    fun getWatchlist(): Flow<List<CryptoWatchlistItem>>
    suspend fun addToWatchlist(item: CryptoWatchlistItem)
    suspend fun removeFromWatchlist(coinId: String)
    suspend fun isInWatchlist(coinId: String): Boolean
    fun getPriceAlerts(): Flow<List<CryptoPriceAlert>>
    suspend fun addPriceAlert(alert: CryptoPriceAlert)
    suspend fun deletePriceAlert(id: String)
    suspend fun getPortfolioSummary(currency: String = "usd"): Result<PortfolioSummary>
}
