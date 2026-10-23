package tj.mahram.lifetrack.feature.investments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.InvestmentStrings
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.i18n.investmentStringsFor
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.domain.model.Investment
import tj.mahram.lifetrack.domain.model.InvestmentsOverview
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.brandVividGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.components.parseHexColor
import tj.mahram.lifetrack.ui.theme.appColors

class InvestmentsScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel<InvestmentsScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        InvestmentsContent(
            state = state,
            onIntent = screenModel::handleIntent,
            onBack = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsContent(
    state: InvestmentsState,
    onIntent: (InvestmentsIntent) -> Unit,
    onBack: () -> Unit
) {
    val s = LocalStrings.current
    val iv = investmentStringsFor(state.language)
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                    .clickable { onIntent(InvestmentsIntent.ShowAddSheet) },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Add, contentDescription = iv.addTitle, tint = Color.White, modifier = Modifier.size(28.dp)) }
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
                        Text(iv.screenTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                        Text(iv.screenSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item(key = "hero") { InvestmentsHero(overview = state.overview, currency = state.currency, strings = iv) }

            if (state.overview.items.isEmpty()) {
                item(key = "empty") {
                    EmptyState(emoji = "📈", title = iv.emptyTitle, subtitle = iv.emptySubtitle, modifier = Modifier.padding(top = 24.dp))
                }
            } else {
                items(state.overview.items, key = { it.id }) { investment ->
                    InvestmentCard(
                        investment = investment,
                        strings = iv,
                        onEdit = { onIntent(InvestmentsIntent.ShowEditSheet(investment)) },
                        onDelete = { onIntent(InvestmentsIntent.RequestDelete(investment)) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        if (state.showSheet) {
            AddInvestmentSheet(
                editing = state.editing,
                strings = iv,
                currency = state.currency,
                onDismiss = { onIntent(InvestmentsIntent.HideSheet) },
                onConfirm = { onIntent(InvestmentsIntent.SaveInvestment(it)) }
            )
        }

        state.deleteTarget?.let { target ->
            AlertDialog(
                onDismissRequest = { onIntent(InvestmentsIntent.CancelDelete) },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp),
                title = { Text(iv.deleteTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                text = { Text(iv.deleteMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                confirmButton = {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(MaterialTheme.appColors.danger)
                            .clickable { onIntent(InvestmentsIntent.ConfirmDelete(target.id)) }.padding(horizontal = 22.dp, vertical = 11.dp)
                    ) { Text(iv.deleteButton, color = Color.White, fontWeight = FontWeight.SemiBold) }
                },
                dismissButton = { TextButton(onClick = { onIntent(InvestmentsIntent.CancelDelete) }) { Text(s.cancelButton, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            )
        }
    }
}

// ─── Hero ────────────────────────────────────────────────────────────────────
@Composable
private fun InvestmentsHero(overview: InvestmentsOverview, currency: String, strings: InvestmentStrings) {
    val up = overview.totalProfit >= 0
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp)).background(brandVividGradient()).padding(24.dp)
    ) {
        Column {
            Text(strings.portfolioValueLabel, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(
                overview.totalCurrentValue.formatCurrency(currency),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${strings.investedLabel} ${overview.totalInvested.formatCurrency(currency)} · ${strings.countText(overview.count)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.16f)).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
                    Icon(if (up) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                }
                Column {
                    Text(strings.totalProfitLabel, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                    Text(
                        "${overview.totalProfit.formatCurrency(currency, showSign = true)}  ·  ${strings.percentText(overview.overallReturnPercent)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ─── Investment card ───────────────────────────────────────────────────────────
@Composable
private fun InvestmentCard(
    investment: Investment,
    strings: InvestmentStrings,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = parseHexColor(investment.color)
    val c = MaterialTheme.appColors
    val up = investment.profit >= 0
    val plColor = if (up) c.success else c.danger

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .glassSurface(shape = RoundedCornerShape(20.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) { Text(investment.icon, fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(investment.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Spacer(Modifier.height(3.dp))
                TypeChip(label = strings.typeLabel(investment.type), accent = accent)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    investment.currentValue.formatCurrency(investment.currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(if (up) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, tint = plColor, modifier = Modifier.size(13.dp))
                    Text(strings.percentText(investment.returnPercent), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = plColor)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // invested → current, with the signed P/L amount on the right.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(investment.amountInvested.formatCurrency(investment.currency), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("→", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(investment.currentValue.formatCurrency(investment.currency), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(investment.profit.formatCurrency(investment.currency, showSign = true), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = plColor)
        }

        if (!investment.note.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(investment.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
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
