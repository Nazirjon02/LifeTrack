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
import androidx.compose.material.icons.filled.*
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
import tj.mahram.lifetrack.ui.theme.ErrorColor
import tj.mahram.lifetrack.ui.theme.SuccessColor
import tj.mahram.lifetrack.ui.theme.TertiaryDark
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
    val s = LocalStrings.current
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
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Default.Refresh, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // ── Stat Grid ──────────────────────────────────────────────
                val today = Clock.System.now().toLocalDateTime(tz).date
                val habitsDone = state.habits.count { h ->
                    h.entries.any { it.completedAt.toLocalDateTime(tz).date == today }
                }
                DashStatGrid(state = state, habitsDone = habitsDone, today = today, tz = tz)

                Spacer(Modifier.height(4.dp))

                // ── Pomodoro Timer ────────────────────────────────────────
                PomodoroCard(
                    pomodoro = state.pomodoro,
                    onToggle = { onIntent(DashboardIntent.PomodoroToggle) },
                    onReset  = { onIntent(DashboardIntent.PomodoroReset) }
                )

                // ── Finance Month ─────────────────────────────────────────
                state.monthlyFinance?.let { finance ->
                    FinanceMonthCard(finance = finance, currency = state.currency)
                }

                // ── Today's Habits ────────────────────────────────────────
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
// STAT GRID  (2 × 2)
// ═══════════════════════════════════════════════════════════
@Composable
private fun DashStatGrid(
    state: DashboardState,
    habitsDone: Int,
    today: kotlinx.datetime.LocalDate,
    tz: TimeZone
) {
    val s = LocalStrings.current
    val balance = state.monthlyFinance?.balance
    val balancePositive = (balance ?: 0.0) >= 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientStatCard(
                modifier = Modifier.weight(1f),
                emoji = "✅",
                label = s.dashboardTasksToday,
                value = if (state.totalTasksToday == 0) "—"
                        else "${state.completedTasksToday}/${state.totalTasksToday}",
                gradient = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                valueColor = MaterialTheme.colorScheme.primary
            )
            GradientStatCard(
                modifier = Modifier.weight(1f),
                emoji = "🔥",
                label = s.dashboardHabitsDoneLabel,
                value = if (state.habits.isEmpty()) "—"
                        else "$habitsDone/${state.habits.size}",
                gradient = listOf(
                    SuccessColor.copy(alpha = 0.22f),
                    SuccessColor.copy(alpha = 0.06f)
                ),
                valueColor = SuccessColor
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientStatCard(
                modifier = Modifier.weight(1f),
                emoji = if (balancePositive) "📈" else "📉",
                label = s.dashboardBalance,
                value = balance?.formatCurrency(state.currency) ?: "—",
                gradient = if (balancePositive)
                    listOf(SuccessColor.copy(alpha = 0.18f), SuccessColor.copy(alpha = 0.05f))
                else
                    listOf(ErrorColor.copy(alpha = 0.18f), ErrorColor.copy(alpha = 0.05f)),
                valueColor = if (balancePositive) SuccessColor else ErrorColor
            )
            GradientStatCard(
                modifier = Modifier.weight(1f),
                emoji = "🍅",
                label = s.dashboardPomodorosLabel,
                value = if (state.pomodoro.completedPomodoros == 0) "—"
                        else "${state.pomodoro.completedPomodoros}",
                gradient = listOf(
                    TertiaryDark.copy(alpha = 0.22f),
                    TertiaryDark.copy(alpha = 0.06f)
                ),
                valueColor = TertiaryDark
            )
        }
    }
}

