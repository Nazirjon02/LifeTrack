package tj.mahram.lifetrack.feature.investments

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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.PieChart
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
import tj.mahram.lifetrack.core.i18n.InvestmentStrings
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.currencySymbol
import tj.mahram.lifetrack.domain.model.Investment
import tj.mahram.lifetrack.domain.model.InvestmentType
import tj.mahram.lifetrack.ui.components.FieldLabel
import tj.mahram.lifetrack.ui.components.SheetHandle
import tj.mahram.lifetrack.ui.components.SheetHeader
import tj.mahram.lifetrack.ui.components.SheetTextField
import tj.mahram.lifetrack.ui.components.parseHexColor

private val InvestmentColors = listOf(
    "#7C3AED", "#2563EB", "#10B981", "#F59E0B",
    "#EF4444", "#EC4899", "#14B8A6", "#6366F1"
)
private val InvestmentIcons = listOf("📈", "📊", "🏢", "🏠", "🪙", "💎", "💵", "🚀")

private fun defaultIconFor(type: InvestmentType): String = when (type) {
    InvestmentType.STOCK -> "📈"
    InvestmentType.FUND -> "📊"
    InvestmentType.BUSINESS -> "🏢"
    InvestmentType.METAL -> "🪙"
    InvestmentType.REAL_ESTATE -> "🏠"
    InvestmentType.OTHER -> "💰"
}

private fun iconFor(type: InvestmentType): ImageVector = when (type) {
    InvestmentType.STOCK -> Icons.AutoMirrored.Filled.ShowChart
    InvestmentType.FUND -> Icons.Default.PieChart
    InvestmentType.BUSINESS -> Icons.Default.Business
    InvestmentType.METAL -> Icons.Default.Diamond
    InvestmentType.REAL_ESTATE -> Icons.Default.HomeWork
    InvestmentType.OTHER -> Icons.Default.Category
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentSheet(
    editing: Investment?,
    strings: InvestmentStrings,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (InvestmentDraft) -> Unit
) {
    var type by remember { mutableStateOf(editing?.type ?: InvestmentType.STOCK) }
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var investedText by remember { mutableStateOf(editing?.amountInvested?.takeIf { it != 0.0 }?.toString() ?: "") }
    var currentText by remember { mutableStateOf(editing?.currentValue?.takeIf { it != 0.0 }?.toString() ?: "") }
    var note by remember { mutableStateOf(editing?.note ?: "") }
    var selectedColor by remember { mutableStateOf(editing?.color ?: InvestmentColors.first()) }
    var selectedIcon by remember { mutableStateOf(editing?.icon ?: defaultIconFor(editing?.type ?: InvestmentType.STOCK)) }
    // Lets the icon follow the type until the user picks one explicitly.
    var iconTouched by remember { mutableStateOf(editing != null) }

    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val accent = parseHexColor(selectedColor)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { SheetHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            SheetHeader(title = if (editing == null) strings.addTitle else strings.editTitle, onClose = onDismiss)

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                // Type (2×3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(strings.typeFieldLabel)
                    listOf(
                        InvestmentType.STOCK to InvestmentType.FUND,
                        InvestmentType.BUSINESS to InvestmentType.METAL,
                        InvestmentType.REAL_ESTATE to InvestmentType.OTHER
                    ).forEach { (left, right) ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf(left, right).forEach { t ->
                                InvestmentTypeOption(
                                    modifier = Modifier.weight(1f),
                                    label = strings.typeLabel(t),
                                    icon = iconFor(t),
                                    color = accent,
                                    selected = type == t,
                                    onClick = {
                                        type = t
                                        if (!iconTouched) selectedIcon = defaultIconFor(t)
                                    }
                                )
                            }
                        }
                    }
                }

                // Name
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(strings.nameLabel)
                    SheetTextField(
                        value = name,
                        onValueChange = { name = it; nameError = false },
                        placeholder = strings.namePlaceholder,
                        isError = nameError,
                        errorText = strings.nameError
                    )
                }

                // Amount invested
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(strings.amountInvestedLabel)
                    AmountField(
                        value = investedText,
                        onValueChange = { investedText = it; amountError = false },
                        currency = currency,
                        accent = accent,
                        isError = amountError
                    )
                }

                // Current value
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(strings.currentValueLabel)
                    AmountField(
                        value = currentText,
                        onValueChange = { currentText = it },
                        currency = currency,
                        accent = accent,
                        isError = false
                    )
                }

                // Note
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(strings.noteLabel)
                    SheetTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = strings.notePlaceholder
                    )
                }

                // Icon
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldLabel(strings.iconLabel)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        InvestmentIcons.forEach { emoji ->
                            val selected = selectedIcon == emoji
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    .border(width = if (selected) 1.5.dp else 1.dp, color = if (selected) accent else Color.Transparent, shape = RoundedCornerShape(12.dp))
                                    .clickable { selectedIcon = emoji; iconTouched = true },
                                contentAlignment = Alignment.Center
                            ) { Text(emoji, fontSize = 18.sp) }
                        }
                    }
                }

                // Color
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldLabel(strings.colorLabel)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        InvestmentColors.forEach { hex ->
                            val col = parseHexColor(hex)
                            val selected = selectedColor == hex
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f)
                                    .clip(CircleShape).background(col)
                                    .border(width = if (selected) 3.dp else 0.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (selected) 0.9f else 0f), shape = CircleShape)
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accent)
                    .clickable {
                        val trimmed = name.trim()
                        if (trimmed.isEmpty()) { nameError = true; return@clickable }
                        val invested = if (investedText.isBlank()) 0.0
                            else investedText.replace(',', '.').toDoubleOrNull() ?: run { amountError = true; return@clickable }
                        // Current value defaults to the invested amount (0% P/L) when left blank.
                        val current = if (currentText.isBlank()) invested
                            else currentText.replace(',', '.').toDoubleOrNull() ?: invested

                        onConfirm(
                            InvestmentDraft(
                                id = editing?.id,
                                name = trimmed,
                                type = type,
                                amountInvested = invested,
                                currentValue = current,
                                note = note.trim().takeIf { it.isNotEmpty() },
                                icon = selectedIcon,
                                color = selectedColor,
                                createdAt = editing?.createdAt
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(if (editing == null) strings.createButton else LocalStrings.current.saveButton, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    currency: String,
    accent: Color,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = isError,
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

@Composable
private fun InvestmentTypeOption(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (selected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        tween(200), label = "investmentTypeBg"
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(width = if (selected) 1.5.dp else 1.dp, color = if (selected) color else Color.Transparent, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) color else MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}
