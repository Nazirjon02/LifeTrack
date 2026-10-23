package tj.mahram.lifetrack.feature.wallets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlin.time.Clock
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.i18n.WalletStrings
import tj.mahram.lifetrack.core.i18n.walletStringsFor
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.core.util.formatDate
import tj.mahram.lifetrack.domain.model.Wallet
import tj.mahram.lifetrack.domain.model.WalletWithBalance
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.brandVividGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.components.parseHexColor
import tj.mahram.lifetrack.ui.theme.appColors

class WalletsScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel<WalletsScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        WalletsContent(
            state = state,
            onIntent = screenModel::handleIntent,
            onBack = { navigator.pop() },
            onOpenWallet = { navigator.push(WalletDetailScreen(it)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsContent(
    state: WalletsState,
    onIntent: (WalletsIntent) -> Unit,
    onBack: () -> Unit,
    onOpenWallet: (String) -> Unit
) {
    val s = LocalStrings.current
    val w = walletStringsFor(state.language)
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                    .clickable { onIntent(WalletsIntent.ShowAddSheet) },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Add, contentDescription = w.addTitle, tint = Color.White, modifier = Modifier.size(28.dp)) }
        }
    ) { padding ->
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 110.dp)) {
            item(key = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(CircleShape).glassSurface(shape = CircleShape).clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(w.screenTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                        Text(w.screenSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item(key = "hero") { WalletsHero(state = state, strings = w) }

            if (state.overview.wallets.isEmpty()) {
                item(key = "empty") {
                    EmptyState(emoji = "💼", title = w.emptyTitle, subtitle = w.emptySubtitle, modifier = Modifier.padding(top = 24.dp))
                }
            } else {
                items(state.overview.wallets, key = { it.wallet.id }) { ww ->
                    WalletCard(
                        ww = ww,
                        strings = w,
                        onOpen = { onOpenWallet(ww.wallet.id) },
                        onEdit = { onIntent(WalletsIntent.ShowEditSheet(ww.wallet)) },
                        onDelete = { onIntent(WalletsIntent.RequestDelete(ww.wallet)) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        if (state.showSheet) {
            AddWalletSheet(
                editing = state.editing,
                strings = w,
                currency = state.currency,
                onDismiss = { onIntent(WalletsIntent.HideSheet) },
                onConfirm = { onIntent(WalletsIntent.SaveWallet(it)) }
            )
        }

        state.deleteTarget?.let { target ->
            AlertDialog(
                onDismissRequest = { onIntent(WalletsIntent.CancelDelete) },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp),
                title = { Text(w.deleteTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                text = { Text(w.deleteMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                confirmButton = {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(MaterialTheme.appColors.danger)
                            .clickable { onIntent(WalletsIntent.ConfirmDelete(target.id)) }.padding(horizontal = 22.dp, vertical = 11.dp)
                    ) { Text(w.deleteButton, color = Color.White, fontWeight = FontWeight.SemiBold) }
                },
                dismissButton = { TextButton(onClick = { onIntent(WalletsIntent.CancelDelete) }) { Text(s.cancelButton, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            )
        }
    }
}

// ─── Hero ────────────────────────────────────────────────────────────────────
@Composable
private fun WalletsHero(state: WalletsState, strings: WalletStrings) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp)).background(brandVividGradient()).padding(24.dp)
    ) {
        Column {
            Text(strings.totalLabel, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(
                state.overview.totalNetWorth.formatCurrency(state.currency),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(strings.countText(state.overview.count), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

// ─── Wallet card ───────────────────────────────────────────────────────────────
@Composable
private fun WalletCard(
    ww: WalletWithBalance,
    strings: WalletStrings,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wallet = ww.wallet
    val accent = parseHexColor(wallet.color)
    val c = MaterialTheme.appColors

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .glassSurface(shape = RoundedCornerShape(20.dp)).clickable(onClick = onOpen).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) { Text(wallet.icon, fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(wallet.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TypeChip(label = strings.typeLabel(wallet.type), accent = accent)
                    if (wallet.isDeposit && wallet.interestRate != null) {
                        TypeChip(label = strings.rateText(wallet.interestRate), accent = c.success)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                ww.currentBalance.formatCurrency(wallet.currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (ww.currentBalance >= 0) MaterialTheme.colorScheme.onSurface else c.danger
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        }

        if (wallet.isDeposit) {
            Spacer(Modifier.height(14.dp))
            DepositProgressStrip(wallet = wallet, strings = strings)
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    .clickable(onClick = onEdit).padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                    Text(strings.editButton, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TypeChip(label: String, accent: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(accent.copy(alpha = 0.14f)).padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DepositProgressStrip(wallet: Wallet, strings: WalletStrings) {
    val c = MaterialTheme.appColors
    val now = Clock.System.now()
    val matured = wallet.isMatured(now)
    val progress by animateFloatAsState(targetValue = wallet.termProgress(now), animationSpec = tween(600), label = "depositProgress")

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(c.success.copy(alpha = 0.15f))) {
            Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(6.dp).clip(CircleShape).background(c.success))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${strings.maturityPayoutLabel}: ${wallet.maturityPayout.formatCurrency(wallet.currency)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (matured) {
                Text(strings.maturedBadge, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = c.success)
            } else {
                wallet.endDate?.let { Text("${strings.maturityDateLabel} ${it.formatDate()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}
