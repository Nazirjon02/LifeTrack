package tj.mahram.lifetrack.domain.model

import kotlin.time.Instant

/**
 * A kind of asset the user holds in their portfolio.
 *
 * [STOCK]       — shares in a listed company.
 * [FUND]        — a mutual fund / ETF / index fund.
 * [BUSINESS]    — a stake in a private business.
 * [METAL]       — precious metals (gold, silver, …).
 * [REAL_ESTATE] — property held as an investment.
 * [OTHER]       — anything that doesn't fit the buckets above.
 */
enum class InvestmentType { STOCK, FUND, BUSINESS, METAL, REAL_ESTATE, OTHER }

/**
 * A single investment holding. [amountInvested] is the cost basis the user put
 * in; [currentValue] is what it is worth now. The gap between the two is the
 * holding's profit (or loss) and drives the per-asset and portfolio returns.
 */
data class Investment(
    val id: String,
    val name: String,
    val type: InvestmentType,
    val amountInvested: Double,
    val currentValue: Double,
    val currency: String,
    val note: String?,
    val icon: String,
    val color: String,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /** Unrealised gain (positive) or loss (negative) on this holding. */
    val profit: Double get() = currentValue - amountInvested

    /** Return on the invested amount, as a percentage (0 when nothing invested). */
    val returnPercent: Double get() = if (amountInvested > 0) profit / amountInvested * 100 else 0.0
}

/**
 * Aggregate view of the whole portfolio. Drives the investments screen hero and
 * the small investments entry card on the finance screen.
 */
data class InvestmentsOverview(
    val items: List<Investment> = emptyList(),
    val currency: String = "USD"
) {
    /** Combined cost basis across every holding. */
    val totalInvested: Double get() = items.sumOf { it.amountInvested }

    /** Combined current value across every holding. */
    val totalCurrentValue: Double get() = items.sumOf { it.currentValue }

    /** Combined unrealised profit (or loss) across the portfolio. */
    val totalProfit: Double get() = totalCurrentValue - totalInvested

    /** Overall return on the invested amount, as a percentage. */
    val overallReturnPercent: Double get() = if (totalInvested > 0) totalProfit / totalInvested * 100 else 0.0

    val hasAny: Boolean get() = items.isNotEmpty()

    val count: Int get() = items.size
}
