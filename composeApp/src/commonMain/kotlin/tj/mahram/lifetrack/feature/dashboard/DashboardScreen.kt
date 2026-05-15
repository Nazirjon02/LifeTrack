package tj.mahram.lifetrack.feature.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.util.MonthNames
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.ui.theme.ErrorColor
import tj.mahram.lifetrack.ui.theme.SuccessColor

class DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<DashboardScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        DashboardContent(state = state, onRefresh = screenModel::refresh)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(state: DashboardState, onRefresh: () -> Unit) {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz)
    val dateStr = "${now.dayOfMonth} ${MonthNames[now.monthNumber - 1]} ${now.year}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.greeting, style = MaterialTheme.typography.titleLarge)
                        Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            // Monthly finance card
            state.monthlyFinance?.let { finance ->
                DashboardCard(title = "This Month") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        BalanceMini("Income",  finance.totalIncome,  isPositive = true,  currency = state.currency)
                        BalanceMini("Expense", finance.totalExpense, isPositive = false, currency = state.currency)
                        BalanceMini("Balance", finance.balance, isPositive = finance.balance >= 0, currency = state.currency)
                    }
                }
            }

            // Tasks card
            DashboardCard(title = "Today's Tasks") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = {
                                if (state.totalTasksToday > 0)
                                    state.completedTasksToday.toFloat() / state.totalTasksToday
                                else 0f
                            },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 5.dp
                        )
                        Text(
                            "${state.completedTasksToday}/${state.totalTasksToday}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        if (state.totalTasksToday == 0) "No tasks today — enjoy! 🎉"
                        else "${state.totalTasksToday - state.completedTasksToday} remaining",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun BalanceMini(label: String, amount: Double, isPositive: Boolean, currency: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            amount.formatCurrency(currency),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isPositive) SuccessColor else ErrorColor
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
