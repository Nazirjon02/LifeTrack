package tj.mahram.lifetrack.feature.investments

import kotlin.time.Instant
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.Investment
import tj.mahram.lifetrack.domain.model.InvestmentType
import tj.mahram.lifetrack.domain.model.InvestmentsOverview

data class InvestmentsState(
    val isLoading: Boolean = true,
    val overview: InvestmentsOverview = InvestmentsOverview(),
    val language: AppLanguage = AppLanguage.ENGLISH,
    val currency: String = "USD",
    val showSheet: Boolean = false,
    val editing: Investment? = null,
    val deleteTarget: Investment? = null,
    val error: String? = null
)

/** Everything the create/edit sheet produces; [id] null means "create". */
data class InvestmentDraft(
    val id: String?,
    val name: String,
    val type: InvestmentType,
    val amountInvested: Double,
    val currentValue: Double,
    val note: String?,
    val icon: String,
    val color: String,
    val createdAt: Instant?
)

sealed class InvestmentsIntent {
    data object ShowAddSheet : InvestmentsIntent()
    data class ShowEditSheet(val investment: Investment) : InvestmentsIntent()
    data object HideSheet : InvestmentsIntent()
    data class SaveInvestment(val draft: InvestmentDraft) : InvestmentsIntent()
    data class RequestDelete(val investment: Investment) : InvestmentsIntent()
    data object CancelDelete : InvestmentsIntent()
    data class ConfirmDelete(val id: String) : InvestmentsIntent()
}
