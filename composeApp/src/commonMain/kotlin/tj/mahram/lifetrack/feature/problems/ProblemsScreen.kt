package tj.mahram.lifetrack.feature.problems

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.AppStrings
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.domain.model.ProblemPriority
import tj.mahram.lifetrack.domain.model.ProblemStatus
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.brandVividGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.components.parseHexColor
import tj.mahram.lifetrack.ui.theme.appColors

class ProblemsScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<ProblemsScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        ProblemsContent(state = state, onIntent = screenModel::handleIntent)
    }
}

// ── Status / priority colours ───────────────────────────────────────────────
@Composable
fun statusColor(status: ProblemStatus): Color {
    val c = MaterialTheme.appColors
    return when (status) {
        ProblemStatus.ACTIVE -> c.danger
        ProblemStatus.IN_PROGRESS -> c.warning
        ProblemStatus.RESOLVED -> c.success
    }
}

@Composable
fun priorityColor(priority: ProblemPriority): Color {
    val c = MaterialTheme.appColors
    return when (priority) {
        ProblemPriority.HIGH -> c.danger
        ProblemPriority.MEDIUM -> c.warning
        ProblemPriority.LOW -> c.success
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemsContent(state: ProblemsState, onIntent: (ProblemsIntent) -> Unit) {
    val s = LocalStrings.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            s.problemsTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (state.problems.isNotEmpty()) {
                            Text(
                                s.problemsSubtitle(state.unresolvedCount, state.problems.size),
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
                    .clickable { onIntent(ProblemsIntent.ShowAddSheet) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = s.addProblemTitle, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            ViewSwitcher(current = state.view, onSelect = { onIntent(ProblemsIntent.SetView(it)) })

            when (state.view) {
                ProblemsView.LIST -> ProblemsListView(state, onIntent)
                ProblemsView.CALENDAR -> ProblemsCalendarView(state, onIntent)
                ProblemsView.ANALYTICS -> ProblemsAnalyticsView(state)
            }
        }

        if (state.showAddSheet) {
            AddProblemSheet(
                editing = state.editing,
                onDismiss = { onIntent(ProblemsIntent.HideAddSheet) },
                onIntent = onIntent
            )
        }
        state.detail?.let { problem ->
            ProblemDetailSheet(
                problem = state.problems.firstOrNull { it.id == problem.id } ?: problem,
                history = state.detailHistory,
                onDismiss = { onIntent(ProblemsIntent.HideDetail) },
                onIntent = onIntent
            )
        }
        state.progressTarget?.let { problem ->
            ProgressDialog(
                problem = problem,
                onDismiss = { onIntent(ProblemsIntent.HideProgress) },
                onConfirm = { onIntent(ProblemsIntent.SetProgress(problem.id, it)) }
            )
        }
    }
}

// ── View switcher (segmented pill) ──────────────────────────────────────────
@Composable
private fun ViewSwitcher(current: ProblemsView, onSelect: (ProblemsView) -> Unit) {
    val s = LocalStrings.current
    val items = listOf(
        ProblemsView.LIST to s.problemsViewList,
        ProblemsView.CALENDAR to s.problemsViewCalendar,
        ProblemsView.ANALYTICS to s.problemsViewAnalytics
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .glassSurface(shape = RoundedCornerShape(20.dp)).padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { (view, label) ->
            val selected = view == current
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(15.dp))
                    .then(if (selected) Modifier.background(brandHorizontalGradient()) else Modifier)
                    .clickable { onSelect(view) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── List view ───────────────────────────────────────────────────────────────
@Composable
private fun ProblemsListView(state: ProblemsState, onIntent: (ProblemsIntent) -> Unit) {
    val s = LocalStrings.current
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    if (state.problems.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                emoji = "🧩",
                title = s.problemsEmptyTitle,
                subtitle = s.problemsEmptySubtitle,
                actionLabel = s.addProblemCreateButton,
                onAction = { onIntent(ProblemsIntent.ShowAddSheet) }
            )
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(top = 6.dp, bottom = 110.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "summary") { ProblemsSummary(state) }

        section(s.problemsSectionActive, state.activeProblems, onIntent)
        section(s.problemsSectionInProgress, state.inProgressProblems, onIntent)
        section(s.problemsSectionResolved, state.resolvedProblems, onIntent)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    title: String,
    problems: List<Problem>,
    onIntent: (ProblemsIntent) -> Unit
) {
    if (problems.isEmpty()) return
    item(key = "h_$title") {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
    items(problems, key = { it.id }) { problem ->
        ProblemCard(problem = problem, onIntent = onIntent, modifier = Modifier.animateItem())
    }
}

@Composable
private fun ProblemsSummary(state: ProblemsState) {
    val s = LocalStrings.current
    val rate = state.analytics.overallRate
    val anim by animateFloatAsState(rate, tween(1000, easing = FastOutSlowInEasing), label = "sumRate")
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(brandVividGradient()).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (state.unresolvedCount == 0) "🎉" else "${state.unresolvedCount}",
                style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color.White
            )
            Text(s.problemsSubtitle(state.unresolvedCount, state.problems.size), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${(anim * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(s.analyticsResolutionRate, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

// ── Problem card ─────────────────────────────────────────────────────────────
@Composable
fun ProblemCard(problem: Problem, onIntent: (ProblemsIntent) -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val accent = statusColor(problem.status)
    val prioColor = priorityColor(problem.priority)
    val resolved = problem.isResolved
    val animProgress by animateFloatAsState(problem.progressFraction, tween(800, easing = FastOutSlowInEasing), label = "p_${problem.id}")

    Column(
        modifier = modifier.fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(22.dp), glow = if (resolved) null else accent)
            .clickable { onIntent(ProblemsIntent.ShowDetail(problem)) }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Status dot
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(accent))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    problem.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (resolved) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Chip(text = s.problemPriorityName(problem.priority), color = prioColor)
                    if (problem.category.isNotBlank()) Chip(text = problem.category, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text("${problem.progress}%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = accent)
        }

        // Progress bar
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(accent.copy(alpha = 0.15f))) {
            Box(modifier = Modifier.fillMaxWidth(animProgress).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.6f)))))
        }

        // Actions
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!resolved) {
                SmallAction(text = "＋ " + s.problemUpdateProgress, color = accent, modifier = Modifier.weight(1f)) { onIntent(ProblemsIntent.ShowProgress(problem)) }
                SmallAction(text = "✓", color = MaterialTheme.appColors.success, filled = true) {
                    onIntent(ProblemsIntent.SetStatus(problem, ProblemStatus.RESOLVED))
                }
            } else {
                SmallAction(text = s.problemReopen, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f)) {
                    onIntent(ProblemsIntent.SetStatus(problem, ProblemStatus.IN_PROGRESS))
                }
            }
            IconButton(onClick = { onIntent(ProblemsIntent.ShowEdit(problem)) }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Outlined.Edit, contentDescription = s.problemEditAction, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { onIntent(ProblemsIntent.Delete(problem.id)) }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = s.problemDeleteAction, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun Chip(text: String, color: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(7.dp)).background(color.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun SmallAction(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(if (filled) Modifier.background(color) else Modifier.background(color.copy(alpha = 0.15f)))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (filled) Color.White else color, maxLines = 1)
    }
}

// ── Progress dialog ──────────────────────────────────────────────────────────
@Composable
private fun ProgressDialog(problem: Problem, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val s = LocalStrings.current
    val accent = statusColor(problem.status)
    var value by remember { mutableStateOf(problem.progress.toFloat()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = { Text(problem.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("${s.problemProgressLabel}: ${value.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0f..100f,
                    steps = 19,
                    colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent)
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(accent).clickable { onConfirm(value.toInt()) }.padding(horizontal = 20.dp, vertical = 10.dp)
            ) { Text(s.saveButton, color = Color.White, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancelButton, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}
