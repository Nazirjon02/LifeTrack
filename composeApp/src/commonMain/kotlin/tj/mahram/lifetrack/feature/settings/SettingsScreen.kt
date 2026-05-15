package tj.mahram.lifetrack.feature.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AppTheme
import tj.mahram.lifetrack.domain.model.SupportedCurrencies

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<SettingsScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        SettingsContent(state = state, onIntent = screenModel::handleIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    val s = LocalStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            s.settingsTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            s.settingsSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Appearance ──────────────────────────────────────────
            item { SectionLabel(s.sectionAppearance) }
            item {
                SettingsCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            s.labelTheme,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppTheme.entries.forEach { theme ->
                                val (emoji, themeLabel) = when (theme) {
                                    AppTheme.DARK   -> "🌙" to s.themeDark
                                    AppTheme.LIGHT  -> "☀️" to s.themeLight
                                    AppTheme.SYSTEM -> "💻" to s.themeSystem
                                }
                                EmojiSelectChip(
                                    emoji = emoji,
                                    label = themeLabel,
                                    isSelected = state.settings.theme == theme,
                                    onClick = { onIntent(SettingsIntent.SetTheme(theme)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Language ─────────────────────────────────────────────
            item { SectionLabel(s.sectionLanguage) }
            item {
                SettingsCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            s.labelAppLanguage,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LanguageCard(
                                flag = "🇺🇸",
                                name = "English",
                                isSelected = state.settings.language == AppLanguage.ENGLISH,
                                onClick = { onIntent(SettingsIntent.SetLanguage(AppLanguage.ENGLISH)) },
                                modifier = Modifier.weight(1f)
                            )
                            LanguageCard(
                                flag = "🇷🇺",
                                name = "Русский",
                                isSelected = state.settings.language == AppLanguage.RUSSIAN,
                                onClick = { onIntent(SettingsIntent.SetLanguage(AppLanguage.RUSSIAN)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── Finance ───────────────────────────────────────────────
            item { SectionLabel(s.sectionFinance) }
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                s.labelBaseCurrency,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                s.labelCurrencySubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = state.settings.currency,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .width(120.dp),
                                textStyle = MaterialTheme.typography.bodySmall,
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                SupportedCurrencies.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency) },
                                        onClick = {
                                            onIntent(SettingsIntent.SetCurrency(currency))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Notifications ─────────────────────────────────────────
            item { SectionLabel(s.sectionNotifications) }
            item {
                SettingsCard {
                    Column {
                        NotifRow(
                            title = s.labelEnableNotif,
                            subtitle = s.labelEnableNotifSub,
                            checked = state.settings.notificationsEnabled,
                            onToggle = { onIntent(SettingsIntent.SetNotifications(it)) },
                            showDivider = true
                        )
                        NotifRow(
                            title = s.labelTaskReminders,
                            subtitle = s.labelTaskRemindersSub,
                            checked = state.settings.taskNotificationsEnabled,
                            onToggle = { onIntent(SettingsIntent.SetTaskNotifications(it)) },
                            showDivider = true
                        )
                        NotifRow(
                            title = s.labelFinanceAlerts,
                            subtitle = s.labelFinanceAlertsSub,
                            checked = state.settings.financeNotificationsEnabled,
                            onToggle = { onIntent(SettingsIntent.SetFinanceNotifications(it)) },
                            showDivider = false
                        )
                    }
                }
            }

            // ── About ─────────────────────────────────────────────────
            item { SectionLabel(s.sectionAbout) }
            item {
                SettingsCard {
                    ListItem(
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚡", style = MaterialTheme.typography.titleLarge)
                            }
                        },
                        headlineContent = {
                            Text("LifeTrack", fontWeight = FontWeight.SemiBold)
                        },
                        supportingContent = {
                            Text(
                                s.aboutVersion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }
}

// ── Private helpers ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun LanguageCard(
    flag: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(250),
        label = "langBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        animationSpec = tween(250),
        label = "langBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(250),
        label = "langText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(flag, style = MaterialTheme.typography.headlineMedium)
            Text(
                name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                    )
            )
        }
    }
}

@Composable
private fun EmojiSelectChip(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(250),
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        animationSpec = tween(250),
        label = "chipBorder"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, style = MaterialTheme.typography.bodyLarge)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotifRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    showDivider: Boolean
) {
    ListItem(
        headlineContent = {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}

// Legacy helpers
@Composable
fun SettingsHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsRow(title: String, content: @Composable () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = content,
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
fun SwitchRow(title: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
