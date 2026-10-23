package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.data.local.InvestmentLocalDataSource
import tj.mahram.lifetrack.data.sync.SyncCollectionNames
import tj.mahram.lifetrack.data.sync.SyncTracker
import tj.mahram.lifetrack.domain.model.Investment
import tj.mahram.lifetrack.domain.repository.InvestmentRepository

class InvestmentRepositoryImpl(
    private val investments: InvestmentLocalDataSource,
    private val sync: SyncTracker
) : InvestmentRepository {

    override fun getAllInvestments(): Flow<List<Investment>> = investments.getAllInvestments()

    override suspend fun getInvestment(id: String): Investment? = investments.getInvestment(id)

    override suspend fun createInvestment(investment: Investment) {
        investments.insert(investment)
        sync.markDirty(SyncCollectionNames.INVESTMENTS, investment.id)
    }

    override suspend fun updateInvestment(investment: Investment) {
        investments.update(investment)
        sync.markDirty(SyncCollectionNames.INVESTMENTS, investment.id)
    }

    override suspend fun deleteInvestment(id: String) {
        investments.delete(id)
        sync.markDeleted(SyncCollectionNames.INVESTMENTS, id)
    }
}
