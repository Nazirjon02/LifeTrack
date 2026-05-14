package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import tj.mahram.lifetrack.domain.model.FinanceSummary
import tj.mahram.lifetrack.domain.model.Transaction
import tj.mahram.lifetrack.domain.model.TransactionType

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    fun getTransactionsByDateRange(start: Instant, end: Instant): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Long): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    suspend fun getSumByTypeAndDateRange(type: TransactionType, start: Instant, end: Instant): Double
    suspend fun getFinanceSummary(start: Instant, end: Instant): FinanceSummary
}
