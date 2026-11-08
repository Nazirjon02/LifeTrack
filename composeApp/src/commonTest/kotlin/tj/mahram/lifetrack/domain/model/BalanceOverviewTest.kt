package tj.mahram.lifetrack.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class BalanceOverviewTest {
    private val overview = BalanceOverview(
        openingBalance = 0.0,
        totalIncome = 100.0,
        totalExpense = 30.0,
        monthIncome = 0.0,
        monthExpense = 0.0,
        currency = "USD",
        walletsBalance = 500.0
    )

    @Test
    fun savingTheShownBalanceUnchangedChangesNothing() {
        assertEquals(570.0, overview.currentBalance)
        // Used to add the wallets' total on every save ($500 became $1.00K).
        assertEquals(0.0, overview.openingBalanceFor(570.0))
    }

    @Test
    fun theEnteredBalanceIsWhatTheUserSees() {
        val adjusted = overview.copy(openingBalance = overview.openingBalanceFor(1_000.0))
        assertEquals(1_000.0, adjusted.currentBalance)
        val lowered = overview.copy(openingBalance = overview.openingBalanceFor(200.0))
        assertEquals(200.0, lowered.currentBalance)
    }
}
