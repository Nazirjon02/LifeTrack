package tj.mahram.lifetrack.feature.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.Habit
import tj.mahram.lifetrack.domain.model.HabitEntry
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.brandVividGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.theme.appColors

class HabitsScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<HabitsScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        HabitsContent(state = state, onIntent = screenModel::handleIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsContent(state: HabitsState, onIntent: (HabitsIntent) -> Unit) {
    val s = LocalStrings.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            s.habitsTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (state.habits.isNotEmpty()) {
                            Text(
                                s.habitsSubtitle(state.completedTodayCount, state.habits.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(brandHorizontalGradient())
                    .clickable { onIntent(HabitsIntent.ShowAddSheet) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = s.habitsTitle, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.habits.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    emoji = "🌱",
                    title = s.habitsEmptyTitle,
                    subtitle = s.habitsEmptySubtitle,
                    actionLabel = s.habitsTitle,
                    onAction = { onIntent(HabitsIntent.ShowAddSheet) }
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 96.dp, start = 16.dp, end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "summary") { HabitsSummaryBanner(state = state) }
                items(state.habits, key = { it.id }) { habit ->
                    val streak = state.streaks[habit.id] ?: 0
                    HabitCard(
                        habit = habit,
                        streak = streak,
                        onToggle = { onIntent(HabitsIntent.ToggleHabit(habit.id)) },
                        onDelete = { onIntent(HabitsIntent.DeleteHabit(habit.id)) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        if (state.showAddSheet) {
            AddHabitSheet(
                onDismiss = { onIntent(HabitsIntent.HideAddSheet) },
                onConfirm = onIntent
            )
        }
    }
}

// ─── Summary Banner (brand-gradient hero) ───────────────────────────────────────
@Composable
private fun HabitsSummaryBanner(state: HabitsState) {
    val s = LocalStrings.current
    val completionRate = if (state.habits.isEmpty()) 0f else state.completedTodayCount.toFloat() / state.habits.size
    val animRate by animateFloatAsState(targetValue = completionRate, animationSpec = tween(1100, easing = FastOutSlowInEasing), label = "completionRate")
    val bestStreak = state.streaks.values.maxOrNull() ?: 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(brandVividGradient())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    when {
                        completionRate >= 1f -> s.habitsCompletionPerfect
                        completionRate >= 0.5f -> s.habitsCompletionGreat
                        completionRate > 0f -> s.habitsCompletionKeepGoing
                        else -> s.habitsCompletionStart
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    s.habitsSubtitle(state.completedTodayCount, state.habits.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                if (bestStreak > 0) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            s.habitsBestStreak(bestStreak),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Box(modifier = Modifier.size(86.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sw = 8.dp.toPx()
                    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
                    val inset = sw / 2f
                    val arc = Size(size.width - inset * 2, size.height - inset * 2)
                    val tl = Offset(inset, inset)
                    drawArc(color = Color.White.copy(alpha = 0.25f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke, topLeft = tl, size = arc)
                    if (animRate > 0f) {
                        drawArc(color = Color.White, startAngle = -90f, sweepAngle = 360f * animRate, useCenter = false, style = stroke, topLeft = tl, size = arc)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(animRate * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(s.plannerDoneLabel, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// ─── Habit Card ─────────────────────────────────────────────────────────────────
@Composable
private fun HabitCard(
    habit: Habit,
    streak: Int,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    val tz = TimeZone.currentSystemDefault()
    val today = kotlin.time.Clock.System.now().toLocalDateTime(tz).date
    val isDone = habit.entries.any { it.completedAt.toLocalDateTime(tz).date == today }
    val habitColor = parseHabitColor(habit.color)

    val checkScale by animateFloatAsState(
        targetValue = if (isDone) 1f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkScale_${habit.id}"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(22.dp), glow = if (isDone) habitColor else null)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(habitColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(habit.icon, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurface
                )
                if (streak > 0) {
                    Text(
                        s.habitsDayStreak(streak),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (streak >= 7) c.warning else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        s.habitFrequencyLabel(habit.frequency),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }
            Box(
                modifier = Modifier
                    .scale(checkScale)
                    .size(46.dp)
                    .clip(CircleShape)
                    .then(
                        if (isDone) Modifier.background(Brush.radialGradient(listOf(habitColor, habitColor.copy(alpha = 0.7f))))
                        else Modifier.glassSurface(shape = CircleShape)
                    )
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) Text("✓", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(Modifier.height(14.dp))
        MiniHeatmap(entries = habit.entries, color = habitColor)
    }
}

// ─── 7-day heatmap ────────────────────────────────────────────────────────────
@Composable
private fun MiniHeatmap(entries: List<HabitEntry>, color: Color) {
    val tz = TimeZone.currentSystemDefault()
    val today = kotlin.time.Clock.System.now().toLocalDateTime(tz).date
    val completed = entries.map { it.completedAt.toLocalDateTime(tz).date }.toSet()

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
        for (i in 6 downTo 0) {
            val day = today.minus(i, DateTimeUnit.DAY)
            val isDone = day in completed
            val isToday = day == today
            val bg by animateColorAsState(
                targetValue = when {
                    isDone -> color.copy(alpha = 0.9f)
                    isToday -> color.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                },
                animationSpec = tween(300), label = "hm_$i"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(
                    day.dayOfWeek.name.take(1),
                    style = MaterialTheme.typography.labelSmall, fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Box(
                    modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(7.dp)).background(bg)
                        .then(if (isToday) Modifier.border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(7.dp)) else Modifier)
                )
            }
        }
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────
fun parseHabitColor(hex: String): Color = runCatching {
    val clean = hex.trimStart('#')
    val long = clean.toLong(16)
    Color(0xFF000000L or long)
}.getOrDefault(Color(0xFF7C3AED))
