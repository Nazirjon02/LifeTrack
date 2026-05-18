package tj.mahram.lifetrack.feature.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.ui.theme.ErrorColor
import tj.mahram.lifetrack.ui.theme.SuccessColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    type: TransactionType,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (FinanceIntent.AddTransaction) -> Unit
) {
    val catType = when (type) {
        TransactionType.EXPENSE -> tj.mahram.lifetrack.domain.model.CategoryType.EXPENSE
        TransactionType.INCOME -> tj.mahram.lifetrack.domain.model.CategoryType.INCOME
    }
    val relevantCats = categories.filter { it.type == catType }

    val s = LocalStrings.current
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(relevantCats.firstOrNull()) }
    var note by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    var catError by remember { mutableStateOf(false) }

    val accentColor = if (type == TransactionType.INCOME) SuccessColor else ErrorColor

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${s.cryptoAddButton} ${s.transactionTypeLabel(type)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = accentColor
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it; amountError = false },
                label = { Text(s.financeAmountLabel) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                supportingText = if (amountError) {{ Text(s.financeAmountError) }} else null,
                prefix = { Text(if (type == TransactionType.INCOME) "+" else "-") },
                singleLine = true
            )

            if (relevantCats.isNotEmpty()) {
                Text(s.financeCategoryLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    relevantCats.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory?.id == cat.id,
                                    onClick = { selectedCategory = cat; catError = false },
                                    label = { Text("${cat.icon} ${cat.name}", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(s.financeNoteLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) { amountError = true; return@Button }
                    if (selectedCategory == null) { catError = true; return@Button }
                    onConfirm(
                        FinanceIntent.AddTransaction(
                            amount = amount,
                            type = type,
                            categoryId = selectedCategory!!.id,
                            note = note.ifBlank { null }
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("${s.cryptoAddButton} ${s.transactionTypeLabel(type)}")
            }
        }
    }
}
