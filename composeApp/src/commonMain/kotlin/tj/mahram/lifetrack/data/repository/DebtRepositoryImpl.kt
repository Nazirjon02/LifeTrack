package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.data.local.DebtLocalDataSource
import tj.mahram.lifetrack.domain.model.Debt
import tj.mahram.lifetrack.domain.repository.DebtRepository

class DebtRepositoryImpl(private val dataSource: DebtLocalDataSource) : DebtRepository {

    override fun getAllDebts(): Flow<List<Debt>> = dataSource.getAllDebts()

    override suspend fun getDebt(id: String): Debt? = dataSource.getDebt(id)

    override suspend fun createDebt(debt: Debt) = dataSource.insert(debt)

    override suspend fun updateDebt(debt: Debt) = dataSource.update(debt)

    override suspend fun recordPayment(id: String, newPaidAmount: Double, settled: Boolean) =
        dataSource.recordPayment(id, newPaidAmount, settled)

    override suspend fun setSettled(id: String, settled: Boolean, paidAmount: Double) =
        dataSource.setSettled(id, settled, paidAmount)

    override suspend fun deleteDebt(id: String) = dataSource.delete(id)
}
