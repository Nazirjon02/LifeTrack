package tj.mahram.lifetrack.feature.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.TransactionType
import tj.mahram.lifetrack.ui.components.parseHexColor
import tj.mahram.lifetrack.ui.components.FieldLabel
import tj.mahram.lifetrack.ui.components.SheetHandle
import tj.mahram.lifetrack.ui.components.SheetTextField
import tj.mahram.lifetrack.ui.theme.appColors

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
    val c = MaterialTheme.appColors
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(relevantCats.firstOrNull()) }
    var note by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    val accent = if (type == TransactionType.INCOME) c.success else c.danger

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { SheetHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            // Colored header (income = green, expense = red)
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.7f))))
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${s.cryptoAddButton} ${s.transactionTypeLabel(type)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)).clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(s.financeAmountLabel)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it; amountError = false },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = amountError,
                        supportingText = if (amountError) ({ Text(s.financeAmountError, color = MaterialTheme.colorScheme.error) }) else null,
                        prefix = { Text(if (type == TransactionType.INCOME) "+ " else "- ", color = accent, fontWeight = FontWeight.Bold) },
                        placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                        )
                    )
                }

                if (relevantCats.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FieldLabel(s.financeCategoryLabel)
                        relevantCats.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                row.forEach { cat ->
                                    val catColor = parseHexColor(cat.color)
                                    val isSelected = selectedCategory?.id == cat.id
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                            .border(width = if (isSelected) 1.5.dp else 1.dp, color = if (isSelected) catColor else Color.Transparent, shape = RoundedCornerShape(14.dp))
                                            .clickable { selectedCategory = cat }
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${cat.icon} ${cat.name}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                                repeat(3 - row.size) { Box(Modifier.weight(1f)) }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(s.financeNoteLabel)
                    SheetTextField(value = note, onValueChange = { note = it }, placeholder = s.financeNoteLabel)
                }
            }

            Spacer(Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.75f))))
                    .clickable {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0) { amountError = true; return@clickable }
                        val cat = selectedCategory ?: return@clickable
                        onConfirm(FinanceIntent.AddTransaction(amount = amount, type = type, categoryId = cat.id, note = note.ifBlank { null }))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("${s.cryptoAddButton} ${s.transactionTypeLabel(type)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
