package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.data.local.TransactionLocalDataSource
import tj.mahram.lifetrack.data.local.WalletLocalDataSource
import tj.mahram.lifetrack.data.sync.SyncCollectionNames
import tj.mahram.lifetrack.data.sync.SyncTracker
import tj.mahram.lifetrack.domain.model.Wallet
import tj.mahram.lifetrack.domain.repository.WalletRepository

class WalletRepositoryImpl(
    private val wallets: WalletLocalDataSource,
    private val transactions: TransactionLocalDataSource,
    private val sync: SyncTracker
) : WalletRepository {

    override fun getAllWallets(): Flow<List<Wallet>> = wallets.getAllWallets()

    override suspend fun getWallet(id: String): Wallet? = wallets.getWallet(id)

    override suspend fun createWallet(wallet: Wallet) {
        wallets.insert(wallet)
        sync.markDirty(SyncCollectionNames.WALLETS, wallet.id)
    }

    override suspend fun updateWallet(wallet: Wallet) {
        wallets.update(wallet)
        sync.markDirty(SyncCollectionNames.WALLETS, wallet.id)
    }

    override suspend fun deleteWallet(id: String) {
        // Keep the ledger entries but detach them from the deleted wallet, and
        // push those unassignments to sync so other devices match.
        val affected = transactions.idsByWallet(id)
        transactions.clearWallet(id)
        affected.forEach { sync.markDirty(SyncCollectionNames.TRANSACTIONS, it) }
        wallets.delete(id)
        sync.markDeleted(SyncCollectionNames.WALLETS, id)
    }
}
