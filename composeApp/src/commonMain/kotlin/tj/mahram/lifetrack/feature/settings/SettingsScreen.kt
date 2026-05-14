package tj.mahram.lifetrack.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 16.dp
            )
        ) {
            item { SettingsHeader("Appearance") }
            item {
                SettingsRow(title = "Theme") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppTheme.entries.forEach { theme ->
                            FilterChip(
                                selected = state.settings.theme == theme,
                                onClick = { onIntent(SettingsIntent.SetTheme(theme)) },
                                label = { Text(theme.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            item { SettingsHeader("Finance") }
            item {
                SettingsRow(title = "Base Currency") {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = state.settings.currency,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().width(120.dp),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

            item { SettingsHeader("Language") }
            item {
                SettingsRow(title = "App Language") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = state.settings.language == lang,
                                onClick = { onIntent(SettingsIntent.SetLanguage(lang)) },
                                label = { Text(lang.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            item { SettingsHeader("Notifications") }
            item {
                SwitchRow("Enable Notifications", state.settings.notificationsEnabled) {
                    onIntent(SettingsIntent.SetNotifications(it))
                }
            }
            item {
                SwitchRow("Task Reminders", state.settings.taskNotificationsEnabled) {
                    onIntent(SettingsIntent.SetTaskNotifications(it))
                }
            }
            item {
                SwitchRow("Finance Alerts", state.settings.financeNotificationsEnabled) {
                    onIntent(SettingsIntent.SetFinanceNotifications(it))
                }
            }
            item {
                SwitchRow("Crypto Price Alerts", state.settings.cryptoNotificationsEnabled) {
                    onIntent(SettingsIntent.SetCryptoNotifications(it))
                }
            }

            item { SettingsHeader("About") }
            item {
                ListItem(
                    headlineContent = { Text("LifeTrack") },
                    supportingContent = { Text("Version 1.0.0 • Kotlin Multiplatform") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }
    }
}

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
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = MaterialTheme.colorScheme.primary)
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
