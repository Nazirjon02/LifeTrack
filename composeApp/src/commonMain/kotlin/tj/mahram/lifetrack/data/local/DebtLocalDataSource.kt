package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.domain.model.Debt
import tj.mahram.lifetrack.domain.model.DebtType
import tj.mahram.lifetrack.Debt as DbDebt

class DebtLocalDataSource(private val db: AppDatabase) {

    fun getAllDebts(): Flow<List<Debt>> =
        db.debtQueries.selectAllDebts().asFlow().mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }

    suspend fun getDebt(id: String): Debt? = withContext(Dispatchers.Default) {
        db.debtQueries.selectDebtById(id).executeAsOneOrNull()?.toDomain()
    }

    suspend fun insert(debt: Debt): Unit = withContext(Dispatchers.Default) {
        db.debtQueries.insertDebt(
            id = debt.id,
            type = debt.type.name,
            counterparty = debt.counterparty,
            amount = debt.amount,
            paidAmount = debt.paidAmount,
            currency = debt.currency,
            note = debt.note,
            dueDate = debt.dueDate?.toEpochMilliseconds(),
            createdAt = debt.createdAt.toEpochMilliseconds(),
            updatedAt = debt.updatedAt.toEpochMilliseconds(),
            isSettled = if (debt.isSettled) 1L else 0L,
            color = debt.color
        )
        Unit
    }

    suspend fun update(debt: Debt): Unit = withContext(Dispatchers.Default) {
        db.debtQueries.updateDebt(
            type = debt.type.name,
            counterparty = debt.counterparty,
            amount = debt.amount,
            currency = debt.currency,
            note = debt.note,
            dueDate = debt.dueDate?.toEpochMilliseconds(),
            color = debt.color,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
            id = debt.id
        )
        Unit
    }

    suspend fun recordPayment(id: String, newPaidAmount: Double, settled: Boolean): Unit =
        withContext(Dispatchers.Default) {
            db.debtQueries.updateDebtPayment(
                paidAmount = newPaidAmount,
                isSettled = if (settled) 1L else 0L,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
                id = id
            )
            Unit
        }

    suspend fun setSettled(id: String, settled: Boolean, paidAmount: Double): Unit =
        withContext(Dispatchers.Default) {
            db.debtQueries.setDebtSettled(
                isSettled = if (settled) 1L else 0L,
                paidAmount = paidAmount,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
                id = id
            )
            Unit
        }

    suspend fun delete(id: String): Unit = withContext(Dispatchers.Default) {
        db.debtQueries.deleteDebt(id)
        Unit
    }

    private fun DbDebt.toDomain() = Debt(
        id = id,
        type = DebtType.valueOf(type),
        counterparty = counterparty,
        amount = amount,
        paidAmount = paidAmount,
        currency = currency,
        note = note,
        dueDate = dueDate?.let { Instant.fromEpochMilliseconds(it) },
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        isSettled = isSettled == 1L,
        color = color
    )
}
