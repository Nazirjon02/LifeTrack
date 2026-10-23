package tj.mahram.lifetrack.core.i18n

import tj.mahram.lifetrack.domain.model.AppLanguage

/**
 * Localized strings for the unified Finance hub chrome: the section headers
 * that group the hub ("Accounts" / "Insights" / "Activity"), the compact
 * net-worth summary, and the Investments entry card.
 *
 * Kept as its own small data class — not folded into [AppStrings] — so its
 * generated constructor stays well under the Dalvik 255-argument ceiling that
 * [AppStrings] is already up against (mirrors the same decision as
 * [WalletStrings]).
 */
data class FinanceHubStrings(
    // Section group headers
    val sectionAccounts: String,
    val sectionInsights: String,
    val sectionActivity: String,
    // Net-worth summary
    val netWorthLabel: String,
    val netWorthPositive: String,
    val netWorthNegative: String,
    // Investments entry card
    val investmentsTitle: String,
    val investmentsSubtitle: String,
    val investmentsComingSoon: String,
)

private val FinanceHubStringsEn = FinanceHubStrings(
    sectionAccounts = "Accounts",
    sectionInsights = "Insights",
    sectionActivity = "Activity",
    netWorthLabel = "Net worth",
    netWorthPositive = "Assets exceed what you owe",
    netWorthNegative = "You owe more than you hold",
    investmentsTitle = "Investments",
    investmentsSubtitle = "Stocks, crypto & assets",
    investmentsComingSoon = "Coming soon",
)

private val FinanceHubStringsRu = FinanceHubStrings(
    sectionAccounts = "Счета",
    sectionInsights = "Аналитика",
    sectionActivity = "Операции",
    netWorthLabel = "Чистый капитал",
    netWorthPositive = "Активы превышают долги",
    netWorthNegative = "Долги превышают активы",
    investmentsTitle = "Инвестиции",
    investmentsSubtitle = "Акции, крипто и активы",
    investmentsComingSoon = "Скоро",
)

fun financeHubStringsFor(language: AppLanguage): FinanceHubStrings = when (language) {
    AppLanguage.RUSSIAN -> FinanceHubStringsRu
    else -> FinanceHubStringsEn
}
