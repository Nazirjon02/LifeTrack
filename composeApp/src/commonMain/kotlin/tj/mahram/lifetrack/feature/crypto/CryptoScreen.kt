package tj.mahram.lifetrack.feature.crypto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.core.util.formatPercent
import tj.mahram.lifetrack.ui.components.*
import tj.mahram.lifetrack.ui.theme.ErrorColor
import tj.mahram.lifetrack.ui.theme.SuccessColor

class CryptoScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<CryptoScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        CryptoContent(state = state, onIntent = screenModel::handleIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoContent(state: CryptoState, onIntent: (CryptoIntent) -> Unit) {
    val s = LocalStrings.current
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(s.cryptoTitle) },
                    actions = {
                        IconButton(onClick = { onIntent(CryptoIntent.Refresh) }) {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onIntent(CryptoIntent.Search(it)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text(s.cryptoSearchPlaceholder) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                PrimaryTabRow(
                    selectedTabIndex = state.selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    CryptoTab.entries.forEach { tab ->
                        Tab(
                            selected = state.selectedTab == tab,
                            onClick = { onIntent(CryptoIntent.SelectTab(tab)) },
                            text = { Text(tab.label) }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when (state.selectedTab) {
            CryptoTab.MARKET -> MarketTab(state, onIntent, padding)
            CryptoTab.PORTFOLIO -> PortfolioTab(state, onIntent, padding)
            CryptoTab.WATCHLIST -> WatchlistTab(state, onIntent, padding)
        }
    }

    // Add to portfolio dialog
    state.showAddToPortfolioFor?.let { coin ->
        AddToPortfolioDialog(
            coin = coin,
            onDismiss = { onIntent(CryptoIntent.HideAddPortfolio) },
            onConfirm = { amount, price ->
                onIntent(CryptoIntent.AddToPortfolio(coin, amount, price))
            }
        )
    }

    // Coin detail bottom sheet
    state.selectedCoin?.let { coin ->
        CoinDetailSheet(
            coin = coin,
            priceHistory = state.selectedCoinHistory,
            selectedDays = state.selectedHistoryDays,
            currency = state.currency,
            onDismiss = { onIntent(CryptoIntent.ClearSelectedCoin) },
            onAddToPortfolio = { onIntent(CryptoIntent.ShowAddPortfolioFor(coin)) },
            onToggleWatchlist = { onIntent(CryptoIntent.ToggleWatchlist(coin)) },
            onSelectDays = { onIntent(CryptoIntent.SelectHistoryRange(it)) }
        )
    }
}

@Composable
fun MarketTab(state: CryptoState, onIntent: (CryptoIntent) -> Unit, padding: PaddingValues) {
    val s = LocalStrings.current
    if (state.isLoadingCoins && state.topCoins.isEmpty()) {
        LazyColumn(contentPadding = padding) {
            items(8) { CryptoCardSkeleton() }
        }
        return
    }
    if (state.displayedCoins.isEmpty()) {
        EmptyState(emoji = "₿", title = s.cryptoMarketEmpty, subtitle = s.cryptoMarketEmptySubtitle)
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 16.dp
        )
    ) {
        items(state.displayedCoins, key = { it.id }) { coin ->
            CryptoCard(
                coin = coin,
                currency = state.currency,
                onClick = { onIntent(CryptoIntent.SelectCoin(coin)) }
            )
        }
    }
}

@Composable
fun PortfolioTab(state: CryptoState, onIntent: (CryptoIntent) -> Unit, padding: PaddingValues) {
    val s = LocalStrings.current
    if (state.portfolioItems.isEmpty()) {
        EmptyState(
            emoji = "📊",
            title = s.cryptoPortfolioEmpty,
            subtitle = s.cryptoPortfolioEmptySubtitle,
            modifier = Modifier.padding(padding)
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 16.dp
        )
    ) {
        state.portfolioSummary?.let { summary ->
            item {
                PortfolioSummaryCard(
                    totalValue = summary.totalValue,
                    pnlPercent = summary.totalPnlPercent,
                    pnl = summary.totalPnl,
                    currency = state.currency,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        items(state.portfolioItems, key = { it.id }) { item ->
            PortfolioItemCard(item = item, currency = state.currency, onRemove = { onIntent(CryptoIntent.RemoveFromPortfolio(item.id)) })
        }
    }
}

@Composable
fun WatchlistTab(state: CryptoState, onIntent: (CryptoIntent) -> Unit, padding: PaddingValues) {
    val s = LocalStrings.current
    if (state.watchlist.isEmpty()) {
        EmptyState(
            emoji = "⭐",
            title = s.cryptoWatchlistEmpty,
            subtitle = s.cryptoWatchlistEmptySubtitle,
            modifier = Modifier.padding(padding)
        )
        return
    }
    LazyColumn(contentPadding = padding) {
        items(state.watchlist, key = { it.coinId }) { item ->
            val coin = state.topCoins.find { it.id == item.coinId }
            WatchlistItemCard(item = item, coin = coin, currency = state.currency)
        }
    }
}

@Composable
fun PortfolioItemCard(
    item: tj.mahram.lifetrack.domain.model.CryptoPortfolioItem,
    currency: String,
    onRemove: () -> Unit
) {
    val isPositive = item.pnl >= 0
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.coinName, style = MaterialTheme.typography.bodyLarge)
                Text("${item.amount} ${item.coinSymbol}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(item.currentValue.formatCurrency(currency), style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${if (isPositive) "+" else ""}${item.pnl.formatCurrency(currency)} (${item.pnlPercent.formatPercent()})",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPositive) SuccessColor else ErrorColor
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun WatchlistItemCard(
    item: tj.mahram.lifetrack.domain.model.CryptoWatchlistItem,
    coin: tj.mahram.lifetrack.domain.model.CryptoCoin?,
    currency: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.coinName, style = MaterialTheme.typography.bodyLarge)
                Text(item.coinSymbol, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (coin != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(coin.currentPrice.formatCurrency(currency), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        coin.priceChangePercent24h.formatPercent(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (coin.priceChangePercent24h >= 0) SuccessColor else ErrorColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailSheet(
    coin: tj.mahram.lifetrack.domain.model.CryptoCoin,
    priceHistory: List<Double>,
    selectedDays: Int,
    currency: String,
    onDismiss: () -> Unit,
    onAddToPortfolio: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onSelectDays: (Int) -> Unit
) {
    val s = LocalStrings.current
    val isPositive = coin.priceChangePercent24h >= 0
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(coin.name, style = MaterialTheme.typography.titleLarge)
                    Text(coin.symbol, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(coin.currentPrice.formatCurrency(currency), style = MaterialTheme.typography.titleLarge)
                    Text(
                        coin.priceChangePercent24h.formatPercent(),
                        color = if (isPositive) SuccessColor else ErrorColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            if (priceHistory.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 7, 30, 365).forEach { days ->
                        FilterChip(selected = selectedDays == days, onClick = { onSelectDays(days) }, label = {
                            Text(when(days) { 1 -> s.cryptoChart1D; 7 -> s.cryptoChart7D; 30 -> s.cryptoChart1M; else -> s.cryptoChart1Y })
                        })
                    }
                }
                LineChart(
                    data = priceHistory,
                    lineColor = if (isPositive) SuccessColor else ErrorColor,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onAddToPortfolio, modifier = Modifier.weight(1f)) { Text(s.cryptoAddToPortfolioButton) }
                OutlinedButton(onClick = onToggleWatchlist, modifier = Modifier.weight(1f)) { Text(s.cryptoWatchlistButton) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPortfolioDialog(
    coin: tj.mahram.lifetrack.domain.model.CryptoCoin,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf(coin.currentPrice.toString()) }

    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.cryptoAddToPortfolioDialog(coin.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text(s.cryptoAmountLabel) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text(s.cryptoPurchasePriceLabel) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toDoubleOrNull() ?: return@TextButton
                val price = priceText.toDoubleOrNull() ?: return@TextButton
                onConfirm(amount, price)
            }) { Text(s.cryptoAddButton) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cryptoCancelButton) } }
    )
}
