package tj.mahram.lifetrack.core.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Money the way a finance app should show it: exact to the cent with thousands
 * separators ("$1,234.56"). Only from a million up is it shortened ("$1.23M",
 * "$4.50B", "$1.20T") so huge numbers still fit.
 *
 * It used to shorten everything from 1,000 ("$1.23K" for $1,234.56 — the exact
 * balance was visible nowhere), print small amounts with six decimals
 * ("$0.500000") and tiny ones in scientific notation.
 */
fun Double.formatCurrency(currency: String = "USD", showSign: Boolean = false): String {
    val symbol = currencySymbol(currency)
    if (!isFinite()) return "$symbol—"
    val absVal = abs(this)
    val number = when {
        absVal >= 1e12 -> (absVal / 1e12).roundTo(2) + "T"
        absVal >= 1e9 -> (absVal / 1e9).roundTo(2) + "B"
        absVal >= 1e6 -> (absVal / 1e6).roundTo(2) + "M"
        else -> absVal.groupedCents()
    }
    val formatted = "$symbol$number"
    return when {
        number == "0.00" -> formatted          // e.g. -0.001: no "-$0.00"
        this < 0 -> "-$formatted"
        showSign && this > 0 -> "+$formatted"
        else -> formatted
    }
}

/** "1,234.56": rounded to the cent, thousands separated (non-negative input < 1e15). */
private fun Double.groupedCents(): String {
    val cents = (this * 100).roundToLong()
    val whole = (cents / 100).toString()
    val fraction = (cents % 100).toString().padStart(2, '0')
    val grouped = whole.reversed().chunked(3).joinToString(",").reversed()
    return "$grouped.$fraction"
}

fun Double.formatPercent(showSign: Boolean = true): String {
    val formatted = "${abs(this).roundTo(2)}%"
    return when {
        showSign && this > 0 -> "+$formatted"
        this < 0 -> "-$formatted"
        else -> formatted
    }
}

/**
 * Plain decimal text with exactly [decimals] digits after the point ("12.50").
 * Built from integer arithmetic, so it never falls into Double.toString()'s
 * scientific notation ("1.0E-4") the way the old version could.
 */
fun Double.roundTo(decimals: Int): String {
    if (!isFinite()) return toString()
    val factor = 10.0.pow(decimals)
    val scaled = abs(this) * factor
    if (scaled >= 9.0e18) return toString() // beyond Long; never reached for money
    val units = scaled.roundToLong()
    val unitFactor = factor.toLong()
    val sign = if (this < 0 && units != 0L) "-" else ""
    val whole = units / unitFactor
    if (decimals <= 0) return "$sign$whole"
    val fraction = (units % unitFactor).toString().padStart(decimals, '0')
    return "$sign$whole.$fraction"
}

fun currencySymbol(currency: String): String = when (currency.uppercase()) {
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    "RUB" -> "₽"
    "TJS" -> "TJS"
    "JPY" -> "¥"
    "BTC" -> "₿"
    else -> currency
}

fun Double.formatMarketCap(currency: String = "USD"): String {
    val symbol = currencySymbol(currency)
    return when {
        this >= 1_000_000_000_000 -> "${symbol}${(this / 1_000_000_000_000).roundTo(2)}T"
        this >= 1_000_000_000 -> "${symbol}${(this / 1_000_000_000).roundTo(2)}B"
        this >= 1_000_000 -> "${symbol}${(this / 1_000_000).roundTo(2)}M"
        else -> this.formatCurrency(currency)
    }
}
