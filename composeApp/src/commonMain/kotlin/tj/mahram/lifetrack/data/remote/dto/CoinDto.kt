package tj.mahram.lifetrack.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoinMarketDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    @SerialName("current_price") val currentPrice: Double?,
    @SerialName("market_cap") val marketCap: Double?,
    @SerialName("market_cap_rank") val marketCapRank: Int?,
    @SerialName("price_change_percentage_24h") val priceChangePercent24h: Double?,
    @SerialName("total_volume") val totalVolume: Double?
)

@Serializable
data class CoinSearchResultDto(
    val coins: List<CoinSearchItemDto>
)

@Serializable
data class CoinSearchItemDto(
    val id: String,
    val symbol: String,
    val name: String,
    val thumb: String?
)

@Serializable
data class MarketChartDto(
    val prices: List<List<Double>>
)
