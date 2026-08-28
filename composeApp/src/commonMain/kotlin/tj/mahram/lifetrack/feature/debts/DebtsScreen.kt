package tj.mahram.lifetrack.feature.debts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.currencySymbol
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.core.util.formatDate
import tj.mahram.lifetrack.domain.model.Debt
import tj.mahram.lifetrack.domain.model.DebtSummary
import tj.mahram.lifetrack.domain.model.DebtType
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.brandVividGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.theme.appColors

class DebtsScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel<DebtsScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        DebtsContent(state = state, onIntent = screenModel::handleIntent, onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsContent(state: DebtsState, onIntent: (DebtsIntent) -> Unit, onBack: () -> Unit) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(c.success)
                        .clickable { onIntent(DebtsIntent.ShowAddSheet(DebtType.LENT)) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ArrowUpward, contentDescription = s.debtAddLent, tint = Color.White, modifier = Modifier.size(22.dp)) }
                Box(
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(c.danger)
                        .clickable { onIntent(DebtsIntent.ShowAddSheet(DebtType.BORROWED)) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ArrowDownward, contentDescription = s.debtAddBorrowed, tint = Color.White, modifier = Modifier.size(26.dp)) }
            }
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
                        Text(s.debtsTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                        Text(s.debtsSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item(key = "summary") { DebtsSummaryHero(summary = state.summary, currency = state.currency) }

            item(key = "tabs") {
                DebtFilterTabs(selected = state.filter, onSelect = { onIntent(DebtsIntent.SetFilter(it)) })
            }

            val visible = state.visibleDebts
            if (visible.isEmpty()) {
                item(key = "empty") {
                    EmptyState(emoji = "🤝", title = s.debtEmptyTitle, subtitle = s.debtEmptySubtitle, modifier = Modifier.padding(top = 24.dp))
                }
            } else {
                items(visible, key = { it.id }) { debt ->
                    DebtCard(
                        debt = debt,
                        onPay = { onIntent(DebtsIntent.ShowPayment(debt)) },
                        onToggleSettled = { onIntent(DebtsIntent.SetSettled(debt, !debt.isSettled)) },
                        onDelete = { onIntent(DebtsIntent.DeleteDebt(debt.id)) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        if (state.showAddSheet) {
            AddDebtSheet(
                presetType = state.addPresetType,
                currency = state.currency,
                onDismiss = { onIntent(DebtsIntent.HideAddSheet) },
                onConfirm = onIntent
            )
        }

        state.paymentTarget?.let { debt ->
            RecordPaymentDialog(
                debt = debt,
                currency = state.currency,
                onDismiss = { onIntent(DebtsIntent.HidePayment) },
                onPay = { amount -> onIntent(DebtsIntent.RecordPayment(debt, amount)) },
                onSettleAll = { onIntent(DebtsIntent.SetSettled(debt, true)) }
            )
        }
    }
}

// ─── Summary hero ────────────────────────────────────────────────────────────
@Composable
private fun DebtsSummaryHero(summary: DebtSummary, currency: String) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp)).background(brandVividGradient()).padding(24.dp)
    ) {
        Column {
            Text(s.debtNetPosition, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(
                summary.net.formatCurrency(currency, showSign = true),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            val caption = when {
                summary.net > 0 -> s.debtNetPositive
                summary.net < 0 -> s.debtNetNegative
                else -> s.debtNetEven
            }
            Text(caption, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeroDebtPill(modifier = Modifier.weight(1f), label = s.debtOwedToMe, amount = summary.totalLent, currency = currency, arrowUp = true)
                HeroDebtPill(modifier = Modifier.weight(1f), label = s.debtIOwe, amount = summary.totalBorrowed, currency = currency, arrowUp = false)
            }
        }
    }
}

@Composable
private fun HeroDebtPill(modifier: Modifier = Modifier, label: String, amount: Double, currency: String, arrowUp: Boolean) {
    Box(modifier = modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.15f)).padding(horizontal = 14.dp, vertical = 12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                    Icon(if (arrowUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
                Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
            }
            Text(amount.formatCurrency(currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ─── Filter tabs ─────────────────────────────────────────────────────────────
@Composable
private fun DebtFilterTabs(selected: DebtFilter, onSelect: (DebtFilter) -> Unit) {
    val s = LocalStrings.current
    val brand = brandHorizontalGradient()
    val tabs = listOf(
        s.debtsFilterAll to DebtFilter.ALL,
        s.debtOwedToMe to DebtFilter.LENT,
        s.debtIOwe to DebtFilter.BORROWED
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).glassSurface(shape = RoundedCornerShape(16.dp)).padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        tabs.forEach { (label, filter) ->
            val isSelected = selected == filter
            val fg by animateColorAsState(if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, tween(200), label = "debtTab_$label")
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .then(if (isSelected) Modifier.background(brand, RoundedCornerShape(12.dp)) else Modifier)
                    .clickable { onSelect(filter) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = fg, maxLines = 1)
            }
        }
    }
}

// ─── Debt card ───────────────────────────────────────────────────────────────
@Composable
private fun DebtCard(
    debt: Debt,
    onPay: () -> Unit,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    val accent = if (debt.type == DebtType.LENT) c.success else c.danger
    val overdue = debt.isOverdue()
    val dimmed = debt.isSettled

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .glassSurface(shape = RoundedCornerShape(20.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar with initial
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(accent.copy(alpha = if (dimmed) 0.10f else 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    debt.counterparty.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent.copy(alpha = if (dimmed) 0.6f else 1f)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    debt.counterparty,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (dimmed) 0.6f else 1f),
                    maxLines = 1
                )
                if (!debt.note.isNullOrBlank()) {
                    Text(debt.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Spacer(Modifier.height(3.dp))
                DebtMetaChip(debt = debt, overdue = overdue)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (debt.type == DebtType.LENT) "+" else "-") + debt.remaining.formatCurrency(debt.currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (dimmed) MaterialTheme.colorScheme.onSurfaceVariant else accent
                )
                if (!debt.isSettled && debt.paidAmount > 0) {
                    Text("${s.debtRemainingWord}", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Repayment progress (only when partially paid & open)
        if (!debt.isSettled && debt.paidAmount > 0) {
            Spacer(Modifier.height(12.dp))
            val progress by animateFloatAsState(targetValue = debt.progress, animationSpec = tween(600), label = "debtProgress")
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f))) {
                Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(6.dp).clip(CircleShape).background(accent))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${s.debtRepaidLabel}: ${debt.paidAmount.formatCurrency(debt.currency)} / ${debt.amount.formatCurrency(debt.currency)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(12.dp))

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!debt.isSettled) {
                DebtActionButton(modifier = Modifier.weight(1f), label = s.debtActionPay, icon = Icons.Default.ArrowDownward, filled = true, accent = accent, onClick = onPay)
                DebtActionButton(modifier = Modifier.weight(1f), label = s.debtActionSettle, icon = Icons.Default.Check, filled = false, accent = accent, onClick = onToggleSettled)
            } else {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(c.success.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = c.success, modifier = Modifier.size(14.dp))
                        Text(s.debtSettledBadge, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.success)
                    }
                }
                DebtActionButton(modifier = Modifier.weight(1f), label = s.debtActionReopen, icon = Icons.AutoMirrored.Filled.Undo, filled = false, accent = MaterialTheme.colorScheme.primary, onClick = onToggleSettled)
            }
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)).clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun DebtMetaChip(debt: Debt, overdue: Boolean) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    val due = debt.dueDate ?: return
    val (bg, fg, label) = if (overdue)
        Triple(c.danger.copy(alpha = 0.15f), c.danger, s.debtOverdue)
    else
        Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), MaterialTheme.colorScheme.onSurfaceVariant, "${s.debtDuePrefix}: ${due.formatDate()}")
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = if (overdue) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun DebtActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    filled: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (filled) accent else accent.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, contentDescription = null, tint = if (filled) Color.White else accent, modifier = Modifier.size(15.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (filled) Color.White else accent, maxLines = 1)
        }
    }
}

// ─── Record payment dialog ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPaymentDialog(
    debt: Debt,
    currency: String,
    onDismiss: () -> Unit,
    onPay: (Double) -> Unit,
    onSettleAll: () -> Unit
) {
    val s = LocalStrings.current
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column {
                Text(s.debtPaymentTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "${debt.counterparty} · ${debt.remaining.formatCurrency(currency)} ${s.debtRemainingWord}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s.debtPaymentHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; isError = false },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    prefix = { Text(currencySymbol(currency) + " ", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline)
                )
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                        .clickable { onSettleAll() }.padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.debtPaymentFull, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(brandHorizontalGradient()).clickable {
                    val v = text.replace(',', '.').toDoubleOrNull()
                    if (v == null || v <= 0.0) { isError = true; return@clickable }
                    onPay(v)
                }.padding(horizontal = 22.dp, vertical = 11.dp)
            ) {
                Text(s.saveButton, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancelButton, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}
