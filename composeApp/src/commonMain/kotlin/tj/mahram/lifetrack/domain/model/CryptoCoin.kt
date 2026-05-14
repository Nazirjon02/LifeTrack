package tj.mahram.lifetrack.domain.model

data class CryptoCoin(
    val id: String,
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val priceChangePercent24h: Double,
    val marketCap: Double,
    val volume24h: Double,
    val imageUrl: String,
    val rank: Int,
    val priceHistory: List<Double> = emptyList()
)

data class CryptoPortfolioItem(
    val id: String,
    val coinId: String,
    val coinSymbol: String,
    val coinName: String,
    val amount: Double,
    val purchasePrice: Double,
    val currentPrice: Double = 0.0
) {
    val currentValue: Double get() = amount * currentPrice
    val investedValue: Double get() = amount * purchasePrice
    val pnl: Double get() = currentValue - investedValue
    val pnlPercent: Double get() = if (investedValue > 0) (pnl / investedValue) * 100 else 0.0
}

data class CryptoWatchlistItem(
    val coinId: String,
    val coinSymbol: String,
    val coinName: String
)

data class CryptoPriceAlert(
    val id: String,
    val coinId: String,
    val coinSymbol: String,
    val targetPrice: Double,
    val isAbove: Boolean,
    val isTriggered: Boolean
)

data class PortfolioSummary(
    val totalValue: Double,
    val totalInvested: Double,
    val totalPnl: Double,
    val totalPnlPercent: Double,
    val items: List<CryptoPortfolioItem>
)
