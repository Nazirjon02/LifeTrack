package tj.mahram.lifetrack.feature.wallets

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
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
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.i18n.WalletStrings
import tj.mahram.lifetrack.core.util.currencySymbol
import tj.mahram.lifetrack.domain.model.Wallet
import tj.mahram.lifetrack.domain.model.WalletType
import tj.mahram.lifetrack.ui.components.FieldLabel
import tj.mahram.lifetrack.ui.components.SheetHandle
import tj.mahram.lifetrack.ui.components.SheetHeader
import tj.mahram.lifetrack.ui.components.SheetTextField
import tj.mahram.lifetrack.ui.components.parseHexColor
import tj.mahram.lifetrack.ui.theme.appColors

private val WalletColors = listOf(
    "#7C3AED", "#2563EB", "#10B981", "#F59E0B",
    "#EF4444", "#EC4899", "#14B8A6", "#6366F1"
)
private val WalletIcons = listOf("💳", "💵", "🏦", "🐷", "💰", "🪙", "💎", "📈")

private fun defaultIconFor(type: WalletType): String = when (type) {
    WalletType.CASH -> "💵"
    WalletType.CARD -> "💳"
    WalletType.SAVINGS -> "🐷"
    WalletType.DEPOSIT -> "🏦"
}

private fun iconFor(type: WalletType): ImageVector = when (type) {
    WalletType.CASH -> Icons.Default.AccountBalanceWallet
    WalletType.CARD -> Icons.Default.CreditCard
    WalletType.SAVINGS -> Icons.Default.Savings
    WalletType.DEPOSIT -> Icons.Default.AccountBalance
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalletSheet(
    editing: Wallet?,
    strings: WalletStrings,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (WalletDraft) -> Unit
) {
    val c = MaterialTheme.appColors

    var type by remember { mutableStateOf(editing?.type ?: WalletType.CASH) }
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var balanceText by remember { mutableStateOf(editing?.balance?.takeIf { it != 0.0 }?.toString() ?: "") }
    var rateText by remember { mutableStateOf(editing?.interestRate?.toString() ?: "") }
    var termMonths by remember { mutableStateOf(initialTerm(editing)) }
    var selectedColor by remember { mutableStateOf(editing?.color ?: WalletColors.first()) }
    var selectedIcon by remember { mutableStateOf(editing?.icon ?: defaultIconFor(editing?.type ?: WalletType.CASH)) }
    // Lets the icon follow the type until the user picks one explicitly.
    var iconTouched by remember { mutableStateOf(editing != null) }

    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var rateError by remember { mutableStateOf(false) }

    val accent = parseHexColor(selectedColor)
    val isDeposit = type == WalletType.DEPOSIT

    val termOptions = listOf(
        strings.term3Months to 3,
        strings.term6Months to 6,
        strings.term1Year to 12,
        strings.term2Years to 24
    )

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
                // Type (2×2)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(strings.typeFieldLabel)
                    listOf(
                        WalletType.CASH to WalletType.CARD,
                        WalletType.SAVINGS to WalletType.DEPOSIT
                    ).forEach { (left, right) ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf(left, right).forEach { t ->
                                WalletTypeOption(
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

                // Balance / principal
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(if (isDeposit) strings.principalLabel else strings.balanceLabel)
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it; amountError = false },
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

                // Deposit details
                if (isDeposit) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FieldLabel(strings.depositSection)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FieldLabel(strings.rateLabel)
                            OutlinedTextField(
                                value = rateText,
                                onValueChange = { rateText = it; rateError = false },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = rateError,
                                supportingText = if (rateError) ({ Text(strings.rateError, color = MaterialTheme.colorScheme.error) }) else null,
                                suffix = { Text("%", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                placeholder = { Text("0.0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accent,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                                )
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FieldLabel(strings.termLabel)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                termOptions.forEach { (label, months) ->
                                    val selected = termMonths == months
                                    Box(
                                        modifier = Modifier.weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (selected) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                            .border(width = if (selected) 1.5.dp else 1.dp, color = if (selected) accent else Color.Transparent, shape = RoundedCornerShape(12.dp))
                                            .clickable { termMonths = months }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) accent else MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                // Icon
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldLabel(strings.iconLabel)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        WalletIcons.forEach { emoji ->
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
                        WalletColors.forEach { hex ->
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
                        val balance = balanceText.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (isDeposit && balance <= 0.0) { amountError = true; return@clickable }

                        var rate: Double? = null
                        var start = editing?.startDate
                        var end = editing?.endDate
                        if (isDeposit) {
                            val r = rateText.replace(',', '.').toDoubleOrNull()
                            if (r == null || r < 0.0) { rateError = true; return@clickable }
                            rate = r
                            val s = start ?: Clock.System.now()
                            start = s
                            end = s.plus(termMonths, DateTimeUnit.MONTH, TimeZone.currentSystemDefault())
                        }

                        onConfirm(
                            WalletDraft(
                                id = editing?.id,
                                name = trimmed,
                                type = type,
                                balance = balance,
                                icon = selectedIcon,
                                color = selectedColor,
                                interestRate = rate,
                                startDate = if (isDeposit) start else null,
                                endDate = if (isDeposit) end else null,
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

/** Best-matching preset term (months) for an existing deposit, default 1 year. */
private fun initialTerm(editing: Wallet?): Int {
    val start = editing?.startDate
    val end = editing?.endDate
    if (start == null || end == null) return 12
    val months = ((end - start).inWholeDays / 30.44).roundToInt()
    return listOf(3, 6, 12, 24).minByOrNull { kotlin.math.abs(it - months) } ?: 12
}

@Composable
private fun WalletTypeOption(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (selected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        tween(200), label = "walletTypeBg"
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
