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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.Task
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.TaskCard
import tj.mahram.lifetrack.ui.components.TaskCardSkeleton
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.glassColor
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.theme.appColors

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
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(brandHorizontalGradient())
                    .clickable { onIntent(PlannerIntent.ShowAddTask) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.padding(top = padding.calculateTopPadding())) {
            if (state.isLoading) {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) { items(5) { TaskCardSkeleton() } }
            } else {
                AnimatedContent(
                    targetState = state.plannerView,
                    transitionSpec = {
                        val toRight = targetState.ordinal > initialState.ordinal
                        (slideInHorizontally(initialOffsetX = { if (toRight) it else -it }, animationSpec = tween(280)) +
                            fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(targetOffsetX = { if (toRight) -it else it }, animationSpec = tween(280)) +
                                fadeOut(tween(150)))
                    },
                    label = "plannerViewAnim"
                ) { view ->
                    when (view) {
                        PlannerView.YEAR -> PlannerYearView(state, onIntent)
                        PlannerView.MONTH -> PlannerMonthView(state, onIntent)
                        PlannerView.DAY -> PlannerDayView(state, onIntent)
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
    val s = LocalStrings.current
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Text(
                    s.plannerScreenTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
        PlannerViewSwitcher(
            currentView = state.plannerView,
            onViewChange = { onIntent(PlannerIntent.SetView(it)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun PlannerViewSwitcher(
    currentView: PlannerView,
    onViewChange: (PlannerView) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val views = listOf(
        PlannerView.YEAR to s.plannerViewYear,
        PlannerView.MONTH to s.plannerViewMonth,
        PlannerView.DAY to s.plannerViewDay
    )
    val brand = brandHorizontalGradient()

    Row(
        modifier = modifier
            .glassSurface(shape = RoundedCornerShape(18.dp))
            .padding(5.dp)
    ) {
        views.forEach { (view, label) ->
            val isSelected = currentView == view
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200), label = "switchText_$label"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .then(if (isSelected) Modifier.background(brand, RoundedCornerShape(14.dp)) else Modifier)
                    .clickable { onViewChange(view) }
                    .padding(vertical = 11.dp),
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

// ═══════════════════════════════════════════════════════════
// YEAR VIEW
// ═══════════════════════════════════════════════════════════
@Composable
private fun PlannerYearView(state: PlannerState, onIntent: (PlannerIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 96.dp)
    ) {
        PlannerNavigator(
            title = "${state.selectedYear}",
            onPrev = { onIntent(PlannerIntent.NavigateYear(state.selectedYear - 1)) },
            onNext = { onIntent(PlannerIntent.NavigateYear(state.selectedYear + 1)) }
        )
        (1..12).chunked(3).forEach { rowMonths ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowMonths.forEach { month ->
                    MonthCard(
                        year = state.selectedYear,
                        month = month,
                        tasks = state.tasksForMonth(state.selectedYear, month),
                        isCurrentMonth = state.todayDate.year == state.selectedYear &&
                            state.todayDate.month.number == month,
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
    val s = LocalStrings.current
    val c = MaterialTheme.appColors
    val completion = if (tasks.isEmpty()) 0f else tasks.count { it.isCompleted }.toFloat() / tasks.size
    val animatedCompletion by animateFloatAsState(targetValue = completion, animationSpec = tween(900), label = "monthCompletion_$month")
    val ringColor = if (isCurrentMonth) MaterialTheme.colorScheme.primary else c.success

    Box(
        modifier = modifier
            .height(110.dp)
            .glassSurface(
                shape = RoundedCornerShape(20.dp),
                glow = if (isCurrentMonth) MaterialTheme.colorScheme.primary else null
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Text(
                s.plannerMonthAbbrevs[month - 1],
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (tasks.isEmpty()) s.plannerNoTasksMonth else s.plannerTasksCountMonth(tasks.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ProgressRing(
            progress = animatedCompletion,
            size = 38.dp,
            strokeWidth = 4.dp,
            color = ringColor,
            trackColor = ringColor.copy(alpha = 0.18f),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Text(
                "${(animatedCompletion * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// MONTH VIEW
// ═══════════════════════════════════════════════════════════
@Composable
private fun PlannerMonthView(state: PlannerState, onIntent: (PlannerIntent) -> Unit) {
    val s = LocalStrings.current
    val year = state.selectedYear
    val month = state.selectedMonth

    val firstDay = remember(year, month) { LocalDate(year, month, 1) }
    val offset = remember(firstDay) { firstDay.dayOfWeek.isoDayNumber - 1 }
    val daysInMonth = remember(year, month) {
        LocalDate(year, month, 1).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).day
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 96.dp)
    ) {
        PlannerNavigator(
            title = "${s.plannerMonthFullNames[month - 1]} $year",
            onPrev = {
                val prev = LocalDate(year, month, 1).minus(1, DateTimeUnit.MONTH)
                onIntent(PlannerIntent.NavigateMonth(prev.year, prev.month.number))
            },
            onNext = {
                val next = LocalDate(year, month, 1).plus(1, DateTimeUnit.MONTH)
                onIntent(PlannerIntent.NavigateMonth(next.year, next.month.number))
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            s.plannerWeekDayHeaders.forEach { day ->
                Text(
                    day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        val totalCells = offset + daysInMonth
        val rows = (totalCells + 6) / 7
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 7) {
                    val day = row * 7 + col - offset + 1
                    if (day < 1 || day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(54.dp))
                    } else {
                        val isToday = state.todayDate.year == year && state.todayDate.month.number == month && state.todayDate.day == day
                        val isSelected = state.selectedDay == day && state.selectedMonth == month && state.selectedYear == year
                        CalendarDayCell(
                            day = day,
                            isToday = isToday,
                            isSelected = isSelected,
                            tasks = state.tasksForDay(year, month, day),
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
    val brand = brandHorizontalGradient()
    val base = modifier
        .height(54.dp)
        .clip(RoundedCornerShape(14.dp))
        .then(
            when {
                isSelected -> Modifier.background(brand, RoundedCornerShape(14.dp))
                isToday -> Modifier.glassSurface(shape = RoundedCornerShape(14.dp), glow = MaterialTheme.colorScheme.primary)
                else -> Modifier
            }
        )
        .clickable(onClick = onClick)
        .padding(vertical = 5.dp)

    val textColor = when {
        isSelected -> Color.White
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = base, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(
            "$day",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
        if (tasks.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 3.dp)) {
                tasks.take(3).forEach { task ->
                    val dotColor = if (isSelected) Color.White.copy(alpha = 0.85f) else task.priority.glassColor()
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(dotColor))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DAY VIEW
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlannerDayView(state: PlannerState, onIntent: (PlannerIntent) -> Unit) {
    val s = LocalStrings.current
    var selectedTab by remember { mutableStateOf(0) }
    val date = state.selectedDate

    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        item {
            PlannerNavigator(
                title = buildString {
                    append(s.plannerDayOfWeekNames[date.dayOfWeek.isoDayNumber - 1])
                    append(", "); append(date.day); append(" ")
                    append(s.plannerMonthAbbrevs[date.month.number - 1])
                },
                onPrev = {
                    val prev = date.minus(1, DateTimeUnit.DAY)
                    onIntent(PlannerIntent.NavigateDay(prev.year, prev.month.number, prev.day))
                },
                onNext = {
                    val next = date.plus(1, DateTimeUnit.DAY)
                    onIntent(PlannerIntent.NavigateDay(next.year, next.month.number, next.day))
                }
            )
        }

        if (state.dayTasks.isNotEmpty()) {
            item { DayProgressCard(state = state) }
        }

        stickyHeader {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    s.plannerTabPending to state.dayPendingTasks.size,
                    s.plannerTabDone to state.dayCompletedTasks.size
                ).forEachIndexed { idx, (label, count) ->
                    SegTab(
                        label = if (count > 0) "$label ($count)" else label,
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        val displayTasks = if (selectedTab == 0) state.dayPendingTasks else state.dayCompletedTasks

        if (displayTasks.isEmpty()) {
            item {
                EmptyState(
                    emoji = if (selectedTab == 0) "✅" else "📋",
                    title = if (selectedTab == 0) s.plannerEmptyDayTitle else s.plannerEmptyDoneTitle,
                    subtitle = if (selectedTab == 0) s.plannerEmptyDaySubtitle else s.plannerDoneTabSubtitle,
                    actionLabel = if (selectedTab == 0) s.plannerAddTaskAction else null,
                    onAction = if (selectedTab == 0) ({ onIntent(PlannerIntent.ShowAddTask) }) else null,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            items(displayTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggle = { done -> onIntent(PlannerIntent.ToggleTask(task.id, done)) },
                    onDelete = { onIntent(PlannerIntent.DeleteTask(task.id)) },
                    onClick = {},
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun SegTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val brand = brandHorizontalGradient()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (selected) Modifier.background(brand, RoundedCornerShape(14.dp)) else Modifier.glassSurface(shape = RoundedCornerShape(14.dp)))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayProgressCard(state: PlannerState) {
    val s = LocalStrings.current
    val progress = if (state.dayTasks.isEmpty()) 0f else state.dayCompletedTasks.size.toFloat() / state.dayTasks.size
    val animated by animateFloatAsState(targetValue = progress, animationSpec = tween(900), label = "dayProgress")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .glassSurface(shape = RoundedCornerShape(24.dp), glow = MaterialTheme.colorScheme.primary)
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when {
                        progress >= 1f -> s.plannerProgressDone
                        progress >= 0.5f -> s.plannerProgressHalfway
                        progress > 0f -> s.plannerProgressKeepGoing
                        else -> s.plannerProgressReady
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    s.plannerTasksDoneOf(state.dayCompletedTasks.size, state.dayTasks.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(16.dp))
            ProgressRing(
                progress = animated, size = 64.dp, strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            ) {
                Text(
                    "${(animated * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavArrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous", onPrev)
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        NavArrow(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next", onNext)
    }
}

@Composable
private fun NavArrow(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(42.dp).glassSurface(shape = CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
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
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2f
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke, topLeft = topLeft, size = arcSize)
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(color.copy(alpha = 0.5f), color)),
                    startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false, style = stroke, topLeft = topLeft, size = arcSize
                )
            }
        }
        content()
    }
}
