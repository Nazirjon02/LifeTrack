package tj.mahram.lifetrack.feature.wallets

import kotlin.time.Instant
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.Transaction
import tj.mahram.lifetrack.domain.model.Wallet
import tj.mahram.lifetrack.domain.model.WalletType
import tj.mahram.lifetrack.domain.model.WalletsOverview

data class WalletsState(
    val isLoading: Boolean = true,
    val overview: WalletsOverview = WalletsOverview(),
    val transactions: List<Transaction> = emptyList(),
    val language: AppLanguage = AppLanguage.ENGLISH,
    val currency: String = "USD",
    val showSheet: Boolean = false,
    val editing: Wallet? = null,
    val deleteTarget: Wallet? = null,
    val error: String? = null
) {
    /** The net of transactions assigned to [walletId]. */
    fun walletLedgerNet(walletId: String): Double =
        overview.wallets.firstOrNull { it.wallet.id == walletId }
            ?.let { it.currentBalance - it.wallet.balance } ?: 0.0
}

/** Everything the create/edit sheet produces; [id] null means "create". */
data class WalletDraft(
    val id: String?,
    val name: String,
    val type: WalletType,
    val balance: Double,
    val icon: String,
    val color: String,
    val interestRate: Double?,
    val startDate: Instant?,
    val endDate: Instant?,
    val createdAt: Instant?
)

sealed class WalletsIntent {
    data object ShowAddSheet : WalletsIntent()
    data class ShowEditSheet(val wallet: Wallet) : WalletsIntent()
    data object HideSheet : WalletsIntent()
    data class SaveWallet(val draft: WalletDraft) : WalletsIntent()
    data class RequestDelete(val wallet: Wallet) : WalletsIntent()
    data object CancelDelete : WalletsIntent()
    data class ConfirmDelete(val id: String) : WalletsIntent()
}
