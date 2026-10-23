package tj.mahram.lifetrack.domain.usecase.investment

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.Clock
import tj.mahram.lifetrack.domain.model.Investment
import tj.mahram.lifetrack.domain.model.InvestmentType
import tj.mahram.lifetrack.domain.model.InvestmentsOverview
import tj.mahram.lifetrack.domain.repository.InvestmentRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository

class GetAllInvestmentsUseCase(private val repository: InvestmentRepository) {
    operator fun invoke(): Flow<List<Investment>> = repository.getAllInvestments()
}

/**
 * Emits the whole portfolio paired with the display currency and keeps it live
 * as holdings or the settings change. Portfolio totals and returns are computed
 * on [InvestmentsOverview] itself.
 */
class ObserveInvestmentsOverviewUseCase(
    private val investmentRepository: InvestmentRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<InvestmentsOverview> =
        combine(
            investmentRepository.getAllInvestments(),
            settingsRepository.getSettings()
        ) { investments, settings ->
            InvestmentsOverview(
                items = investments,
                currency = settings.currency
            )
        }
}

class CreateInvestmentUseCase(private val repository: InvestmentRepository) {
    suspend operator fun invoke(
        name: String,
        type: InvestmentType,
        amountInvested: Double,
        currentValue: Double,
        currency: String,
        note: String?,
        icon: String,
        color: String
    ) {
        require(name.isNotBlank()) { "Name must not be blank" }
        val now = Clock.System.now()
        repository.createInvestment(
            Investment(
                id = generateUuid(),
                name = name.trim(),
                type = type,
                amountInvested = amountInvested,
                currentValue = currentValue,
                currency = currency,
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                icon = icon,
                color = color,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}

class UpdateInvestmentUseCase(private val repository: InvestmentRepository) {
    suspend operator fun invoke(investment: Investment) {
        require(investment.name.isNotBlank()) { "Name must not be blank" }
        repository.updateInvestment(investment.copy(updatedAt = Clock.System.now()))
    }
}

class DeleteInvestmentUseCase(private val repository: InvestmentRepository) {
    suspend operator fun invoke(id: String) = repository.deleteInvestment(id)
}

private fun generateUuid(): String {
    val chars = "0123456789abcdef"
    fun randomHex(length: Int) = (1..length).map { chars.random() }.joinToString("")
    return "${randomHex(8)}-${randomHex(4)}-4${randomHex(3)}-${listOf("8", "9", "a", "b").random()}${randomHex(3)}-${randomHex(12)}"
}
