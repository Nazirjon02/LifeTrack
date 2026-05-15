package tj.mahram.lifetrack.feature.planner

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.Task
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.TaskCard
import tj.mahram.lifetrack.ui.components.TaskCardSkeleton
import tj.mahram.lifetrack.ui.components.color
import tj.mahram.lifetrack.ui.theme.SuccessColor

class PlannerScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<PlannerScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        PlannerContent(state = state, onIntent = screenModel::handleIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerContent(state: PlannerState, onIntent: (PlannerIntent) -> Unit) {
    val s = LocalStrings.current
    var showSearch  by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = s.tabPlanner,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            showSearch = !showSearch
                            if (!showSearch) onIntent(PlannerIntent.Search(""))
                        }) {
                            Icon(
                                imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (showSearch) "Close search" else "Search"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                AnimatedVisibility(
                    visible = showSearch,
                    enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit  = shrinkVertically(animationSpec = tween(200)) + fadeOut()
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onIntent(PlannerIntent.Search(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        placeholder = { Text(s.plannerSearchHint) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onIntent(PlannerIntent.ShowAddTask) },
                icon    = { Icon(Icons.Default.Add, contentDescription = null) },
                text    = { Text(s.plannerFabNewTask, fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (state.isLoading) {
            LazyColumn(contentPadding = padding) {
                items(5) { TaskCardSkeleton() }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top    = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 96.dp
                )
            ) {

                // ── PROGRESS HEADER ─────────────────────────────────────
                item(key = "header") {
                    PlannerProgressHeader(state = state)
                }

                // ── PRIORITY BREAKDOWN BAR CHART ────────────────────────
                if (state.pendingTasks.isNotEmpty()) {
                    item(key = "breakdown") {
                        Spacer(Modifier.height(4.dp))
                        PriorityBreakdown(tasks = state.pendingTasks)
                    }
                }

                // ── STICKY TAB ROW ───────────────────────────────────────
                stickyHeader(key = "tabs") {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor   = Color.Transparent,
                            contentColor     = MaterialTheme.colorScheme.primary,
                            divider = {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick  = { selectedTab = 0 },
                                text = {
                                    Text(
                                        buildString {
                                            append(s.plannerTabPending)
                                            if (state.pendingTasks.isNotEmpty())
                                                append("  (${state.pendingTasks.size})")
                                        },
                                        fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick  = { selectedTab = 1 },
                                text = {
                                    Text(
                                        buildString {
                                            append(s.plannerTabDone)
                                            if (state.completedTasks.isNotEmpty())
                                                append("  (${state.completedTasks.size})")
                                        },
                                        fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }

                // ── TASK LIST ────────────────────────────────────────────
                val displayTasks = if (selectedTab == 0) state.pendingTasks else state.completedTasks

                if (displayTasks.isEmpty()) {
                    item(key = "empty_$selectedTab") {
                        EmptyState(
                            emoji    = if (selectedTab == 0) "✅" else "📋",
                            title    = if (selectedTab == 0) s.plannerEmptyPendingTitle
                                       else s.plannerEmptyDoneTitle,
                            subtitle = if (selectedTab == 0) s.plannerEmptyPendingSubtitle
                                       else s.plannerEmptyDoneSubtitle,
                            actionLabel = if (selectedTab == 0) s.plannerAddTaskAction else null,
                            onAction    = if (selectedTab == 0) ({ onIntent(PlannerIntent.ShowAddTask) }) else null,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                } else {
                    items(
                        items = displayTasks,
                        key   = { it.id }
                    ) { task ->
                        TaskCard(
                            task     = task,
                            onToggle = { done -> onIntent(PlannerIntent.ToggleTask(task.id, done)) },
                            onDelete = { onIntent(PlannerIntent.DeleteTask(task.id)) },
                            onClick  = {},
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        // ── BOTTOM SHEET ─────────────────────────────────────────────────
        if (state.showAddTaskSheet) {
            AddTaskSheet(
                categories = state.categories,
                onDismiss  = { onIntent(PlannerIntent.HideAddTask) },
                onConfirm  = onIntent
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PROGRESS HEADER CARD
// ═══════════════════════════════════════════════════════════
@Composable
fun PlannerProgressHeader(state: PlannerState) {
    val s = LocalStrings.current
    val animatedProgress by animateFloatAsState(
        targetValue = state.progressPercent,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "headerProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: text + stats
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        state.tasks.isEmpty()          -> s.plannerProgressReady
                        state.progressPercent >= 1f    -> s.plannerProgressDone
                        state.progressPercent >= 0.75f -> s.plannerProgressAlmostThere
                        state.progressPercent >= 0.5f  -> s.plannerProgressHalfway
                        state.progressPercent > 0f     -> s.plannerProgressKeepGoing
                        else                           -> s.plannerProgressLetsGo
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        state.tasks.isEmpty()          -> s.plannerAddFirstTask
                        state.pendingTasks.isEmpty()   -> s.plannerAllTasksDone
                        else                           -> s.plannerTasksRemaining(state.pendingTasks.size)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                )

                Spacer(Modifier.height(20.dp))

                // Stats row
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    HeaderStat(
                        value = "${state.tasks.size}",
                        label = s.plannerLabelTotal,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    HeaderStat(
                        value = "${state.completedTasks.size}",
                        label = s.plannerLabelDone,
                        color = SuccessColor
                    )
                    HeaderStat(
                        value = "${state.pendingTasks.size}",
                        label = s.plannerLabelLeft,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            // Right: animated circular progress ring
            ProgressRing(
                progress    = animatedProgress,
                size        = 86.dp,
                strokeWidth = 7.dp,
                color       = MaterialTheme.colorScheme.primary,
                trackColor  = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = s.plannerDoneLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f)
        )
    }
}

// ═══════════════════════════════════════════════════════════
// ANIMATED CIRCULAR PROGRESS RING (Canvas)
// ═══════════════════════════════════════════════════════════
@Composable
fun ProgressRing(
    progress: Float,
    size: Dp,
    strokeWidth: Dp,
    color: Color,
    trackColor: Color,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset  = strokeWidth.toPx() / 2f
            val arcSize = Size(
                width  = this.size.width  - inset * 2,
                height = this.size.height - inset * 2
            )
            val topLeft = Offset(inset, inset)

            // Track
            drawArc(
                color      = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                style      = stroke,
                topLeft    = topLeft,
                size       = arcSize
            )
            // Progress arc
            if (progress > 0f) {
                drawArc(
                    color      = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter  = false,
                    style      = stroke,
                    topLeft    = topLeft,
                    size       = arcSize
                )
            }
        }
        content()
    }
}

// ═══════════════════════════════════════════════════════════
// PRIORITY BREAKDOWN BAR CHART
// ═══════════════════════════════════════════════════════════
@Composable
fun PriorityBreakdown(tasks: List<Task>) {
    val s = LocalStrings.current
    val counts = remember(tasks) {
        TaskPriority.entries
            .map { p -> p to tasks.count { it.priority == p } }
            .filter { it.second > 0 }
    }
    if (counts.isEmpty()) return

    val maxCount = counts.maxOf { it.second }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text  = s.plannerPriorityBreakdown,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            counts.sortedByDescending { it.first.ordinal }.forEach { (priority, count) ->
                PriorityBar(priority = priority, count = count, maxCount = maxCount)
            }
        }
    }
}

@Composable
private fun PriorityBar(priority: TaskPriority, count: Int, maxCount: Int) {
    val s = LocalStrings.current
    val color    = priority.color()
    val fraction = count.toFloat() / maxCount.coerceAtLeast(1)

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "bar_${priority.name}"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text      = s.priorityLabel(priority),
            style     = MaterialTheme.typography.labelSmall,
            color     = color,
            modifier  = Modifier.width(56.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(color, color.copy(alpha = 0.55f))
                        )
                    )
            )
        }
        Text(
            text     = "$count",
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(18.dp)
        )
    }
}
