package tj.mahram.lifetrack.feature.finance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.FinanceSummary
import tj.mahram.lifetrack.domain.model.Transaction
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.feature.habits.parseHabitColor
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.TransactionCard
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.brandVividGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.theme.appColors

class FinanceScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<FinanceScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        FinanceContent(state = state, onIntent = screenModel::handleIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceContent(state: FinanceState, onIntent: (FinanceIntent) -> Unit) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(c.success).clickable { onIntent(FinanceIntent.ShowAddIncome) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ArrowUpward, contentDescription = "Add income", tint = Color.White, modifier = Modifier.size(22.dp)) }
                Box(
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(c.danger).clickable { onIntent(FinanceIntent.ShowAddExpense) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ArrowDownward, contentDescription = "Add expense", tint = Color.White, modifier = Modifier.size(26.dp)) }
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 96.dp)
        ) {
            item(key = "header") {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 4.dp)) {
                    Text(s.financeTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text(s.financeSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item(key = "balance") {
                state.summary?.let { summary -> FinanceBalanceHero(summary = summary, currency = state.currency) }
            }

            if (state.transactions.size >= 2) {
                item(key = "chart") { FinanceSparklineCard(transactions = state.filteredTransactions.take(30)) }
            }

            item(key = "donut") {
                val expensesByCategory = state.transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .mapValues { (_, txns) -> txns.sumOf { it.amount } }
                if (expensesByCategory.size >= 2) {
                    ExpenseDonutCard(expensesByCategory = expensesByCategory, categories = state.categories, currency = state.currency)
                }
            }

            item(key = "tabs") {
                FinancePillTabs(selectedType = state.selectedType, onSelect = { onIntent(FinanceIntent.SelectType(it)) })
            }

            if (state.filteredTransactions.isEmpty()) {
                item(key = "empty") {
                    EmptyState(emoji = "💰", title = s.financeEmptyTitle, subtitle = s.financeEmptySubtitle, modifier = Modifier.padding(top = 32.dp))
                }
            } else {
                items(state.filteredTransactions, key = { it.id }) { txn ->
                    val cat = state.categoryById(txn.categoryId)
                    TransactionCard(
                        transaction = txn,
                        categoryIcon = cat?.icon ?: "💵",
                        categoryName = cat?.name ?: "Other",
                        onDelete = { onIntent(FinanceIntent.DeleteTransaction(txn.id)) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        if (state.showAddSheet) {
            AddTransactionSheet(
                type = state.addSheetType,
                categories = state.categories,
                onDismiss = { onIntent(FinanceIntent.HideAddSheet) },
                onConfirm = onIntent
            )
        }
    }
}

// ─── Balance Hero ──────────────────────────────────────────────────────────────
@Composable
private fun FinanceBalanceHero(summary: FinanceSummary, currency: String) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(brandVividGradient())
            .padding(24.dp)
    ) {
        Column {
            Text(s.financeTotalBalance, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(
                summary.balance.formatCurrency(currency),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (summary.balance >= 0) Color.White else Color(0xFFFFD7DE),
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeroStatPill(modifier = Modifier.weight(1f), label = s.financeIncomeLabel, amount = summary.totalIncome, currency = currency, color = c.success, arrowUp = true)
                HeroStatPill(modifier = Modifier.weight(1f), label = s.financeExpensesLabel, amount = summary.totalExpense, currency = currency, color = Color(0xFFFFB4C0), arrowUp = false)
            }
        }
    }
}

@Composable
private fun HeroStatPill(modifier: Modifier = Modifier, label: String, amount: Double, currency: String, color: Color, arrowUp: Boolean) {
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

// ─── Pill Tab Row ──────────────────────────────────────────────────────────────
@Composable
private fun FinancePillTabs(selectedType: TransactionType?, onSelect: (TransactionType?) -> Unit) {
    val s = LocalStrings.current
    val brand = brandHorizontalGradient()
    val tabs = listOf<Pair<String, TransactionType?>>(
        s.financeTabAll to null,
        s.financeTabExpenses to TransactionType.EXPENSE,
        s.financeTabIncome to TransactionType.INCOME
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).glassSurface(shape = RoundedCornerShape(16.dp)).padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        tabs.forEach { (label, type) ->
            val isSelected = selectedType == type
            val fg by animateColorAsState(targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, animationSpec = tween(200), label = "tabFg_$label")
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).then(if (isSelected) Modifier.background(brand, RoundedCornerShape(12.dp)) else Modifier).clickable { onSelect(type) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = fg)
            }
        }
    }
}

// ─── Expense Donut Chart ──────────────────────────────────────────────────────
@Composable
private fun ExpenseDonutCard(expensesByCategory: Map<String, Double>, categories: List<Category>, currency: String) {
    val s = LocalStrings.current
    val total = expensesByCategory.values.sum().coerceAtLeast(0.01)
    val entries = expensesByCategory.entries.sortedByDescending { it.value }.take(6).map { (catId, amount) ->
        val cat = categories.find { it.id == catId }
        Triple(cat?.name ?: "Other", parseHabitColor(cat?.color ?: "#6B7280"), amount)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).glassSurface(shape = RoundedCornerShape(22.dp)).padding(20.dp)
    ) {
        Text(s.financeExpenseBreakdown, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(modifier = Modifier.size(124.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                    var startAngle = -90f
                    val gapAngle = 3f
                    entries.forEach { (_, color, amount) ->
                        val sweep = ((amount / total) * 360f).toFloat() - gapAngle
                        if (sweep > 0f) {
                            drawArc(color = color, startAngle = startAngle, sweepAngle = sweep, useCenter = false, style = stroke)
                            startAngle += sweep + gapAngle
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(total.formatCurrency(currency), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(s.financeTotalLabel, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                entries.forEach { (name, color, amount) ->
                    val pct = ((amount / total) * 100).toInt()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                        Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Text("$pct%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
                    }
                }
            }
        }
    }
}

// ─── Sparkline Chart ──────────────────────────────────────────────────────────
@Composable
private fun FinanceSparklineCard(transactions: List<Transaction>) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
    if (expenses.size < 2) return

    val amounts = expenses.map { it.amount.toFloat() }
    val maxAmt = amounts.max()
    val minAmt = amounts.min()
    val range = (maxAmt - minAmt).coerceAtLeast(0.01f)
    val lineColor = c.danger

    val animProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(1200, easing = FastOutSlowInEasing), label = "sparkline")

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).glassSurface(shape = RoundedCornerShape(22.dp)).padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(s.financeExpenseTrend, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(lineColor.copy(alpha = 0.14f)).padding(horizontal = 9.dp, vertical = 4.dp)) {
                Text(s.financeRecords(expenses.size), style = MaterialTheme.typography.labelSmall, color = lineColor, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(14.dp))

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(72.dp)) {
            val w = size.width
            val h = size.height
            val pad = 4.dp.toPx()
            val step = if (amounts.size > 1) w / (amounts.size - 1) else w
            val visCount = (amounts.size * animProgress).toInt().coerceAtLeast(2)
            val vis = amounts.take(visCount)

            fun yOf(amt: Float) = pad + (h - 2 * pad) * (1f - (amt - minAmt) / range)

            val path = Path()
            val fillPath = Path()
            vis.forEachIndexed { i, amt ->
                val x = i * step
                val y = yOf(amt)
                if (i == 0) { path.moveTo(x, y); fillPath.moveTo(x, h); fillPath.lineTo(x, y) }
                else { path.lineTo(x, y); fillPath.lineTo(x, y) }
            }
            fillPath.lineTo((vis.size - 1) * step, h)
            fillPath.close()

            drawPath(path = fillPath, brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.25f), Color.Transparent)))
            drawPath(path = path, color = lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

            val lastX = (vis.size - 1) * step
            val lastY = yOf(vis.last())
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(lastX, lastY))
            drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = Offset(lastX, lastY))
        }
    }
}
