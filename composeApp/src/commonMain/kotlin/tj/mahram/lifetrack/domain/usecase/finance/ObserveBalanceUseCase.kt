package tj.mahram.lifetrack.domain.usecase.finance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import tj.mahram.lifetrack.core.util.endOfMonth
import tj.mahram.lifetrack.core.util.startOfMonth
import tj.mahram.lifetrack.domain.model.BalanceOverview
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.repository.TransactionRepository

/**
 * Emits the app's single central balance and keeps it live: it recomputes
 * whenever the ledger changes (any transaction — including the ones generated
 * by purchase goals) or the opening balance / currency changes.
 */
class ObserveBalanceUseCase(
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<BalanceOverview> =
        combine(
            transactionRepository.getAllTransactions(),
            settingsRepository.getSettings()
        ) { transactions, settings ->
            val monthStart = startOfMonth().toEpochMilliseconds()
            val monthEnd = endOfMonth().toEpochMilliseconds()

            var totalIncome = 0.0
            var totalExpense = 0.0
            var monthIncome = 0.0
            var monthExpense = 0.0

            for (t in transactions) {
                val inMonth = t.date.toEpochMilliseconds() in monthStart..monthEnd
                when (t.type) {
                    TransactionType.INCOME -> {
                        totalIncome += t.amount
                        if (inMonth) monthIncome += t.amount
                    }
                    TransactionType.EXPENSE -> {
                        totalExpense += t.amount
                        if (inMonth) monthExpense += t.amount
                    }
                }
            }

            BalanceOverview(
                openingBalance = settings.openingBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                monthIncome = monthIncome,
                monthExpense = monthExpense,
                currency = settings.currency
            )
        }
}
