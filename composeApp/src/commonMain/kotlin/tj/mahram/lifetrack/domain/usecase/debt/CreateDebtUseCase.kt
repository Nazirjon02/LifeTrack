package tj.mahram.lifetrack.domain.usecase.debt

import kotlin.time.Clock
import kotlin.time.Instant
import tj.mahram.lifetrack.domain.model.Debt
import tj.mahram.lifetrack.domain.model.DebtType
import tj.mahram.lifetrack.domain.repository.DebtRepository

class CreateDebtUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(
        type: DebtType,
        counterparty: String,
        amount: Double,
        currency: String,
        note: String?,
        dueDate: Instant?,
        color: String,
        alreadyPaid: Double = 0.0
    ) {
        val now = Clock.System.now()
        val paid = alreadyPaid.coerceIn(0.0, amount)
        repository.createDebt(
            Debt(
                id = now.toEpochMilliseconds().toString(),
                type = type,
                counterparty = counterparty.trim(),
                amount = amount,
                paidAmount = paid,
                currency = currency,
                note = note?.trim()?.ifEmpty { null },
                dueDate = dueDate,
                createdAt = now,
                updatedAt = now,
                isSettled = paid >= amount && amount > 0,
                color = color
            )
        )
    }
}
