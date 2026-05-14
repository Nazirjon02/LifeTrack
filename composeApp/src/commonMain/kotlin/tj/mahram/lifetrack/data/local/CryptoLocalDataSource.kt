package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.domain.model.CryptoPriceAlert
import tj.mahram.lifetrack.domain.model.CryptoPortfolioItem
import tj.mahram.lifetrack.domain.model.CryptoWatchlistItem

class CryptoLocalDataSource(private val db: AppDatabase) {

    fun getPortfolioItems(): Flow<List<CryptoPortfolioItem>> =
        db.cryptoPortfolioQueries.selectAllPortfolioItems().asFlow().mapToList(Dispatchers.IO)
            .map { list ->
                list.map { item ->
                    CryptoPortfolioItem(
                        id = item.id,
                        coinId = item.coinId,
                        coinSymbol = item.coinSymbol,
                        coinName = item.coinName,
                        amount = item.amount,
                        purchasePrice = item.purchasePrice
                    )
                }
            }

    suspend fun insertPortfolioItem(item: CryptoPortfolioItem) {
        withContext(Dispatchers.IO) {
            db.cryptoPortfolioQueries.insertPortfolioItem(
                id = item.id,
                coinId = item.coinId,
                coinSymbol = item.coinSymbol,
                coinName = item.coinName,
                amount = item.amount,
                purchasePrice = item.purchasePrice,
                addedAt = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    suspend fun updatePortfolioItemAmount(id: String, amount: Double) {
        withContext(Dispatchers.IO) {
            db.cryptoPortfolioQueries.updatePortfolioItemAmount(amount = amount, id = id)
        }
    }

    suspend fun deletePortfolioItem(id: String) {
        withContext(Dispatchers.IO) {
            db.cryptoPortfolioQueries.deletePortfolioItem(id)
        }
    }

    fun getWatchlist(): Flow<List<CryptoWatchlistItem>> =
        db.cryptoPortfolioQueries.selectAllWatchlistItems().asFlow().mapToList(Dispatchers.IO)
            .map { list ->
                list.map { item ->
                    CryptoWatchlistItem(
                        coinId = item.coinId,
                        coinSymbol = item.coinSymbol,
                        coinName = item.coinName
                    )
                }
            }

    suspend fun insertWatchlistItem(item: CryptoWatchlistItem) {
        withContext(Dispatchers.IO) {
            db.cryptoPortfolioQueries.insertWatchlistItem(
                coinId = item.coinId,
                coinSymbol = item.coinSymbol,
                coinName = item.coinName,
                addedAt = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    suspend fun deleteWatchlistItem(coinId: String) {
        withContext(Dispatchers.IO) {
            db.cryptoPortfolioQueries.deleteWatchlistItem(coinId)
        }
    }

    suspend fun isInWatchlist(coinId: String): Boolean = withContext(Dispatchers.IO) {
        db.cryptoPortfolioQueries.isInWatchlist(coinId).executeAsOne() > 0
    }

    fun getPriceAlerts(): Flow<List<CryptoPriceAlert>> =
        db.cryptoPortfolioQueries.selectAllPriceAlerts().asFlow().mapToList(Dispatchers.IO)
            .map { list ->
                list.map { alert ->
                    CryptoPriceAlert(
                        id = alert.id,
                        coinId = alert.coinId,
                        coinSymbol = alert.coinSymbol,
                        targetPrice = alert.targetPrice,
                        isAbove = alert.isAbove == 1L,
                        isTriggered = alert.isTriggered == 1L
                    )
                }
            }

    suspend fun insertPriceAlert(alert: CryptoPriceAlert) {
        withContext(Dispatchers.IO) {
            db.cryptoPortfolioQueries.insertPriceAlert(
                id = alert.id,
                coinId = alert.coinId,
                coinSymbol = alert.coinSymbol,
                targetPrice = alert.targetPrice,
                isAbove = if (alert.isAbove) 1L else 0L,
                isTriggered = if (alert.isTriggered) 1L else 0L,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    suspend fun deletePriceAlert(id: String) {
        withContext(Dispatchers.IO) {
            db.cryptoPortfolioQueries.deletePriceAlert(id)
        }
    }

    suspend fun getPortfolioItemByCoinId(coinId: String): CryptoPortfolioItem? = withContext(Dispatchers.IO) {
        db.cryptoPortfolioQueries.selectPortfolioItemByCoinId(coinId).executeAsOneOrNull()?.let { item ->
            CryptoPortfolioItem(
                id = item.id,
                coinId = item.coinId,
                coinSymbol = item.coinSymbol,
                coinName = item.coinName,
                amount = item.amount,
                purchasePrice = item.purchasePrice
            )
        }
    }
}
