package tj.mahram.lifetrack.feature.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.MonthNames
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.ui.components.GlassCard
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.components.parseHexColor
import tj.mahram.lifetrack.ui.theme.appColors
import kotlin.time.Clock

class DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<DashboardScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        DashboardContent(
            state = state,
            onRefresh = { screenModel.handleIntent(DashboardIntent.Refresh) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(state: DashboardState, onRefresh: () -> Unit) {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz)
    val dayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val dateStr = "$dayName, ${now.day} ${MonthNames[now.month.number - 1]}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "${state.greeting} 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier.padding(end = 12.dp).size(42.dp).glassSurface(shape = CircleShape).clickable(onClick = onRefresh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                DashStatGrid(state = state)

                state.monthlyFinance?.let { finance ->
                    FinanceMonthCard(finance = finance, currency = state.currency)
                }

                if (state.activeProblems.isNotEmpty()) {
                    ProblemsQuickWidget(problems = state.activeProblems, total = state.totalProblemsCount)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// STAT GRID
// ═══════════════════════════════════════════════════════════
@Composable
private fun DashStatGrid(state: DashboardState) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    val balance = state.balance?.currentBalance
    val balancePositive = (balance ?: 0.0) >= 0

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.TaskAlt,
                label = s.dashboardTasksToday,
                value = if (state.totalTasksToday == 0) "—" else "${state.completedTasksToday}/${state.totalTasksToday}",
                accent = MaterialTheme.colorScheme.primary
            )
            StatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Lightbulb,
                label = s.dashboardProblemsLabel,
                value = if (state.totalProblemsCount == 0) "—" else "${state.activeProblemsCount}",
                accent = c.danger
            )
        }
        StatTile(
            modifier = Modifier.fillMaxWidth(),
            icon = if (balancePositive) Icons.AutoMirrored.Outlined.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
            label = s.dashboardBalance,
            value = balance?.formatCurrency(state.currency) ?: "—",
            accent = if (balancePositive) c.success else c.danger
        )
    }
}

@Composable
private fun StatTile(modifier: Modifier = Modifier, icon: ImageVector, label: String, value: String, accent: Color) {
    GlassCard(modifier = modifier.height(116.dp), shape = RoundedCornerShape(24.dp), glow = accent, contentPadding = PaddingValues(16.dp)) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ═══════════════════════════════════════════════════════════
// FINANCE MONTH CARD
// ═══════════════════════════════════════════════════════════
@Composable
private fun FinanceMonthCard(finance: tj.mahram.lifetrack.domain.model.FinanceSummary, currency: String) {
    val s = LocalStrings.current
    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), shape = RoundedCornerShape(24.dp), contentPadding = PaddingValues(20.dp)) {
        Text(s.dashboardFinanceMonth, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            FinanceMiniStat(s.dashboardIncome, finance.totalIncome, true, currency)
            Divider34()
            FinanceMiniStat(s.dashboardExpense, finance.totalExpense, false, currency)
            Divider34()
            FinanceMiniStat(s.dashboardBalance, finance.balance, finance.balance >= 0, currency)
        }
    }
}

@Composable
private fun Divider34() {
    Box(Modifier.width(1.dp).height(36.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))
}

@Composable
private fun FinanceMiniStat(label: String, amount: Double, isPositive: Boolean, currency: String) {
    val c = MaterialTheme.appColors
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(amount.formatCurrency(currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isPositive) c.success else c.danger)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ═══════════════════════════════════════════════════════════
// ACTIVE PROBLEMS QUICK WIDGET
// ═══════════════════════════════════════════════════════════
@Composable
private fun ProblemsQuickWidget(problems: List<Problem>, total: Int) {
    val s = LocalStrings.current
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(s.dashboardActiveProblems, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(s.dashboardProblemsCount(problems.size, total), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        }
        problems.take(4).forEach { problem -> ProblemMiniRow(problem) }
    }
}

@Composable
private fun ProblemMiniRow(problem: Problem) {
    val accent = parseHexColor(problem.color)
    Row(
        modifier = Modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(16.dp), glow = accent).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accent))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(problem.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(accent.copy(alpha = 0.15f))) {
                Box(modifier = Modifier.fillMaxWidth(problem.progressFraction).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.6f)))))
            }
        }
        Text("${problem.progress}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accent)
    }
}
