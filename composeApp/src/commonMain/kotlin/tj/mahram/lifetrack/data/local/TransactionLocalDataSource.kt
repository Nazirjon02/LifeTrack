package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.Transaction_record as DbTransaction
import tj.mahram.lifetrack.domain.model.RecurringType
import tj.mahram.lifetrack.domain.model.Transaction
import tj.mahram.lifetrack.domain.model.TransactionType

class TransactionLocalDataSource(private val db: AppDatabase) {

    fun getAllTransactions(): Flow<List<Transaction>> =
        db.transactionRecordQueries.selectAllTransactions().asFlow().mapToList(Dispatchers.Default)
            .map { it.map { t -> t.toDomain() } }

    fun getByType(type: TransactionType): Flow<List<Transaction>> =
        db.transactionRecordQueries.selectTransactionsByType(type.name).asFlow().mapToList(Dispatchers.Default)
            .map { it.map { t -> t.toDomain() } }

    fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>> =
        db.transactionRecordQueries.selectTransactionsByDateRange(start, end).asFlow().mapToList(Dispatchers.Default)
            .map { it.map { t -> t.toDomain() } }

    fun getRecent(limit: Long): Flow<List<Transaction>> =
        db.transactionRecordQueries.selectRecentTransactions(limit).asFlow().mapToList(Dispatchers.Default)
            .map { it.map { t -> t.toDomain() } }

    suspend fun insert(transaction: Transaction) {
        withContext(Dispatchers.Default) {
            db.transactionRecordQueries.insertTransaction(
                id = transaction.id,
                amount = transaction.amount,
                type = transaction.type.name,
                categoryId = transaction.categoryId,
                note = transaction.note,
                date = transaction.date.toEpochMilliseconds(),
                currency = transaction.currency,
                isRecurring = if (transaction.isRecurring) 1L else 0L,
                recurringType = transaction.recurringType?.name,
                cryptoCoinId = transaction.cryptoCoinId
            )
        }
    }

    suspend fun delete(id: String) {
        withContext(Dispatchers.Default) {
            db.transactionRecordQueries.deleteTransaction(id)
        }
    }

    suspend fun getSumByTypeAndRange(type: String, start: Long, end: Long): Double = withContext(Dispatchers.Default) {
        db.transactionRecordQueries.sumByTypeAndDateRange(type, start, end).executeAsOne()
    }

    private fun DbTransaction.toDomain() = Transaction(
        id = id,
        amount = amount,
        type = TransactionType.valueOf(type),
        categoryId = categoryId,
        note = note,
        date = Instant.fromEpochMilliseconds(date),
        currency = currency,
        isRecurring = isRecurring == 1L,
        recurringType = recurringType?.let { RecurringType.valueOf(it) },
        cryptoCoinId = cryptoCoinId
    )
}
