package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.Investment

interface InvestmentRepository {
    fun getAllInvestments(): Flow<List<Investment>>
    suspend fun getInvestment(id: String): Investment?
    suspend fun createInvestment(investment: Investment)
    suspend fun updateInvestment(investment: Investment)
    suspend fun deleteInvestment(id: String)
}
