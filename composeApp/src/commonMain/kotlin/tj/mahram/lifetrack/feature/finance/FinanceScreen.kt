package tj.mahram.lifetrack.feature.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.SummaryCard
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
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Finance") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                // Summary row
                state.summary?.let { summary ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryCard(
                            title = "Income",
                            amount = summary.totalIncome,
                            currency = state.currency,
                            isPositive = true,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Expense",
                            amount = summary.totalExpense,
                            currency = state.currency,
                            isPositive = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                summary.balance.formatCurrency(state.currency),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (summary.balance >= 0) SuccessColor else ErrorColor
                            )
                        }
                    }
                }
                // Filter tabs
                TabRow(
                    selectedTabIndex = when (state.selectedType) {
                        null -> 0; TransactionType.EXPENSE -> 1; TransactionType.INCOME -> 2
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    divider = {}
                ) {
                    Tab(selected = state.selectedType == null, onClick = { onIntent(FinanceIntent.SelectType(null)) }, text = { Text("All") })
                    Tab(selected = state.selectedType == TransactionType.EXPENSE, onClick = { onIntent(FinanceIntent.SelectType(TransactionType.EXPENSE)) }, text = { Text("Expenses") })
                    Tab(selected = state.selectedType == TransactionType.INCOME, onClick = { onIntent(FinanceIntent.SelectType(TransactionType.INCOME)) }, text = { Text("Income") })
                }
            }
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(
                    onClick = { onIntent(FinanceIntent.ShowAddIncome) },
                    containerColor = SuccessColor
                ) {
                    Text("+", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
                FloatingActionButton(
                    onClick = { onIntent(FinanceIntent.ShowAddExpense) },
                    containerColor = ErrorColor
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add expense", tint = MaterialTheme.colorScheme.onError)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.filteredTransactions.isEmpty()) {
            EmptyState(
                emoji = "💰",
                title = "No transactions",
                subtitle = "Tap + to add your first transaction",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 96.dp
                )
            ) {
                items(state.filteredTransactions, key = { it.id }) { txn ->
                    val cat = state.categoryById(txn.categoryId)
                    TransactionCard(
                        transaction = txn,
                        categoryIcon = cat?.icon ?: "💵",
                        categoryName = cat?.name ?: "Other",
                        onDelete = { onIntent(FinanceIntent.DeleteTransaction(txn.id)) }
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
