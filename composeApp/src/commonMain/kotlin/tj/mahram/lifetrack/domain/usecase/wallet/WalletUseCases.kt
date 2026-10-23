package tj.mahram.lifetrack.domain.usecase.wallet

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.Clock
import kotlin.time.Instant
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.domain.model.Wallet
import tj.mahram.lifetrack.domain.model.WalletType
import tj.mahram.lifetrack.domain.model.WalletWithBalance
import tj.mahram.lifetrack.domain.model.WalletsOverview
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.repository.TransactionRepository
import tj.mahram.lifetrack.domain.repository.WalletRepository

class GetAllWalletsUseCase(private val repository: WalletRepository) {
    operator fun invoke(): Flow<List<Wallet>> = repository.getAllWallets()
}

/**
 * Emits every wallet paired with its current balance (base balance plus the net
 * of the transactions assigned to it) and keeps it live as wallets or the ledger
 * change. Deposits keep their principal as the balance — accrued interest is a
 * projection, not part of the live number.
 */
class ObserveWalletsOverviewUseCase(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<WalletsOverview> =
        combine(
            walletRepository.getAllWallets(),
            transactionRepository.getAllTransactions(),
            settingsRepository.getSettings()
        ) { wallets, transactions, settings ->
            val netByWallet = HashMap<String, Double>()
            for (t in transactions) {
                val wid = t.walletId ?: continue
                val signed = if (t.type == TransactionType.INCOME) t.amount else -t.amount
                netByWallet[wid] = (netByWallet[wid] ?: 0.0) + signed
            }
            WalletsOverview(
                wallets = wallets
                    .filter { !it.isArchived }
                    .map { WalletWithBalance(it, it.balance + (netByWallet[it.id] ?: 0.0)) },
                currency = settings.currency
            )
        }
}

class CreateWalletUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(
        name: String,
        type: WalletType,
        balance: Double,
        currency: String,
        icon: String,
        color: String,
        interestRate: Double? = null,
        startDate: Instant? = null,
        endDate: Instant? = null
    ) {
        require(name.isNotBlank()) { "Name must not be blank" }
        val now = Clock.System.now()
        repository.createWallet(
            Wallet(
                id = generateUuid(),
                name = name.trim(),
                type = type,
                balance = balance,
                currency = currency,
                icon = icon,
                color = color,
                createdAt = now,
                updatedAt = now,
                isArchived = false,
                interestRate = if (type == WalletType.DEPOSIT) interestRate else null,
                startDate = if (type == WalletType.DEPOSIT) startDate else null,
                endDate = if (type == WalletType.DEPOSIT) endDate else null
            )
        )
    }
}

class UpdateWalletUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(wallet: Wallet) {
        require(wallet.name.isNotBlank()) { "Name must not be blank" }
        repository.updateWallet(wallet.copy(updatedAt = Clock.System.now()))
    }
}

class DeleteWalletUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(id: String) = repository.deleteWallet(id)
}

private fun generateUuid(): String {
    val chars = "0123456789abcdef"
    fun randomHex(length: Int) = (1..length).map { chars.random() }.joinToString("")
    return "${randomHex(8)}-${randomHex(4)}-4${randomHex(3)}-${listOf("8", "9", "a", "b").random()}${randomHex(3)}-${randomHex(12)}"
}
