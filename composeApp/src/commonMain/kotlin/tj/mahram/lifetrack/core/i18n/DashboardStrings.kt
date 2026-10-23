package tj.mahram.lifetrack.core.i18n

import tj.mahram.lifetrack.domain.model.AppLanguage

/**
 * Localized strings for the Dashboard's finance summary widget. Kept as its own
 * data class — not folded into AppStrings — so its generated constructor stays
 * small (see the AppStrings 255-argument Dalvik limit note in memory). Labels
 * already provided by AppStrings/WalletStrings (income, expense, balance,
 * wallets, debts, …) are reused directly and are intentionally not duplicated
 * here.
 */
data class DashboardStrings(
    val totalBalance: String,
    val netThisMonth: String,
    val owedToYou: String,
    val youOwe: String,
    val allSettled: String,
)

private val DashboardStringsEn = DashboardStrings(
    totalBalance = "Total balance",
    netThisMonth = "Net this month",
    owedToYou = "Owed to you",
    youOwe = "You owe",
    allSettled = "All settled",
)

private val DashboardStringsRu = DashboardStrings(
    totalBalance = "Общий баланс",
    netThisMonth = "Чистыми за месяц",
    owedToYou = "Вам должны",
    youOwe = "Вы должны",
    allSettled = "Всё в расчёте",
)

fun dashboardStringsFor(language: AppLanguage): DashboardStrings = when (language) {
    AppLanguage.RUSSIAN -> DashboardStringsRu
    else -> DashboardStringsEn
}
