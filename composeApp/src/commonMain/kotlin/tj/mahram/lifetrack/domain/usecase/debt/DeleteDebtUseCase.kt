package tj.mahram.lifetrack.domain.usecase.debt

import tj.mahram.lifetrack.domain.repository.DebtRepository

class DeleteDebtUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(id: String) = repository.deleteDebt(id)
}
