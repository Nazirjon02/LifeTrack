package tj.mahram.lifetrack.domain.usecase.debt

import kotlinx.coroutines.flow.Flow
import tj.mahram.lifetrack.domain.model.Debt
import tj.mahram.lifetrack.domain.repository.DebtRepository

class GetAllDebtsUseCase(private val repository: DebtRepository) {
    operator fun invoke(): Flow<List<Debt>> = repository.getAllDebts()
}