@Composable
private fun GradientStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String,
    gradient: List<Color>,
    valueColor: Color
) {
    Box(
        modifier = modifier
            .height(94.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(gradient))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(emoji, fontSize = 22.sp)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
    val workColor  = MaterialTheme.colorScheme.primary
    val breakColor = SuccessColor
    val timerColor = if (pomodoro.mode == PomodoroMode.WORK) workColor else breakColor

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue  = if (pomodoro.isRunning) 1.018f else 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )
    val animProgress by animateFloatAsState(
        targetValue = pomodoro.progressFraction,
        animationSpec = tween(300, easing = LinearEasing),
        label = "pomodoroProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (pomodoro.mode == PomodoroMode.WORK) s.dashboardFocusTime else s.dashboardBreakTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (pomodoro.completedPomodoros > 0)
                            s.dashboardSession(pomodoro.completedPomodoros)
                        else s.dashboardIdleMessage,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledIconButton(
                    onClick = onReset,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Ring
            Box(
                modifier = Modifier.scale(pulseScale).size(164.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sw = 11.dp.toPx()
                    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
                    val inset = sw / 2f
                    val arc = Size(size.width - inset * 2, size.height - inset * 2)
                    val tl = Offset(inset, inset)
                    drawArc(
                        color = timerColor.copy(alpha = 0.10f),
                        startAngle = -90f, sweepAngle = 360f,
                        useCenter = false, style = stroke, topLeft = tl, size = arc
                    )
                    if (animProgress > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(listOf(timerColor.copy(alpha = 0.6f), timerColor)),
                            startAngle = -90f, sweepAngle = 360f * animProgress,
                            useCenter = false, style = stroke, topLeft = tl, size = arc
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        pomodoro.timeString,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = timerColor
                    )
                    Text(
                        if (pomodoro.mode == PomodoroMode.WORK) s.dashboardModeWork else s.dashboardModeRest,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = timerColor.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Control button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (pomodoro.isRunning)
                            Brush.horizontalGradient(listOf(timerColor.copy(alpha = 0.22f), timerColor.copy(alpha = 0.12f)))
                        else
                            Brush.horizontalGradient(listOf(timerColor, timerColor.copy(alpha = 0.75f)))
                    )
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = pomodoro.isRunning,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                    label = "pomBtn"
                ) { running ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (running) timerColor else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            if (running) s.dashboardPause else s.dashboardStartFocus,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (running) timerColor else Color.White
                        )
                    }
                }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                s.dashboardFinanceMonth,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FinanceMiniStat(s.dashboardIncome,  finance.totalIncome,  true, currency)
                Box(Modifier.width(1.dp).height(38.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)))
                FinanceMiniStat(s.dashboardExpense, finance.totalExpense, false, currency)
                Box(Modifier.width(1.dp).height(38.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)))
                FinanceMiniStat(s.dashboardBalance, finance.balance, finance.balance >= 0, currency)
            }
        }
    }
}

@Composable
private fun FinanceMiniStat(label: String, amount: Double, isPositive: Boolean, currency: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            amount.formatCurrency(currency),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) SuccessColor else ErrorColor
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
    val tz    = TimeZone.currentSystemDefault()
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
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                s.dashboardHabitsDoneCount(doneCount, habits.size),
                style = MaterialTheme.typography.labelMedium,
                color = SuccessColor,
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
        targetValue = if (isDone) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chipScale_${habit.id}"
    )
    val bg by animateColorAsState(
        targetValue = if (isDone) habitColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(280), label = "chipBg_${habit.id}"
    )
    val border by animateColorAsState(
        targetValue = if (isDone) habitColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        animationSpec = tween(280), label = "chipBorder_${habit.id}"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .width(78.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
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

// ═══════════════════════════════════════════════════════════
// LEGACY EXPORTS (used by other screens)
// ═══════════════════════════════════════════════════════════
@Composable
fun DashCard(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DashboardCard(title: String, content: @Composable ColumnScope.() -> Unit) =
    DashCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), title = title, content = content)

@Composable
fun BalanceMini(label: String, amount: Double, isPositive: Boolean, currency: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(amount.formatCurrency(currency), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = if (isPositive) SuccessColor else ErrorColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
