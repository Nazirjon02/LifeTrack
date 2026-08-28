package tj.mahram.lifetrack.feature.debts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.currencySymbol
import tj.mahram.lifetrack.domain.model.DebtType
import tj.mahram.lifetrack.ui.components.FieldLabel
import tj.mahram.lifetrack.ui.components.SheetHandle
import tj.mahram.lifetrack.ui.components.SheetTextField
import tj.mahram.lifetrack.ui.theme.appColors

private fun colorForType(type: DebtType): String =
    if (type == DebtType.LENT) "#10B981" else "#EF4444"

private fun dueFromNow(days: Int): Instant =
    Clock.System.now().plus(days, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtSheet(
    presetType: DebtType,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (DebtsIntent.CreateDebt) -> Unit
) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors

    var type by remember { mutableStateOf(presetType) }
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dueDays by remember { mutableStateOf<Int?>(null) }
    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val accent = if (type == DebtType.LENT) c.success else c.danger

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { SheetHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.7f))))
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(s.debtAddTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)).clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                // Direction toggle
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(s.debtDirectionLabel)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        DirectionOption(
                            modifier = Modifier.weight(1f),
                            label = s.debtAddLent,
                            icon = Icons.Default.ArrowUpward,
                            color = c.success,
                            selected = type == DebtType.LENT,
                            onClick = { type = DebtType.LENT }
                        )
                        DirectionOption(
                            modifier = Modifier.weight(1f),
                            label = s.debtAddBorrowed,
                            icon = Icons.Default.ArrowDownward,
                            color = c.danger,
                            selected = type == DebtType.BORROWED,
                            onClick = { type = DebtType.BORROWED }
                        )
                    }
                }

                // Person
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(s.debtPersonLabel)
                    SheetTextField(
                        value = name,
                        onValueChange = { name = it; nameError = false },
                        placeholder = s.debtPersonPlaceholder,
                        isError = nameError,
                        errorText = s.debtNameError
                    )
                }

                // Amount
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(s.debtAmountLabel)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it; amountError = false },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = amountError,
                        prefix = { Text(currencySymbol(currency) + " ", color = accent, fontWeight = FontWeight.Bold) },
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

                // Due date quick picks
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldLabel(s.debtDueLabel)
                    val options = listOf(
                        s.debtDueNone to null,
                        s.debtDueWeek to 7,
                        s.debtDue2Weeks to 14,
                        s.debtDueMonth to 30,
                        s.debtDue3Months to 90
                    )
                    FlowRowChips(
                        options = options.map { it.first },
                        selectedIndex = options.indexOfFirst { it.second == dueDays },
                        accent = accent,
                        onSelect = { idx -> dueDays = options[idx].second }
                    )
                }

                // Note
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(s.debtNoteLabel)
                    SheetTextField(value = note, onValueChange = { note = it }, placeholder = s.debtNoteLabel)
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
                        val trimmedName = name.trim()
                        if (trimmedName.isEmpty()) { nameError = true; return@clickable }
                        val amount = amountText.replace(',', '.').toDoubleOrNull()
                        if (amount == null || amount <= 0) { amountError = true; return@clickable }
                        onConfirm(
                            DebtsIntent.CreateDebt(
                                type = type,
                                counterparty = trimmedName,
                                amount = amount,
                                note = note.ifBlank { null },
                                dueDate = dueDays?.let { dueFromNow(it) },
                                color = colorForType(type)
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(s.debtCreateButton, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun DirectionOption(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (selected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        tween(200), label = "dirBg"
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(width = if (selected) 1.5.dp else 1.dp, color = if (selected) color else Color.Transparent, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(CircleShape).background(if (selected) color else color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) Color.White else color, modifier = Modifier.size(18.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) color else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FlowRowChips(
    options: List<String>,
    selectedIndex: Int,
    accent: Color,
    onSelect: (Int) -> Unit
) {
    // Simple two-row wrap without experimental FlowRow: chunk into rows of 3.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(3).forEachIndexed { rowIdx, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEachIndexed { colIdx, label ->
                    val index = rowIdx * 3 + colIdx
                    val selected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .border(width = if (selected) 1.5.dp else 1.dp, color = if (selected) accent else Color.Transparent, shape = RoundedCornerShape(12.dp))
                            .clickable { onSelect(index) }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
                repeat(3 - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}
