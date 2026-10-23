package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.Wallet

interface WalletRepository {
    fun getAllWallets(): Flow<List<Wallet>>
    suspend fun getWallet(id: String): Wallet?
    suspend fun createWallet(wallet: Wallet)
    suspend fun updateWallet(wallet: Wallet)
    suspend fun deleteWallet(id: String)
}
