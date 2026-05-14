package tj.mahram.lifetrack.core.util

import kotlin.math.abs

fun Double.formatCurrency(currency: String = "USD", showSign: Boolean = false): String {
    val symbol = currencySymbol(currency)
    val absVal = abs(this)
    val formatted = when {
        absVal >= 1_000_000_000 -> "${symbol}${(absVal / 1_000_000_000).roundTo(2)}B"
        absVal >= 1_000_000 -> "${symbol}${(absVal / 1_000_000).roundTo(2)}M"
        absVal >= 1_000 -> "${symbol}${(absVal / 1_000).roundTo(2)}K"
        absVal >= 1 -> "${symbol}${absVal.roundTo(2)}"
        absVal > 0 -> "${symbol}${absVal.roundTo(6)}"
        else -> "${symbol}0.00"
    }
    return if (showSign && this > 0) "+$formatted"
    else if (this < 0) "-$formatted"
    else formatted
}

fun Double.formatPercent(showSign: Boolean = true): String {
    val formatted = "${abs(this).roundTo(2)}%"
    return when {
        showSign && this > 0 -> "+$formatted"
        this < 0 -> "-$formatted"
        else -> formatted
    }
}

fun Double.roundTo(decimals: Int): String {
    val factor = Math.pow(10.0, decimals.toDouble())
    val rounded = kotlin.math.round(this * factor) / factor
    val str = rounded.toString()
    val dotIdx = str.indexOf('.')
    return if (dotIdx < 0) {
        "$str.${"0".repeat(decimals)}"
    } else {
        val decimalPart = str.substring(dotIdx + 1)
        if (decimalPart.length < decimals) {
            "$str${"0".repeat(decimals - decimalPart.length)}"
        } else {
            str.substring(0, dotIdx + decimals + 1)
        }
    }
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
