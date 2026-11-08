package tj.mahram.lifetrack.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyUtilsTest {

    @Test
    fun amountsBelowAMillionAreExactWithSeparators() {
        assertEquals("$1,234.56", 1_234.56.formatCurrency())   // was "$1.23K"
        assertEquals("$1,000.00", 1_000.0.formatCurrency())    // was "$1.00K"
        assertEquals("$999,999.99", 999_999.99.formatCurrency())
        assertEquals("$487.50", 487.5.formatCurrency())
        assertEquals("€12.50", 12.5.formatCurrency("EUR"))
    }

    @Test
    fun smallAmountsUseCents() {
        assertEquals("$0.50", 0.5.formatCurrency())   // was "$0.500000"
        assertEquals("$0.00", 0.0005.formatCurrency()) // could be "$5.0E-4" on the JVM
        assertEquals("$0.01", 0.005.formatCurrency())
        assertEquals("$0.00", 0.0.formatCurrency())
    }

    @Test
    fun hugeAmountsAreShortened() {
        assertEquals("$1.00M", 1_000_000.0.formatCurrency())
        assertEquals("$1.23M", 1_234_567.0.formatCurrency())
        assertEquals("$4.50B", 4.5e9.formatCurrency())
        assertEquals("$1.20T", 1.2e12.formatCurrency())
    }

    @Test
    fun signs() {
        assertEquals("-$12.50", (-12.5).formatCurrency())
        assertEquals("+$12.50", 12.5.formatCurrency(showSign = true))
        assertEquals("-$1,234.56", (-1_234.56).formatCurrency(showSign = true))
        assertEquals("$0.00", (-0.001).formatCurrency(), "no negative zero")
        assertEquals("$—", Double.NaN.formatCurrency())
    }

    @Test
    fun roundToNeverUsesScientificNotation() {
        assertEquals("0.000100", 0.0001.roundTo(6)) // Double.toString() gives "1.0E-4"
        assertEquals("1000.00", 1_000.0.roundTo(2)) // balance dialog prefill
        assertEquals("2.5", 2.46.roundTo(1))
        assertEquals("-3.14", (-3.14159).roundTo(2))
        assertEquals("0.00", (-0.001).roundTo(2))
        assertEquals("12", 12.4.roundTo(0))
    }
}
