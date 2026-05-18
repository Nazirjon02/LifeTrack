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
import androidx.compose.material.icons.filled.Delete
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
import tj.mahram.lifetrack.ui.theme.SuccessColor

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
                            fontWeight = FontWeight.Bold
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(HabitsIntent.ShowAddSheet) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = s.habitsTitle, modifier = Modifier.size(26.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.habits.isEmpty()) {
            HabitsEmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "summary") {
                    HabitsSummaryBanner(state = state)
                }
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

// ─── Summary Banner ────────────────────────────────────────────────────────────

@Composable
private fun HabitsSummaryBanner(state: HabitsState) {
    val s = LocalStrings.current
    val completionRate = if (state.habits.isEmpty()) 0f
                        else state.completedTodayCount.toFloat() / state.habits.size

    val animRate by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(1100, easing = FastOutSlowInEasing),
        label = "completionRate"
    )

    val bestStreak = state.streaks.values.maxOrNull() ?: 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    when {
                        completionRate >= 1f   -> s.habitsCompletionPerfect
                        completionRate >= 0.5f -> s.habitsCompletionGreat
                        completionRate > 0f    -> s.habitsCompletionKeepGoing
                        else                   -> s.habitsCompletionStart
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    s.habitsSubtitle(state.completedTodayCount, state.habits.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                if (bestStreak > 0) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.18f))
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
            }

            Spacer(Modifier.width(16.dp))

            // Progress ring
            Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sw = 8.dp.toPx()
                    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
                    val inset = sw / 2f
                    val arc = Size(size.width - inset * 2, size.height - inset * 2)
                    val tl = Offset(inset, inset)
                    drawArc(
                        color = Color.White.copy(alpha = 0.20f),
                        startAngle = -90f, sweepAngle = 360f,
                        useCenter = false, style = stroke, topLeft = tl, size = arc
                    )
                    if (animRate > 0f) {
                        drawArc(
                            color = Color.White,
                            startAngle = -90f, sweepAngle = 360f * animRate,
                            useCenter = false, style = stroke, topLeft = tl, size = arc
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(animRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        s.plannerDoneLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

// ─── Habit Card ───────────────────────────────────────────────────────────────

@Composable
private fun HabitCard(
    habit: Habit,
    streak: Int,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val tz = TimeZone.currentSystemDefault()
    val today = kotlin.time.Clock.System.now().toLocalDateTime(tz).date
    val isDone = habit.entries.any { it.completedAt.toLocalDateTime(tz).date == today }
    val habitColor = parseHabitColor(habit.color)

    val checkScale by animateFloatAsState(
        targetValue = if (isDone) 1f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkScale_${habit.id}"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) habitColor.copy(alpha = 0.10f)
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(habitColor, habitColor.copy(alpha = 0.4f))
                        )
                    )
            )
            Column(modifier = Modifier.padding(start = 14.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(habitColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(habit.icon, fontSize = 22.sp)
                    }

                    // Name + info
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            habit.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        if (streak > 0) {
                            Text(
                                s.habitsDayStreak(streak),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (streak >= 7) Color(0xFFFF8C00)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                s.habitFrequencyLabel(habit.frequency),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Delete
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Check button
                    Box(
                        modifier = Modifier
                            .scale(checkScale)
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDone) Brush.radialGradient(listOf(habitColor, habitColor.copy(alpha = 0.7f)))
                                else Brush.radialGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                            )
                            .clickable(onClick = onToggle),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Text("✓", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 7-day heatmap
                MiniHeatmap(entries = habit.entries, color = habitColor)
            }
        }
    }
}

// ─── 7-day heatmap ────────────────────────────────────────────────────────────

@Composable
private fun MiniHeatmap(entries: List<HabitEntry>, color: Color) {
    val tz = TimeZone.currentSystemDefault()
    val today = kotlin.time.Clock.System.now().toLocalDateTime(tz).date
    val completed = entries.map { it.completedAt.toLocalDateTime(tz).date }.toSet()

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 6 downTo 0) {
            val day = today.minus(i, DateTimeUnit.DAY)
            val isDone = day in completed
            val isToday = day == today

            val bg by animateColorAsState(
                targetValue = when {
                    isDone  -> color.copy(alpha = 0.85f)
                    isToday -> color.copy(alpha = 0.18f)
                    else    -> MaterialTheme.colorScheme.surfaceVariant
                },
                animationSpec = tween(300),
                label = "hm_$i"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    day.dayOfWeek.name.take(1),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .then(
                            if (isToday) Modifier.border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            else Modifier
                        )
                )
            }
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun HabitsEmptyState(modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌱", fontSize = 72.sp)
        Spacer(Modifier.height(20.dp))
        Text(
            s.habitsEmptyTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            s.habitsEmptySubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

fun parseHabitColor(hex: String): Color = runCatching {
    val clean = hex.trimStart('#')
    val long = clean.toLong(16)
    Color(0xFF000000L or long)
}.getOrDefault(Color(0xFF7C3AED))
