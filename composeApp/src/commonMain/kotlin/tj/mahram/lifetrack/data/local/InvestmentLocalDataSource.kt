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
import tj.mahram.lifetrack.domain.model.Investment
import tj.mahram.lifetrack.domain.model.InvestmentType
import tj.mahram.lifetrack.Investment as DbInvestment

class InvestmentLocalDataSource(private val db: AppDatabase) {

    fun getAllInvestments(): Flow<List<Investment>> =
        db.investmentQueries.selectAllInvestments().asFlow().mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }

    suspend fun getInvestment(id: String): Investment? = withContext(Dispatchers.Default) {
        db.investmentQueries.selectInvestmentById(id).executeAsOneOrNull()?.toDomain()
    }

    suspend fun insert(investment: Investment): Unit = withContext(Dispatchers.Default) {
        db.investmentQueries.insertInvestment(
            id = investment.id,
            name = investment.name,
            type = investment.type.name,
            amountInvested = investment.amountInvested,
            currentValue = investment.currentValue,
            currency = investment.currency,
            note = investment.note,
            icon = investment.icon,
            color = investment.color,
            createdAt = investment.createdAt.toEpochMilliseconds(),
            updatedAt = investment.updatedAt.toEpochMilliseconds()
        )
        Unit
    }

    suspend fun update(investment: Investment): Unit = withContext(Dispatchers.Default) {
        db.investmentQueries.updateInvestment(
            name = investment.name,
            type = investment.type.name,
            amountInvested = investment.amountInvested,
            currentValue = investment.currentValue,
            currency = investment.currency,
            note = investment.note,
            icon = investment.icon,
            color = investment.color,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
            id = investment.id
        )
        Unit
    }

    suspend fun delete(id: String): Unit = withContext(Dispatchers.Default) {
        db.investmentQueries.deleteInvestment(id)
        Unit
    }

    private fun DbInvestment.toDomain() = Investment(
        id = id,
        name = name,
        type = runCatching { InvestmentType.valueOf(type) }.getOrDefault(InvestmentType.STOCK),
        amountInvested = amountInvested,
        currentValue = currentValue,
        currency = currency,
        note = note,
        icon = icon,
        color = color,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
}
