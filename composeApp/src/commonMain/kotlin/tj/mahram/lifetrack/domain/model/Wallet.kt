package tj.mahram.lifetrack.domain.model

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A place the user keeps money.
 *
 * [CASH]    — physical cash.
 * [CARD]    — a bank card / current account.
 * [SAVINGS] — a plain savings pot (no interest tracking).
 * [DEPOSIT] — a fixed-term bank deposit that earns interest. Deposit wallets
 *             additionally carry [Wallet.interestRate], [Wallet.startDate] and
 *             [Wallet.endDate] and can project accrued interest and the payout
 *             at maturity.
 */
enum class WalletType { CASH, CARD, SAVINGS, DEPOSIT }

/** Rough milliseconds in a calendar year — good enough for interest display. */
private const val MILLIS_PER_YEAR = 365.0 * 24 * 60 * 60 * 1000

/**
 * A single wallet with its own running balance and currency.
 *
 * [balance] is the wallet's base/manual balance; its *current* balance is this
 * plus the net of the transactions assigned to it (see [WalletWithBalance]).
 * For a [DEPOSIT] wallet [balance] is the principal that was locked in — accrued
 * interest and the maturity payout are projections (below) and are intentionally
 * NOT folded into the live balance so the headline number stays stable.
 */
data class Wallet(
    val id: String,
    val name: String,
    val type: WalletType,
    val balance: Double,
    val currency: String,
    val icon: String,
    val color: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isArchived: Boolean = false,
    // ── DEPOSIT-only (null for the other types) ──────────────────────────────
    val interestRate: Double? = null,   // annual %, e.g. 12.5
    val startDate: Instant? = null,
    val endDate: Instant? = null,        // maturity date
) {
    val isDeposit: Boolean get() = type == WalletType.DEPOSIT

    /** Full term length in years (0 when the deposit dates are missing). */
    val termYears: Double
        get() {
            val start = startDate ?: return 0.0
            val end = endDate ?: return 0.0
            return ((end - start).inWholeMilliseconds.coerceAtLeast(0)) / MILLIS_PER_YEAR
        }

    /** Simple interest earned over the full term. */
    val fullTermInterest: Double
        get() {
            val rate = interestRate ?: return 0.0
            return balance * rate / 100.0 * termYears
        }

    /** What the deposit pays out at maturity: principal + full-term interest. */
    val maturityPayout: Double
        get() = balance + fullTermInterest

    /**
     * Interest accrued so far, using simple interest proportional to the elapsed
     * fraction of the term and capped at the full-term interest.
     */
    fun accruedInterest(now: Instant = Clock.System.now()): Double {
        val start = startDate ?: return 0.0
        if (endDate == null || interestRate == null) return 0.0
        val elapsedMs = (now - start).inWholeMilliseconds.coerceAtLeast(0L).toDouble()
        val elapsedYears = elapsedMs / MILLIS_PER_YEAR
        val accrued = balance * interestRate / 100.0 * elapsedYears
        return accrued.coerceIn(0.0, fullTermInterest)
    }

    /** Fraction of the term elapsed, in 0f..1f. */
    fun termProgress(now: Instant = Clock.System.now()): Float {
        val start = startDate ?: return 0f
        val end = endDate ?: return 0f
        val total = (end - start).inWholeMilliseconds.coerceAtLeast(1L).toDouble()
        val elapsed = (now - start).inWholeMilliseconds.coerceAtLeast(0L).toDouble()
        return (elapsed / total).toFloat().coerceIn(0f, 1f)
    }

    /** True once the deposit has reached its maturity date. */
    fun isMatured(now: Instant = Clock.System.now()): Boolean {
        val end = endDate ?: return false
        return now >= end
    }
}

/** A wallet paired with its current balance (base balance + its ledger net). */
data class WalletWithBalance(
    val wallet: Wallet,
    val currentBalance: Double
)

/**
 * Aggregate view of every wallet. Drives the wallets screen hero and the small
 * wallets entry card on the finance screen.
 */
data class WalletsOverview(
    val wallets: List<WalletWithBalance> = emptyList(),
    val currency: String = "USD"
) {
    /** Combined current balance across all wallets. */
    val totalNetWorth: Double
        get() = wallets.sumOf { it.currentBalance }

    val count: Int get() = wallets.size

    val hasAny: Boolean get() = wallets.isNotEmpty()
}
