package tj.mahram.lifetrack.feature.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlin.time.Clock
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.ReminderFrequency
import tj.mahram.lifetrack.domain.model.ReminderSchedule
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.FieldLabel
import tj.mahram.lifetrack.ui.components.GradientButton
import tj.mahram.lifetrack.ui.components.SheetHandle
import tj.mahram.lifetrack.ui.components.SheetHeader
import tj.mahram.lifetrack.ui.components.SheetTextField
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.glassSurface

class RemindersScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel<RemindersScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        RemindersContent(state = state, onIntent = screenModel::handleIntent, onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemindersContent(state: RemindersState, onIntent: (RemindersIntent) -> Unit, onBack: () -> Unit) {
    val s = LocalStrings.current
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                title = {
                    Column {
                        Text(s.remindersTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                        Text(s.remindersSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(brandHorizontalGradient()).clickable { onIntent(RemindersIntent.ShowAdd) },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Add, contentDescription = s.reminderAddButton, tint = Color.White, modifier = Modifier.size(28.dp)) }
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (state.reminders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(emoji = "🔔", title = s.reminderEmptyTitle, subtitle = s.reminderEmptySubtitle, actionLabel = s.reminderAddButton, onAction = { onIntent(RemindersIntent.ShowAdd) })
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()).verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp).padding(bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!state.hasPermission) {
                    Box(modifier = Modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(16.dp), glow = MaterialTheme.colorScheme.error).padding(14.dp)) {
                        Text("⚠️ ${s.labelEnableNotif}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                state.reminders.forEach { reminder ->
                    ReminderCard(reminder = reminder, onIntent = onIntent)
                }
            }
        }

        if (state.showSheet) {
            AddReminderSheet(editing = state.editing, onDismiss = { onIntent(RemindersIntent.HideSheet) }, onSave = { onIntent(RemindersIntent.Save(it)) })
        }
    }
}

@Composable
private fun ReminderCard(reminder: ReminderSchedule, onIntent: (RemindersIntent) -> Unit) {
    val s = LocalStrings.current
    val freqLabel = when (reminder.frequency) {
        ReminderFrequency.DAILY -> s.reminderFreqDaily
        ReminderFrequency.WEEKDAYS -> s.reminderFreqWeekdays
        ReminderFrequency.WEEKENDS -> s.reminderFreqWeekends
        ReminderFrequency.CUSTOM -> reminder.daysOfWeek.sorted().joinToString(" ") { s.plannerWeekDayHeaders[it - 1] }
    }
    Row(
        modifier = Modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(20.dp), glow = if (reminder.enabled) MaterialTheme.colorScheme.primary else null).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(reminder.timeLabel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = if (reminder.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Text(reminder.message.ifBlank { s.reminderDefaultMessage }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(freqLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Switch(
                checked = reminder.enabled,
                onCheckedChange = { onIntent(RemindersIntent.ToggleEnabled(reminder.id, it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
            )
            Row {
                IconButton(onClick = { onIntent(RemindersIntent.ShowEdit(reminder)) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = { onIntent(RemindersIntent.Delete(reminder.id)) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderSheet(editing: ReminderSchedule?, onDismiss: () -> Unit, onSave: (ReminderSchedule) -> Unit) {
    val s = LocalStrings.current
    var message by remember { mutableStateOf(editing?.message ?: "") }
    var hour by remember { mutableStateOf(editing?.hour ?: 9) }
    var minute by remember { mutableStateOf(editing?.minute ?: 0) }
    var frequency by remember { mutableStateOf(editing?.frequency ?: ReminderFrequency.DAILY) }
    var customDays by remember { mutableStateOf(editing?.daysOfWeek ?: setOf(1, 2, 3, 4, 5)) }
    var enabled by remember { mutableStateOf(editing?.enabled ?: true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { SheetHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp)) {
            SheetHeader(title = if (editing != null) s.reminderEditTitle else s.reminderNewTitle, subtitle = s.remindersSubtitle, onClose = onDismiss)
            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                FieldLabel(s.reminderMessageField)
                SheetTextField(value = message, onValueChange = { message = it }, placeholder = s.reminderMessagePlaceholder)

                FieldLabel(s.reminderTimeField)
                TimeSelector(hour, minute, onHour = { hour = it }, onMinute = { minute = it })

                FieldLabel(s.reminderFrequencyField)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FreqChip(s.reminderFreqDaily, frequency == ReminderFrequency.DAILY) { frequency = ReminderFrequency.DAILY }
                    FreqChip(s.reminderFreqWeekdays, frequency == ReminderFrequency.WEEKDAYS) { frequency = ReminderFrequency.WEEKDAYS }
                    FreqChip(s.reminderFreqWeekends, frequency == ReminderFrequency.WEEKENDS) { frequency = ReminderFrequency.WEEKENDS }
                    FreqChip(s.reminderFreqCustom, frequency == ReminderFrequency.CUSTOM) { frequency = ReminderFrequency.CUSTOM }
                }

                if (frequency == ReminderFrequency.CUSTOM) {
                    FieldLabel(s.reminderDaysField)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (iso in 1..7) {
                            val sel = iso in customDays
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f).clip(CircleShape)
                                    .then(if (sel) Modifier.background(brandHorizontalGradient()) else Modifier.glassSurface(shape = CircleShape))
                                    .clickable { customDays = if (sel) customDays - iso else customDays + iso },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(s.plannerWeekDayHeaders[iso - 1], style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(s.reminderEnabledLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Switch(checked = enabled, onCheckedChange = { enabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary))
                }
            }

            Spacer(Modifier.height(26.dp))
            GradientButton(
                text = s.reminderSaveButton,
                onClick = {
                    val id = editing?.id ?: Clock.System.now().toEpochMilliseconds().toString()
                    onSave(
                        ReminderSchedule(
                            id = id,
                            message = message.trim().ifBlank { s.reminderDefaultMessage },
                            hour = hour, minute = minute,
                            frequency = frequency,
                            daysOfWeek = if (frequency == ReminderFrequency.CUSTOM) customDays else emptySet(),
                            enabled = enabled
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                height = 56.dp
            )
        }
    }
}

@Composable
private fun TimeSelector(hour: Int, minute: Int, onHour: (Int) -> Unit, onMinute: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Stepper(value = hour.toString().padStart(2, '0'), onUp = { onHour((hour + 1) % 24) }, onDown = { onHour((hour + 23) % 24) })
        Text(":", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 12.dp))
        Stepper(value = minute.toString().padStart(2, '0'), onUp = { onMinute((minute + 5) % 60) }, onDown = { onMinute((minute + 55) % 60) })
    }
}

@Composable
private fun Stepper(value: String, onUp: () -> Unit, onDown: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onUp) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        Box(
            modifier = Modifier.clip(RoundedCornerShape(14.dp)).glassSurface(shape = RoundedCornerShape(14.dp)).padding(horizontal = 22.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = onDown) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun FreqChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
            .then(if (selected) Modifier.background(brandHorizontalGradient()) else Modifier.glassSurface(shape = RoundedCornerShape(12.dp)))
            .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}
