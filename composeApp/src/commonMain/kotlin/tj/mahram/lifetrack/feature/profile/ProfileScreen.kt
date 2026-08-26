package tj.mahram.lifetrack.feature.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AppTheme
import tj.mahram.lifetrack.domain.model.SupportedCurrencies
import tj.mahram.lifetrack.ui.components.GradientButton
import tj.mahram.lifetrack.ui.components.brandVividGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.theme.appColors

private val AvatarEmojis = listOf(
    "🚀", "🧑‍🚀", "🦊", "🐼", "🐯", "🦁", "🐧", "🐨",
    "🦄", "🔥", "⭐", "🌙", "🎯", "💎", "🧠", "⚡", "🌈", "👑"
)

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<ProfileScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        ProfileContent(state = state, onIntent = screenModel::handleIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(state: ProfileState, onIntent: (ProfileIntent) -> Unit) {
    val s = LocalStrings.current
    var editing by remember { mutableStateOf(false) }

    Scaffold(containerColor = Color.Transparent) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Hero ───────────────────────────────────────────────────────
            item(key = "hero") {
                ProfileHero(state = state, onEdit = { editing = true })
            }

            // ── Stats grid ─────────────────────────────────────────────────
            item(key = "stats") {
                ProfileStats(state = state)
            }

            // ── Settings header ────────────────────────────────────────────
            item(key = "settings_header") {
                Spacer(Modifier.height(2.dp))
                Text(
                    s.profileSettingsHeader,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
                )
            }

            // ── Appearance ─────────────────────────────────────────────────
            item(key = "sec_appearance") {
                SettingsSectionHeader(label = s.sectionAppearance, icon = Icons.Default.Palette)
            }
            item(key = "theme") {
                SettingsCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RowLabel(label = s.labelTheme, icon = Icons.Default.BrightnessMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppTheme.entries.forEach { theme ->
                                val (emoji, label) = when (theme) {
                                    AppTheme.DARK -> "🌙" to s.themeDark
                                    AppTheme.LIGHT -> "☀️" to s.themeLight
                                    AppTheme.SYSTEM -> "💻" to s.themeSystem
                                }
                                EmojiSelectChip(
                                    emoji = emoji,
                                    label = label,
                                    isSelected = state.settings.theme == theme,
                                    onClick = { onIntent(ProfileIntent.SetTheme(theme)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Language ───────────────────────────────────────────────────
            item(key = "sec_lang") {
                SettingsSectionHeader(label = s.sectionLanguage, icon = Icons.Default.Language)
            }
            item(key = "language") {
                SettingsCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RowLabel(label = s.labelAppLanguage, icon = Icons.Default.Translate)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LanguageCard(
                                flag = "🇺🇸",
                                name = "English",
                                isSelected = state.settings.language == AppLanguage.ENGLISH,
                                onClick = { onIntent(ProfileIntent.SetLanguage(AppLanguage.ENGLISH)) },
                                modifier = Modifier.weight(1f)
                            )
                            LanguageCard(
                                flag = "🇷🇺",
                                name = "Русский",
                                isSelected = state.settings.language == AppLanguage.RUSSIAN,
                                onClick = { onIntent(ProfileIntent.SetLanguage(AppLanguage.RUSSIAN)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── Finance ────────────────────────────────────────────────────
            item(key = "sec_finance") {
                SettingsSectionHeader(label = s.sectionFinance, icon = Icons.Default.AccountBalance)
            }
            item(key = "currency") {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconTile(icon = Icons.Default.AttachMoney, tint = MaterialTheme.colorScheme.primary)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(s.labelBaseCurrency, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    s.labelCurrencySubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = state.settings.currency,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                                    .width(110.dp),
                                textStyle = MaterialTheme.typography.bodySmall,
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                SupportedCurrencies.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency) },
                                        onClick = {
                                            onIntent(ProfileIntent.SetCurrency(currency))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Notifications ──────────────────────────────────────────────
            item(key = "sec_notif") {
                SettingsSectionHeader(label = s.sectionNotifications, icon = Icons.Default.Notifications)
            }
            item(key = "notifications") {
                SettingsCard {
                    Column {
                        NotifRow(
                            title = s.labelEnableNotif,
                            subtitle = s.labelEnableNotifSub,
                            icon = Icons.Default.NotificationsActive,
                            checked = state.settings.notificationsEnabled,
                            onToggle = { onIntent(ProfileIntent.SetNotifications(it)) },
                            showDivider = true
                        )
                        NotifRow(
                            title = s.labelTaskReminders,
                            subtitle = s.labelTaskRemindersSub,
                            icon = Icons.Default.CheckCircle,
                            checked = state.settings.taskNotificationsEnabled,
                            onToggle = { onIntent(ProfileIntent.SetTaskNotifications(it)) },
                            showDivider = true
                        )
                        NotifRow(
                            title = s.labelFinanceAlerts,
                            subtitle = s.labelFinanceAlertsSub,
                            icon = Icons.Default.AccountBalance,
                            checked = state.settings.financeNotificationsEnabled,
                            onToggle = { onIntent(ProfileIntent.SetFinanceNotifications(it)) },
                            showDivider = false
                        )
                    }
                }
            }

            // ── About ──────────────────────────────────────────────────────
            item(key = "sec_about") {
                SettingsSectionHeader(label = s.sectionAbout, icon = Icons.Default.Info)
            }
            item(key = "about") {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(brandVividGradient()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 22.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("LifeTrack", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                s.aboutVersion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                s.settingsVersionLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    if (editing) {
        EditProfileDialog(
            initialName = state.settings.displayName,
            initialAvatar = state.settings.avatarEmoji,
            onDismiss = { editing = false },
            onSave = { name, avatar ->
                onIntent(ProfileIntent.SetDisplayName(name))
                onIntent(ProfileIntent.SetAvatar(avatar))
                editing = false
            }
        )
    }
}

// ─── Hero ───────────────────────────────────────────────────────────────────

@Composable
private fun ProfileHero(state: ProfileState, onEdit: () -> Unit) {
    val s = LocalStrings.current
    val name = state.settings.displayName.ifBlank { s.profileDefaultName }
    val rank = s.profileRankTitles[(state.level - 1).coerceIn(0, s.profileRankTitles.lastIndex)]
    val progress by animateFloatAsState(
        targetValue = state.levelProgress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "levelProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(brandVividGradient())
            .drawBehind {
                // decorative depth rings
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = size.minDimension * 0.55f,
                    center = Offset(size.width * 0.92f, size.height * 0.08f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = size.minDimension * 0.40f,
                    center = Offset(size.width * 0.08f, size.height * 0.95f)
                )
            }
            .padding(22.dp)
    ) {
        // Edit button top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.16f))
                .clickable(onClick = onEdit),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Edit, contentDescription = s.profileEditAction, tint = Color.White, modifier = Modifier.size(18.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar with edit badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(2.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                            .clickable(onClick = onEdit),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.settings.avatarEmoji, fontSize = 42.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = onEdit),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        HeroChip(text = "${s.profileLevelLabel(state.level)} · $rank")
                        if (state.bestStreak > 0) HeroChip(text = "🔥 ${state.bestStreak}")
                    }
                }
            }

            // Level / score progress
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "${state.productivityScore}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            s.profileScoreLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        s.profileToNextLevel(state.pointsToNextLevel),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                // progress track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0.02f, 1f))
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.White, Color.White.copy(alpha = 0.75f))
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Stats grid ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileStats(state: ProfileState) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    val currency = state.balance?.currency ?: state.settings.currency

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.Default.AccountBalanceWallet,
                accent = c.success,
                value = (state.balance?.currentBalance ?: 0.0).formatCurrency(currency),
                label = s.profileStatBalance,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Default.CheckCircle,
                accent = MaterialTheme.colorScheme.primary,
                value = "${state.tasksDone}",
                label = s.profileStatTasks,
                sub = if (state.tasksTotal > 0) "of ${state.tasksTotal}" else null,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.Default.LocalFireDepartment,
                accent = c.warning,
                value = "${state.habitsCount}",
                label = s.profileStatHabits,
                sub = if (state.bestStreak > 0) "🔥 ${state.bestStreak}" else null,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Default.TrackChanges,
                accent = c.info,
                value = "${state.goalsAchieved}",
                label = s.profileStatGoals,
                sub = if (state.goalsTotal > 0) "of ${state.goalsTotal}" else null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    accent: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    sub: String? = null
) {
    Column(
        modifier = modifier
            .glassSurface(shape = RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (sub != null) {
                    Text(
                        sub,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─── Edit profile dialog ──────────────────────────────────────────────────────

@Composable
private fun EditProfileDialog(
    initialName: String,
    initialAvatar: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf(initialName) }
    var avatar by remember { mutableStateOf(initialAvatar) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassSurface(shape = RoundedCornerShape(26.dp), elevated = true)
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(brandVividGradient()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(avatar, fontSize = 22.sp)
                }
                Text(s.profileEditTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            // name field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    s.profileNameLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(s.profileNameHint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // avatar picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    s.profilePickAvatar,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 6-per-row emoji grid without nested scrolling
                val rows = AvatarEmojis.chunked(6)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { emoji ->
                                AvatarChip(
                                    emoji = emoji,
                                    isSelected = emoji == avatar,
                                    onClick = { avatar = emoji },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(s.cancelButton)
                }
                GradientButton(
                    text = s.saveButton,
                    onClick = { onSave(name, avatar) },
                    modifier = Modifier.weight(1f),
                    height = 48.dp
                )
            }
        }
    }
}

@Composable
private fun AvatarChip(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "avatarBg"
    )
    val border by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        animationSpec = tween(200),
        label = "avatarBorder"
    )
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(if (isSelected) 1.5.dp else 1.dp, border, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 22.sp)
    }
}

// ─── Shared settings building blocks ──────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(label: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(20.dp))) {
        content()
    }
}

@Composable
private fun RowLabel(label: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IconTile(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
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
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "langBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        animationSpec = tween(250),
        label = "langBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(250),
        label = "langText"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
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
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(250),
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        animationSpec = tween(250),
        label = "chipBorder"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, style = MaterialTheme.typography.bodyLarge)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotifRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    showDivider: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}
