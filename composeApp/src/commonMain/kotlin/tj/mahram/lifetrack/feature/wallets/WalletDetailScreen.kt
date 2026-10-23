package tj.mahram.lifetrack.feature.wallets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlin.time.Clock
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.WalletStrings
import tj.mahram.lifetrack.core.i18n.walletStringsFor
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.core.util.formatDate
import tj.mahram.lifetrack.domain.model.Transaction
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.domain.model.Wallet
import tj.mahram.lifetrack.domain.model.WalletWithBalance
import tj.mahram.lifetrack.ui.components.brandVividGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.components.parseHexColor
import tj.mahram.lifetrack.ui.theme.appColors

data class WalletDetailScreen(val walletId: String) : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel<WalletsScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        val ww = state.overview.wallets.firstOrNull { it.wallet.id == walletId }
        val txns = state.transactions.filter { it.walletId == walletId }
        WalletDetailContent(
            ww = ww,
            transactions = txns,
            strings = walletStringsFor(state.language),
            onBack = { navigator.pop() }
        )
    }
}

@Composable
private fun WalletDetailContent(
    ww: WalletWithBalance?,
    transactions: List<Transaction>,
    strings: WalletStrings,
    onBack: () -> Unit
) {
    if (ww == null) {
        // Wallet was deleted elsewhere — just offer a way back.
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).glassSurface(shape = CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) }
        }
        return
    }
    val wallet = ww.wallet
    val accent = parseHexColor(wallet.color)

    LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)) {
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).glassSurface(shape = CircleShape).clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) }
                Column {
                    Text(wallet.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
                    Text(strings.typeLabel(wallet.type), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item(key = "hero") { DetailHero(ww = ww, strings = strings) }

        if (wallet.isDeposit) {
            item(key = "deposit") { DepositDetailCard(wallet = wallet, strings = strings) }
        }

        item(key = "txnHeader") {
            Text(
                strings.detailTransactions,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp).padding(top = 14.dp, bottom = 6.dp)
            )
        }

        if (transactions.isEmpty()) {
            item(key = "txnEmpty") {
                Text(
                    strings.detailNoTransactions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        } else {
            items(transactions, key = { it.id }) { txn ->
                WalletTxnRow(txn = txn, currency = wallet.currency)
            }
        }
    }
}

@Composable
private fun DetailHero(ww: WalletWithBalance, strings: WalletStrings) {
    val wallet = ww.wallet
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp)).background(brandVividGradient()).padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text(wallet.icon, fontSize = 20.sp) }
                Text(strings.typeLabel(wallet.type), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                ww.currentBalance.formatCurrency(wallet.currency),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            if (wallet.isDeposit && wallet.interestRate != null) {
                Spacer(Modifier.height(4.dp))
                Text(strings.rateText(wallet.interestRate), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            }
        }
    }
}

@Composable
private fun DepositDetailCard(wallet: Wallet, strings: WalletStrings) {
    val c = MaterialTheme.appColors
    val now = Clock.System.now()
    val progress by animateFloatAsState(targetValue = wallet.termProgress(now), animationSpec = tween(700), label = "detailProgress")
    val accrued = wallet.accruedInterest(now)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .glassSurface(shape = RoundedCornerShape(24.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(strings.depositSection, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

        // Term progress ring + payout
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(modifier = Modifier.size(104.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(color = c.success.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
                    drawArc(color = c.success, startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f), useCenter = false, style = stroke)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(strings.termProgressLabel, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBlock(label = strings.maturityPayoutLabel, value = wallet.maturityPayout.formatCurrency(wallet.currency), valueColor = c.success)
                StatBlock(label = strings.accruedLabel, value = "+ " + accrued.formatCurrency(wallet.currency), valueColor = c.success)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        StatRow(label = strings.principalLabel, value = wallet.balance.formatCurrency(wallet.currency))
        wallet.interestRate?.let { StatRow(label = strings.rateLabel, value = strings.rateText(it)) }
        wallet.startDate?.let { StatRow(label = strings.startedLabel, value = it.formatDate()) }
        wallet.endDate?.let {
            StatRow(
                label = strings.maturityDateLabel,
                value = if (wallet.isMatured(now)) strings.maturedBadge else it.formatDate(),
                valueColor = if (wallet.isMatured(now)) c.success else null
            )
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, valueColor: Color? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor ?: MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor ?: MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun WalletTxnRow(txn: Transaction, currency: String) {
    val c = MaterialTheme.appColors
    val income = txn.type == TransactionType.INCOME
    val accent = if (income) c.success else c.danger
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .glassSurface(shape = RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(txn.note?.ifBlank { null } ?: txn.date.formatDate(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(txn.date.formatDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            (if (income) "+ " else "- ") + txn.amount.formatCurrency(currency),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}
