package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.Debt

interface DebtRepository {
    fun getAllDebts(): Flow<List<Debt>>
    suspend fun getDebt(id: String): Debt?
    suspend fun createDebt(debt: Debt)
    suspend fun updateDebt(debt: Debt)
    suspend fun recordPayment(id: String, newPaidAmount: Double, settled: Boolean)
    suspend fun setSettled(id: String, settled: Boolean, paidAmount: Double)
    suspend fun deleteDebt(id: String)
}
