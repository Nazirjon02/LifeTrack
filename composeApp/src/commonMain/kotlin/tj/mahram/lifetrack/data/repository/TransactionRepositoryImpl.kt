package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import tj.mahram.lifetrack.data.local.TransactionLocalDataSource
import tj.mahram.lifetrack.domain.model.FinanceSummary
import tj.mahram.lifetrack.domain.model.Transaction
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.domain.repository.TransactionRepository

class TransactionRepositoryImpl(
    private val local: TransactionLocalDataSource
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> = local.getAllTransactions()

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> =
        local.getByType(type)

    override fun getTransactionsByDateRange(start: Instant, end: Instant): Flow<List<Transaction>> =
        local.getByDateRange(start.toEpochMilliseconds(), end.toEpochMilliseconds())

    override fun getRecentTransactions(limit: Long): Flow<List<Transaction>> =
        local.getRecent(limit)

    override suspend fun addTransaction(transaction: Transaction) = local.insert(transaction)

    override suspend fun updateTransaction(transaction: Transaction) {
        local.delete(transaction.id)
        local.insert(transaction)
    }

    override suspend fun deleteTransaction(id: String) = local.delete(id)

    override suspend fun getSumByTypeAndDateRange(
        type: TransactionType,
        start: Instant,
        end: Instant
    ): Double = local.getSumByTypeAndRange(type.name, start.toEpochMilliseconds(), end.toEpochMilliseconds())

    override suspend fun getFinanceSummary(start: Instant, end: Instant): FinanceSummary {
        val income = getSumByTypeAndDateRange(TransactionType.INCOME, start, end)
        val expense = getSumByTypeAndDateRange(TransactionType.EXPENSE, start, end)
        return FinanceSummary(
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            currency = "USD"
        )
    }
}
