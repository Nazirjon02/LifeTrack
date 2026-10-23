package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.domain.model.Wallet
import tj.mahram.lifetrack.domain.model.WalletType
import tj.mahram.lifetrack.Wallet as DbWallet

class WalletLocalDataSource(private val db: AppDatabase) {

    fun getAllWallets(): Flow<List<Wallet>> =
        db.walletQueries.selectAllWallets().asFlow().mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }

    suspend fun getWallet(id: String): Wallet? = withContext(Dispatchers.Default) {
        db.walletQueries.selectWalletById(id).executeAsOneOrNull()?.toDomain()
    }

    suspend fun insert(wallet: Wallet): Unit = withContext(Dispatchers.Default) {
        db.walletQueries.insertWallet(
            id = wallet.id,
            name = wallet.name,
            type = wallet.type.name,
            balance = wallet.balance,
            currency = wallet.currency,
            icon = wallet.icon,
            color = wallet.color,
            createdAt = wallet.createdAt.toEpochMilliseconds(),
            updatedAt = wallet.updatedAt.toEpochMilliseconds(),
            isArchived = if (wallet.isArchived) 1L else 0L,
            interestRate = wallet.interestRate,
            startDate = wallet.startDate?.toEpochMilliseconds(),
            endDate = wallet.endDate?.toEpochMilliseconds()
        )
        Unit
    }

    suspend fun update(wallet: Wallet): Unit = withContext(Dispatchers.Default) {
        db.walletQueries.updateWallet(
            name = wallet.name,
            type = wallet.type.name,
            balance = wallet.balance,
            currency = wallet.currency,
            icon = wallet.icon,
            color = wallet.color,
            isArchived = if (wallet.isArchived) 1L else 0L,
            interestRate = wallet.interestRate,
            startDate = wallet.startDate?.toEpochMilliseconds(),
            endDate = wallet.endDate?.toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
            id = wallet.id
        )
        Unit
    }

    suspend fun delete(id: String): Unit = withContext(Dispatchers.Default) {
        db.walletQueries.deleteWallet(id)
        Unit
    }

    private fun DbWallet.toDomain() = Wallet(
        id = id,
        name = name,
        type = runCatching { WalletType.valueOf(type) }.getOrDefault(WalletType.CASH),
        balance = balance,
        currency = currency,
        icon = icon,
        color = color,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        isArchived = isArchived == 1L,
        interestRate = interestRate,
        startDate = startDate?.let { Instant.fromEpochMilliseconds(it) },
        endDate = endDate?.let { Instant.fromEpochMilliseconds(it) }
    )
}
