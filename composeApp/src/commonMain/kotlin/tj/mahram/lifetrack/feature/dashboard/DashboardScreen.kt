package tj.mahram.lifetrack.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.MonthNames
import tj.mahram.lifetrack.core.util.formatCurrency
import tj.mahram.lifetrack.domain.model.Habit
import tj.mahram.lifetrack.feature.habits.parseHabitColor
import tj.mahram.lifetrack.ui.components.GlassCard
import tj.mahram.lifetrack.ui.components.brandGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.theme.appColors
import kotlin.time.Clock

class DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<DashboardScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        DashboardContent(
            state = state,
            onRefresh = { screenModel.handleIntent(DashboardIntent.Refresh) },
            onIntent = screenModel::handleIntent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardState,
    onRefresh: () -> Unit,
    onIntent: (DashboardIntent) -> Unit
) {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz)
    val dayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val dateStr = "$dayName, ${now.day} ${MonthNames[now.month.number - 1]}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "${state.greeting} 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(42.dp)
                            .glassSurface(shape = CircleShape)
                            .clickable(onClick = onRefresh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Refresh, contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val today = now.date
                val habitsDone = state.habits.count { h ->
                    h.entries.any { it.completedAt.toLocalDateTime(tz).date == today }
                }
                DashStatGrid(state = state, habitsDone = habitsDone)

                PomodoroCard(
                    pomodoro = state.pomodoro,
                    onToggle = { onIntent(DashboardIntent.PomodoroToggle) },
                    onReset = { onIntent(DashboardIntent.PomodoroReset) }
                )

                state.monthlyFinance?.let { finance ->
                    FinanceMonthCard(finance = finance, currency = state.currency)
                }

                if (state.habits.isNotEmpty()) {
                    HabitsQuickWidget(
                        habits = state.habits,
                        habitStreaks = state.habitStreaks,
                        onToggle = { id -> onIntent(DashboardIntent.ToggleHabit(id)) }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// STAT GRID  (2 × 2 glass tiles)
// ═══════════════════════════════════════════════════════════
@Composable
private fun DashStatGrid(state: DashboardState, habitsDone: Int) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    val balance = state.monthlyFinance?.balance
    val balancePositive = (balance ?: 0.0) >= 0

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.TaskAlt,
                label = s.dashboardTasksToday,
                value = if (state.totalTasksToday == 0) "—" else "${state.completedTasksToday}/${state.totalTasksToday}",
                accent = MaterialTheme.colorScheme.primary
            )
            StatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.LocalFireDepartment,
                label = s.dashboardHabitsDoneLabel,
                value = if (state.habits.isEmpty()) "—" else "$habitsDone/${state.habits.size}",
                accent = c.success
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                modifier = Modifier.weight(1f),
                icon = if (balancePositive) Icons.AutoMirrored.Outlined.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
                label = s.dashboardBalance,
                value = balance?.formatCurrency(state.currency) ?: "—",
                accent = if (balancePositive) c.success else c.danger
            )
            StatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Timer,
                label = s.dashboardPomodorosLabel,
                value = if (state.pomodoro.completedPomodoros == 0) "—" else "${state.pomodoro.completedPomodoros}",
                accent = c.info
            )
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color
) {
    GlassCard(
        modifier = modifier.height(116.dp),
        shape = RoundedCornerShape(24.dp),
        glow = accent,
        contentPadding = PaddingValues(16.dp)
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═══════════════════════════════════════════════════════════
// POMODORO CARD
// ═══════════════════════════════════════════════════════════
@Composable
private fun PomodoroCard(
    pomodoro: PomodoroState,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    val timerColor = if (pomodoro.mode == PomodoroMode.WORK) MaterialTheme.colorScheme.primary else c.success
    val brandBrush = brandGradient()

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (pomodoro.isRunning) 1.02f else 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )
    val animProgress by animateFloatAsState(
        targetValue = pomodoro.progressFraction,
        animationSpec = tween(300, easing = LinearEasing),
        label = "pomodoroProgress"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        glow = timerColor,
        contentPadding = PaddingValues(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (pomodoro.mode == PomodoroMode.WORK) s.dashboardFocusTime else s.dashboardBreakTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (pomodoro.completedPomodoros > 0) s.dashboardSession(pomodoro.completedPomodoros)
                    else s.dashboardIdleMessage,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier.size(40.dp).glassSurface(shape = CircleShape).clickable(onClick = onReset),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(22.dp))

        Box(
            modifier = Modifier.fillMaxWidth().scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(170.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sw = 12.dp.toPx()
                    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
                    val inset = sw / 2f
                    val arc = Size(size.width - inset * 2, size.height - inset * 2)
                    val tl = Offset(inset, inset)
                    drawArc(
                        color = timerColor.copy(alpha = 0.12f),
                        startAngle = -90f, sweepAngle = 360f,
                        useCenter = false, style = stroke, topLeft = tl, size = arc
                    )
                    if (animProgress > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(listOf(timerColor.copy(alpha = 0.5f), timerColor)),
                            startAngle = -90f, sweepAngle = 360f * animProgress,
                            useCenter = false, style = stroke, topLeft = tl, size = arc
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        pomodoro.timeString,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (pomodoro.mode == PomodoroMode.WORK) s.dashboardModeWork else s.dashboardModeRest,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = timerColor
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .then(
                    if (pomodoro.isRunning)
                        Modifier.glassSurface(shape = RoundedCornerShape(18.dp))
                    else
                        Modifier.background(brandBrush, RoundedCornerShape(18.dp))
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    if (pomodoro.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (pomodoro.isRunning) MaterialTheme.colorScheme.onSurface else Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    if (pomodoro.isRunning) s.dashboardPause else s.dashboardStartFocus,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (pomodoro.isRunning) MaterialTheme.colorScheme.onSurface else Color.White
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FINANCE MONTH CARD
// ═══════════════════════════════════════════════════════════
@Composable
private fun FinanceMonthCard(
    finance: tj.mahram.lifetrack.domain.model.FinanceSummary,
    currency: String
) {
    val s = LocalStrings.current
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(20.dp)
    ) {
        Text(
            s.dashboardFinanceMonth,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FinanceMiniStat(s.dashboardIncome, finance.totalIncome, true, currency)
            Divider34()
            FinanceMiniStat(s.dashboardExpense, finance.totalExpense, false, currency)
            Divider34()
            FinanceMiniStat(s.dashboardBalance, finance.balance, finance.balance >= 0, currency)
        }
    }
}

@Composable
private fun Divider34() {
    Box(Modifier.width(1.dp).height(36.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))
}

@Composable
private fun FinanceMiniStat(label: String, amount: Double, isPositive: Boolean, currency: String) {
    val c = MaterialTheme.appColors
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            amount.formatCurrency(currency),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) c.success else c.danger
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ═══════════════════════════════════════════════════════════
// HABITS QUICK WIDGET
// ═══════════════════════════════════════════════════════════
@Composable
private fun HabitsQuickWidget(
    habits: List<Habit>,
    habitStreaks: Map<String, Int>,
    onToggle: (String) -> Unit
) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    val doneCount = habits.count { h -> h.entries.any { it.completedAt.toLocalDateTime(tz).date == today } }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                s.dashboardTodaysHabits,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                s.dashboardHabitsDoneCount(doneCount, habits.size),
                style = MaterialTheme.typography.labelMedium,
                color = c.success,
                fontWeight = FontWeight.SemiBold
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(habits, key = { it.id }) { habit ->
                val isDone = habit.entries.any { it.completedAt.toLocalDateTime(tz).date == today }
                val streak = habitStreaks[habit.id] ?: 0
                HabitQuickChip(
                    habit = habit,
                    isDone = isDone,
                    streak = streak,
                    habitColor = parseHabitColor(habit.color),
                    onToggle = { onToggle(habit.id) }
                )
            }
        }
    }
}

@Composable
private fun HabitQuickChip(
    habit: Habit,
    isDone: Boolean,
    streak: Int,
    habitColor: Color,
    onToggle: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isDone) 1f else 0.96f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chipScale_${habit.id}"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .width(82.dp)
            .glassSurface(
                shape = RoundedCornerShape(20.dp),
                glow = if (isDone) habitColor else null
            )
            .clickable(onClick = onToggle)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(habit.icon, fontSize = 26.sp)
        Text(
            habit.name,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        AnimatedContent(
            targetState = isDone,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "chipStatus_${habit.id}"
        ) { done ->
            if (done) {
                Box(
                    modifier = Modifier.size(18.dp).clip(CircleShape).background(habitColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else if (streak > 0) {
                Text("🔥$streak", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Box(Modifier.height(18.dp))
            }
        }
    }
}
