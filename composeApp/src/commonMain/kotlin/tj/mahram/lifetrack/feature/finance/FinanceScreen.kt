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
import androidx.compose.ui.graphics.StrokeJoin
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
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.FinanceSummary
import tj.mahram.lifetrack.domain.model.Transaction
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.feature.habits.parseHabitColor
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.TransactionCard
import tj.mahram.lifetrack.ui.theme.ErrorColor
import tj.mahram.lifetrack.ui.theme.SuccessColor

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
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { onIntent(FinanceIntent.ShowAddIncome) },
                    containerColor = SuccessColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Add income", modifier = Modifier.size(20.dp))
                }
                FloatingActionButton(
                    onClick = { onIntent(FinanceIntent.ShowAddExpense) },
                    containerColor = ErrorColor,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Add expense")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 96.dp
            )
        ) {
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Finance",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Track your money",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item(key = "balance") {
                state.summary?.let { summary ->
                    FinanceBalanceHero(summary = summary, currency = state.currency)
                }
            }

            if (state.transactions.size >= 2) {
                item(key = "chart") {
                    FinanceSparklineCard(transactions = state.filteredTransactions.take(30))
                }
            }

            item(key = "donut") {
                val expensesByCategory = state.transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .mapValues { (_, txns) -> txns.sumOf { it.amount } }
                if (expensesByCategory.size >= 2) {
                    ExpenseDonutCard(
                        expensesByCategory = expensesByCategory,
                        categories = state.categories,
                        currency = state.currency
                    )
                }
            }

            item(key = "tabs") {
                FinancePillTabs(
                    selectedType = state.selectedType,
                    onSelect = { onIntent(FinanceIntent.SelectType(it)) }
                )
            }

            if (state.filteredTransactions.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        emoji = "💰",
                        title = "No transactions",
                        subtitle = "Tap + to add your first transaction",
                        modifier = Modifier.padding(top = 48.dp)
                    )
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A0A40),
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                "Total Balance",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                summary.balance.formatCurrency(currency),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (summary.balance >= 0) Color.White else ErrorColor,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeroStatPill(
                    modifier = Modifier.weight(1f),
                    label = "Income",
                    amount = summary.totalIncome,
                    currency = currency,
                    color = SuccessColor,
                    arrowUp = true
                )
                HeroStatPill(
                    modifier = Modifier.weight(1f),
                    label = "Expenses",
                    amount = summary.totalExpense,
                    currency = currency,
                    color = ErrorColor,
                    arrowUp = false
                )
            }
        }
    }
}

@Composable
private fun HeroStatPill(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    currency: String,
    color: Color,
    arrowUp: Boolean
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (arrowUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(11.dp)
                    )
                }
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Text(
                amount.formatCurrency(currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─── Pill Tab Row ──────────────────────────────────────────────────────────────

@Composable
private fun FinancePillTabs(
    selectedType: TransactionType?,
    onSelect: (TransactionType?) -> Unit
) {
    val tabs = listOf<Pair<String, TransactionType?>>(
        "All" to null,
        "Expenses" to TransactionType.EXPENSE,
        "Income" to TransactionType.INCOME
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEach { (label, type) ->
            val isSelected = selectedType == type
            val bg by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(200),
                label = "tabBg_$label"
            )
            val fg by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                              else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "tabFg_$label"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .clickable { onSelect(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = fg
                )
            }
        }
    }
}

// ─── Expense Donut Chart ──────────────────────────────────────────────────────

@Composable
private fun ExpenseDonutCard(
    expensesByCategory: Map<String, Double>,
    categories: List<Category>,
    currency: String
) {
    val total = expensesByCategory.values.sum().coerceAtLeast(0.01)
    val entries = expensesByCategory.entries
        .sortedByDescending { it.value }
        .take(6)
        .map { (catId, amount) ->
            val cat = categories.find { it.id == catId }
            Triple(cat?.name ?: "Other", parseHabitColor(cat?.color ?: "#6B7280"), amount)
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Expense Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                        var startAngle = -90f
                        val gapAngle = 2f
                        entries.forEach { (_, color, amount) ->
                            val sweep = ((amount / total) * 360f).toFloat() - gapAngle
                            if (sweep > 0f) {
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = stroke
                                )
                                startAngle += sweep + gapAngle
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            total.formatCurrency(currency),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "total",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entries.forEach { (name, color, amount) ->
                        val pct = ((amount / total) * 100).toInt()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Text(
                                name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "$pct%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Sparkline Chart ──────────────────────────────────────────────────────────

@Composable
private fun FinanceSparklineCard(transactions: List<Transaction>) {
    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
    if (expenses.size < 2) return

    val amounts = expenses.map { it.amount.toFloat() }
    val maxAmt = amounts.max()
    val minAmt = amounts.min()
    val range = (maxAmt - minAmt).coerceAtLeast(0.01f)

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "sparkline"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Expense Trend",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorColor.copy(alpha = 0.10f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${expenses.size} records",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
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
                    if (i == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo((vis.size - 1) * step, h)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(ErrorColor.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
                drawPath(
                    path = path,
                    color = ErrorColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                val lastX = (vis.size - 1) * step
                val lastY = yOf(vis.last())
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(lastX, lastY))
                drawCircle(color = ErrorColor, radius = 3.5.dp.toPx(), center = Offset(lastX, lastY))
            }
        }
    }
}
