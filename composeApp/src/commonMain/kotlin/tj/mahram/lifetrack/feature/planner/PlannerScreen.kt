package tj.mahram.lifetrack.feature.planner

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.datetime.*
import org.koin.compose.getKoin
import tj.mahram.lifetrack.domain.model.Task
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.TaskCard
import tj.mahram.lifetrack.ui.components.TaskCardSkeleton
import tj.mahram.lifetrack.ui.components.color
import tj.mahram.lifetrack.ui.theme.SuccessColor

private val MonthAbbrevs = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
private val MonthFullNames = listOf("January","February","March","April","May","June","July","August","September","October","November","December")
private val WeekDayHeaders = listOf("Mo","Tu","We","Th","Fr","Sa","Su")

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
    Scaffold(
        topBar = { PlannerTopBar(state = state, onIntent = onIntent) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(PlannerIntent.ShowAddTask) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(26.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(top = padding.calculateTopPadding())) {
            if (state.isLoading) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 96.dp)
                ) { items(5) { TaskCardSkeleton() } }
            } else {
                AnimatedContent(
                    targetState = state.plannerView,
                    transitionSpec = {
                        val toRight = targetState.ordinal > initialState.ordinal
                        (slideInHorizontally(
                            initialOffsetX = { if (toRight) it else -it },
                            animationSpec = tween(280)
                        ) + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { if (toRight) -it else it },
                            animationSpec = tween(280)
                        ) + fadeOut(tween(150)))
                    },
                    label = "plannerViewAnim"
                ) { view ->
                    when (view) {
                        PlannerView.YEAR  -> PlannerYearView(state, onIntent, padding.calculateBottomPadding())
                        PlannerView.MONTH -> PlannerMonthView(state, onIntent, padding.calculateBottomPadding())
                        PlannerView.DAY   -> PlannerDayView(state, onIntent, padding.calculateBottomPadding())
                    }
                }
            }
        }

        if (state.showAddTaskSheet) {
            val initialMillis = if (state.plannerView == PlannerView.DAY) {
                state.selectedDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            } else null
            AddTaskSheet(
                categories = state.categories,
                initialDueDateMillis = initialMillis,
                onDismiss = { onIntent(PlannerIntent.HideAddTask) },
                onConfirm = onIntent
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// TOP BAR
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlannerTopBar(state: PlannerState, onIntent: (PlannerIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Planner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        PlannerViewSwitcher(
            currentView = state.plannerView,
            onViewChange = { onIntent(PlannerIntent.SetView(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun PlannerViewSwitcher(
    currentView: PlannerView,
    onViewChange: (PlannerView) -> Unit,
    modifier: Modifier = Modifier
) {
    val views = listOf(PlannerView.YEAR to "Year", PlannerView.MONTH to "Month", PlannerView.DAY to "Day")

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            views.forEach { (view, label) ->
                val isSelected = currentView == view
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "switchBg_$label"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200),
                    label = "switchText_$label"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { onViewChange(view) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// YEAR VIEW
// ═══════════════════════════════════════════════════════════
@Composable
private fun PlannerYearView(
    state: PlannerState,
    onIntent: (PlannerIntent) -> Unit,
    bottomPadding: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding + 96.dp)
    ) {
        PlannerNavigator(
            title = "${state.selectedYear}",
            onPrev = { onIntent(PlannerIntent.NavigateYear(state.selectedYear - 1)) },
            onNext = { onIntent(PlannerIntent.NavigateYear(state.selectedYear + 1)) }
        )
        (1..12).chunked(3).forEach { rowMonths ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowMonths.forEach { month ->
                    MonthCard(
                        year = state.selectedYear,
                        month = month,
                        tasks = state.tasksForMonth(state.selectedYear, month),
                        isCurrentMonth = state.todayDate.year == state.selectedYear &&
                                         state.todayDate.monthNumber == month,
                        onClick = { onIntent(PlannerIntent.SelectMonth(state.selectedYear, month)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCard(
    year: Int,
    month: Int,
    tasks: List<Task>,
    isCurrentMonth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completion = if (tasks.isEmpty()) 0f
                     else tasks.count { it.isCompleted }.toFloat() / tasks.size
    val animatedCompletion by animateFloatAsState(
        targetValue = completion,
        animationSpec = tween(900),
        label = "monthCompletion_$month"
    )

    val cardBg = if (isCurrentMonth) MaterialTheme.colorScheme.primaryContainer
                 else MaterialTheme.colorScheme.surface
    val onCard  = if (isCurrentMonth) MaterialTheme.colorScheme.onPrimaryContainer
                  else MaterialTheme.colorScheme.onSurface
    val ringColor = if (isCurrentMonth) MaterialTheme.colorScheme.primary else SuccessColor

    Card(
        onClick = onClick,
        modifier = modifier
            .height(108.dp)
            .then(
                if (isCurrentMonth) Modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column {
                Text(
                    MonthAbbrevs[month - 1],
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = onCard
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (tasks.isEmpty()) "No tasks"
                    else "${tasks.size} task${if (tasks.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = onCard.copy(alpha = 0.65f)
                )
            }
            ProgressRing(
                progress = animatedCompletion,
                size = 36.dp,
                strokeWidth = 4.dp,
                color = ringColor,
                trackColor = ringColor.copy(alpha = 0.15f),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    "${(animatedCompletion * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = onCard,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// MONTH VIEW
// ═══════════════════════════════════════════════════════════
@Composable
private fun PlannerMonthView(
    state: PlannerState,
    onIntent: (PlannerIntent) -> Unit,
    bottomPadding: Dp
) {
    val year  = state.selectedYear
    val month = state.selectedMonth

    val firstDay    = remember(year, month) { LocalDate(year, month, 1) }
    val offset      = remember(firstDay) { firstDay.dayOfWeek.isoDayNumber - 1 }
    val daysInMonth = remember(year, month) {
        LocalDate(year, month, 1).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding + 96.dp)
    ) {
        PlannerNavigator(
            title = "${MonthFullNames[month - 1]} $year",
            onPrev = {
                val prev = LocalDate(year, month, 1).minus(1, DateTimeUnit.MONTH)
                onIntent(PlannerIntent.NavigateMonth(prev.year, prev.monthNumber))
            },
            onNext = {
                val next = LocalDate(year, month, 1).plus(1, DateTimeUnit.MONTH)
                onIntent(PlannerIntent.NavigateMonth(next.year, next.monthNumber))
            }
        )

        // Day-of-week headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeekDayHeaders.forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Calendar grid
        val totalCells = offset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    val day = row * 7 + col - offset + 1
                    if (day < 1 || day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(52.dp))
                    } else {
                        val isToday    = state.todayDate.year == year &&
                                         state.todayDate.monthNumber == month &&
                                         state.todayDate.dayOfMonth == day
                        val isSelected = state.selectedDay == day &&
                                         state.selectedMonth == month &&
                                         state.selectedYear == year
                        val dayTasks = state.tasksForDay(year, month, day)

                        CalendarDayCell(
                            day = day,
                            isToday = isToday,
                            isSelected = isSelected,
                            tasks = dayTasks,
                            onClick = { onIntent(PlannerIntent.SelectDay(year, month, day)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    tasks: List<Task>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday    -> MaterialTheme.colorScheme.primaryContainer
        else       -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday    -> MaterialTheme.colorScheme.onPrimaryContainer
        else       -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "$day",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
        if (tasks.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                tasks.take(3).forEach { task ->
                    val dotColor = if (isSelected) textColor.copy(alpha = 0.8f) else task.priority.color()
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DAY VIEW
// ═══════════════════════════════════════════════════════════
@Composable
private fun PlannerDayView(
    state: PlannerState,
    onIntent: (PlannerIntent) -> Unit,
    bottomPadding: Dp
) {
    var selectedTab by remember { mutableStateOf(0) }
    val date = state.selectedDate

    LazyColumn(
        contentPadding = PaddingValues(bottom = bottomPadding + 96.dp)
    ) {
        item {
            PlannerNavigator(
                title = buildString {
                    val dow = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                    append(dow)
                    append(", ")
                    append(date.dayOfMonth)
                    append(" ")
                    append(MonthAbbrevs[date.monthNumber - 1])
                },
                onPrev = {
                    val prev = date.minus(1, DateTimeUnit.DAY)
                    onIntent(PlannerIntent.NavigateDay(prev.year, prev.monthNumber, prev.dayOfMonth))
                },
                onNext = {
                    val next = date.plus(1, DateTimeUnit.DAY)
                    onIntent(PlannerIntent.NavigateDay(next.year, next.monthNumber, next.dayOfMonth))
                }
            )
        }

        if (state.dayTasks.isNotEmpty()) {
            item { DayProgressCard(state = state) }
        }

        stickyHeader {
            Surface(color = MaterialTheme.colorScheme.background) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                ) {
                    listOf(
                        "Pending" to state.dayPendingTasks.size,
                        "Done"    to state.dayCompletedTasks.size
                    ).forEachIndexed { idx, (label, count) ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick  = { selectedTab = idx },
                            text = {
                                Text(
                                    if (count > 0) "$label ($count)" else label,
                                    fontWeight = if (selectedTab == idx) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        val displayTasks = if (selectedTab == 0) state.dayPendingTasks else state.dayCompletedTasks

        if (displayTasks.isEmpty()) {
            item {
                EmptyState(
                    emoji    = if (selectedTab == 0) "✅" else "📋",
                    title    = if (selectedTab == 0) "No tasks for this day" else "No completed tasks",
                    subtitle = if (selectedTab == 0) "Tap + to schedule a task here" else "Complete some tasks first",
                    actionLabel = if (selectedTab == 0) "Add Task" else null,
                    onAction    = if (selectedTab == 0) ({ onIntent(PlannerIntent.ShowAddTask) }) else null,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            items(displayTasks, key = { it.id }) { task ->
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

@Composable
private fun DayProgressCard(state: PlannerState) {
    val progress = if (state.dayTasks.isEmpty()) 0f
                   else state.dayCompletedTasks.size.toFloat() / state.dayTasks.size
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(900),
        label = "dayProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when {
                        progress >= 1f    -> "All done! 🎉"
                        progress >= 0.5f  -> "Halfway there 💪"
                        progress > 0f     -> "Keep going 🚀"
                        else              -> "Ready to start? ⚡"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${state.dayCompletedTasks.size} of ${state.dayTasks.size} tasks done",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.width(16.dp))
            ProgressRing(
                progress    = animated,
                size        = 64.dp,
                strokeWidth = 5.dp,
                color       = MaterialTheme.colorScheme.primary,
                trackColor  = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    "${(animated * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SHARED: NAVIGATOR ROW
// ═══════════════════════════════════════════════════════════
@Composable
private fun PlannerNavigator(title: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onNext) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// ANIMATED CIRCULAR PROGRESS RING
// ═══════════════════════════════════════════════════════════
@Composable
fun ProgressRing(
    progress: Float,
    size: Dp,
    strokeWidth: Dp,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke  = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset   = strokeWidth.toPx() / 2f
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor, startAngle = -90f, sweepAngle = 360f,
                useCenter = false, style = stroke, topLeft = topLeft, size = arcSize
            )
            if (progress > 0f) {
                drawArc(
                    color = color, startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false, style = stroke, topLeft = topLeft, size = arcSize
                )
            }
        }
        content()
    }
}
