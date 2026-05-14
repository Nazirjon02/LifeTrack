package tj.mahram.lifetrack.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import tj.mahram.lifetrack.data.remote.dto.CoinMarketDto
import tj.mahram.lifetrack.data.remote.dto.CoinSearchResultDto
import tj.mahram.lifetrack.data.remote.dto.MarketChartDto
import tj.mahram.lifetrack.domain.model.CryptoCoin

class CoinGeckoApi(private val client: HttpClient) {

    private val baseUrl = "https://api.coingecko.com/api/v3"

    suspend fun getTopCoins(currency: String, page: Int): List<CryptoCoin> {
        val dtos: List<CoinMarketDto> = client.get("$baseUrl/coins/markets") {
            parameter("vs_currency", currency)
            parameter("order", "market_cap_desc")
            parameter("per_page", 50)
            parameter("page", page)
            parameter("sparkline", false)
            parameter("price_change_percentage", "24h")
        }.body()
        return dtos.mapIndexed { index, dto ->
            dto.toDomain(startRank = (page - 1) * 50 + index + 1)
        }
    }

    suspend fun searchCoins(query: String): List<CryptoCoin> {
        val result: CoinSearchResultDto = client.get("$baseUrl/search") {
            parameter("query", query)
        }.body()
        return result.coins.take(20).mapIndexed { index, item ->
            CryptoCoin(
                id = item.id,
                symbol = item.symbol.uppercase(),
                name = item.name,
                currentPrice = 0.0,
                priceChangePercent24h = 0.0,
                marketCap = 0.0,
                volume24h = 0.0,
                imageUrl = item.thumb ?: "",
                rank = index + 1
            )
        }
    }

    suspend fun getCoinById(coinId: String, currency: String = "usd"): CryptoCoin {
        val dtos: List<CoinMarketDto> = client.get("$baseUrl/coins/markets") {
            parameter("vs_currency", currency)
            parameter("ids", coinId)
            parameter("sparkline", false)
        }.body()
        return dtos.firstOrNull()?.toDomain(1)
            ?: error("Coin not found: $coinId")
    }

    suspend fun getCoinPriceHistory(coinId: String, days: Int, currency: String = "usd"): List<Double> {
        val chart: MarketChartDto = client.get("$baseUrl/coins/$coinId/market_chart") {
            parameter("vs_currency", currency)
            parameter("days", days)
        }.body()
        return chart.prices.map { it[1] }
    }

    private fun CoinMarketDto.toDomain(startRank: Int = 0): CryptoCoin = CryptoCoin(
        id = id,
        symbol = symbol.uppercase(),
        name = name,
        currentPrice = currentPrice ?: 0.0,
        priceChangePercent24h = priceChangePercent24h ?: 0.0,
        marketCap = marketCap ?: 0.0,
        volume24h = totalVolume ?: 0.0,
        imageUrl = image,
        rank = marketCapRank ?: startRank
    )
}
