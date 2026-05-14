package tj.mahram.lifetrack.domain.usecase.finance

import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlinx.datetime.Instant
import tj.mahram.lifetrack.domain.model.FinanceSummary
import tj.mahram.lifetrack.domain.model.Transaction
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.domain.repository.TransactionRepository

class GetAllTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(): Flow<List<Transaction>> = repository.getAllTransactions()
}

class GetTransactionsByDateRangeUseCase(private val repository: TransactionRepository) {
    operator fun invoke(start: Instant, end: Instant): Flow<List<Transaction>> =
        repository.getTransactionsByDateRange(start, end)
}

class GetRecentTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(limit: Long = 10): Flow<List<Transaction>> =
        repository.getRecentTransactions(limit)
}

class AddTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(
        amount: Double,
        type: TransactionType,
        categoryId: String,
        note: String? = null,
        dateMillis: Long = Clock.System.now().toEpochMilliseconds(),
        currency: String = "USD"
    ) {
        require(amount > 0) { "Amount must be positive" }
        require(categoryId.isNotBlank()) { "Category must be selected" }
        val transaction = Transaction(
            id = generateUuid(),
            amount = amount,
            type = type,
            categoryId = categoryId,
            note = note?.trim(),
            date = Instant.fromEpochMilliseconds(dateMillis),
            currency = currency,
            isRecurring = false,
            recurringType = null,
            cryptoCoinId = null
        )
        repository.addTransaction(transaction)
    }
}

class DeleteTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(id: String) = repository.deleteTransaction(id)
}

class GetFinanceSummaryUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(start: Instant, end: Instant): FinanceSummary =
        repository.getFinanceSummary(start, end)
}

private fun generateUuid(): String {
    val chars = "0123456789abcdef"
    fun randomHex(length: Int) = (1..length).map { chars.random() }.joinToString("")
    return "${randomHex(8)}-${randomHex(4)}-4${randomHex(3)}-${listOf("8","9","a","b").random()}${randomHex(3)}-${randomHex(12)}"
}
