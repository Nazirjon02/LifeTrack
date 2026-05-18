package tj.mahram.lifetrack.feature.goals

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.roundTo
import tj.mahram.lifetrack.domain.model.Goal
import tj.mahram.lifetrack.feature.habits.parseHabitColor

private val GoalIcons = listOf("🎯", "🏆", "💪", "📚", "💰", "🚀", "❤️", "🌍", "🎸", "⚽", "🎓", "🏋️")
private val GoalColors = listOf(
    "#7C3AED", "#4F46E5", "#0EA5E9", "#10B981",
    "#F59E0B", "#EF4444", "#EC4899", "#06B6D4"
)

class GoalsScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<GoalsScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        GoalsContent(state = state, onIntent = screenModel::handleIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsContent(state: GoalsState, onIntent: (GoalsIntent) -> Unit) {
    val s = LocalStrings.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            s.goalsTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.goals.isNotEmpty()) {
                            Text(
                                s.goalsSubtitle(state.completedGoals.size, state.goals.size),
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
                onClick = { onIntent(GoalsIntent.ShowAddSheet) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = s.goalsTitle, modifier = Modifier.size(26.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.goals.isEmpty()) {
            GoalsEmptyState(modifier = Modifier.padding(padding))
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
                    GoalsSummaryBanner(state = state)
                }
                if (state.activeGoals.isNotEmpty()) {
                    item(key = "active_header") {
                        Text(
                            s.goalsActiveSection,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                    items(state.activeGoals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onUpdate = { onIntent(GoalsIntent.ShowUpdate(goal)) },
                            onDelete = { onIntent(GoalsIntent.Delete(goal.id)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                if (state.completedGoals.isNotEmpty()) {
                    item(key = "done_header") {
                        Text(
                            s.goalsCompletedSection,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                    items(state.completedGoals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onUpdate = { onIntent(GoalsIntent.ShowUpdate(goal)) },
                            onDelete = { onIntent(GoalsIntent.Delete(goal.id)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        if (state.showAddSheet) {
            AddGoalSheet(
                onDismiss = { onIntent(GoalsIntent.HideAddSheet) },
                onConfirm = onIntent
            )
        }

        state.editingGoal?.let { goal ->
            UpdateProgressDialog(
                goal = goal,
                onDismiss = { onIntent(GoalsIntent.HideUpdate) },
                onConfirm = { newValue ->
                    onIntent(GoalsIntent.UpdateProgress(goal.id, newValue))
                }
            )
        }
    }
}

// ─── Summary Banner ────────────────────────────────────────────────────────────

@Composable
private fun GoalsSummaryBanner(state: GoalsState) {
    val s = LocalStrings.current
    val completionRate = if (state.goals.isEmpty()) 0f
                        else state.completedGoals.size.toFloat() / state.goals.size
    val animRate by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(1100, easing = FastOutSlowInEasing),
        label = "goalRate"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
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
                        completionRate >= 1f   -> s.goalsCompletionAll
                        completionRate >= 0.5f -> s.goalsCompletionHalfway
                        completionRate > 0f    -> s.goalsCompletionKeepPushing
                        else                   -> s.goalsCompletionSet
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    s.goalsAchieved(state.completedGoals.size, state.goals.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                if (state.activeGoals.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            s.goalsActiveCount(state.activeGoals.size),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

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

// ─── Goal Card ─────────────────────────────────────────────────────────────────

@Composable
private fun GoalCard(
    goal: Goal,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val goalColor = parseHabitColor(goal.color)
    val animProgress by animateFloatAsState(
        targetValue = goal.progressFraction,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "goalProgress_${goal.id}"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.isCompleted) goalColor.copy(alpha = 0.07f)
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(goalColor, goalColor.copy(alpha = if (goal.isCompleted) 0.2f else 0.4f))
                        )
                    )
            )
            Column(modifier = Modifier.padding(start = 14.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(goalColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(goal.icon, fontSize = 22.sp)
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            goal.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (goal.isCompleted)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${formatGoalValue(goal.currentValue)} / ${formatGoalValue(goal.targetValue)} ${goal.unit}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (goal.isCompleted) {
                        Text("✅", fontSize = 20.sp)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(goalColor, goalColor.copy(alpha = 0.6f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${goal.progressPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 9.sp
                            )
                        }
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(goalColor.copy(alpha = 0.13f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(goalColor, goalColor.copy(alpha = 0.7f))
                                )
                            )
                    )
                }

                if (!goal.isCompleted) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(goalColor.copy(alpha = 0.12f))
                            .clickable(onClick = onUpdate)
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            s.goalsUpdateProgress,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = goalColor
                        )
                    }
                }
            }
        }
    }
}

// ─── Add Goal Sheet ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalSheet(
    onDismiss: () -> Unit,
    onConfirm: (GoalsIntent) -> Unit
) {
    val s = LocalStrings.current
    var title         by remember { mutableStateOf("") }
    var description   by remember { mutableStateOf("") }
    var selectedIcon  by remember { mutableStateOf(GoalIcons.first()) }
    var selectedColor by remember { mutableStateOf(GoalColors.first()) }
    var targetText    by remember { mutableStateOf("") }
    var unit          by remember { mutableStateOf("") }
    var titleError    by remember { mutableStateOf(false) }
    var targetError   by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            s.addGoalSheetTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            s.addGoalSheetSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Title
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.addGoalFieldTitle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; titleError = false },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(s.addGoalPlaceholderTitle, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        isError = titleError,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        supportingText = if (titleError) ({ Text(s.addGoalErrorTitleEmpty, color = MaterialTheme.colorScheme.error) }) else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }

                // Target + Unit
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(s.addGoalFieldTarget, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = targetText,
                            onValueChange = { targetText = it; targetError = false },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("100", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            isError = targetError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(s.addGoalFieldUnit, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("km", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                // Icon picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.addHabitPickIcon, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoalIcons.forEach { icon ->
                            val isSelected = icon == selectedIcon
                            val bg by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                label = "iconBg_$icon"
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(bg)
                                    .then(
                                        if (isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                                        else Modifier
                                    )
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(icon, fontSize = 22.sp)
                            }
                        }
                    }
                }

                // Color picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.addHabitColorLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoalColors.forEach { hex ->
                            val color = parseHabitColor(hex)
                            val isSelected = hex == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(2.5.dp, Color.White, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    )
                    .clickable {
                        if (title.isBlank()) { titleError = true; return@clickable }
                        val target = targetText.toDoubleOrNull()
                        if (target == null || target <= 0) { targetError = true; return@clickable }
                        onConfirm(
                            GoalsIntent.Create(
                                title = title.trim(),
                                description = description.trim().ifBlank { null },
                                icon = selectedIcon,
                                targetValue = target,
                                unit = unit.trim().ifBlank { "units" },
                                color = selectedColor
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    s.addGoalCreateButton,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ─── Update Progress Dialog ────────────────────────────────────────────────────

@Composable
private fun UpdateProgressDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val s = LocalStrings.current
    val goalColor = parseHabitColor(goal.color)
    var valueText by remember { mutableStateOf(goal.currentValue.let { if (it == 0.0) "" else formatGoalValue(it) }) }
    var isError   by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(goal.icon + " " + goal.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    s.updateGoalCurrentProgress(formatGoalValue(goal.currentValue), formatGoalValue(goal.targetValue), goal.unit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(goalColor.copy(alpha = 0.13f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(goal.progressFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Brush.horizontalGradient(listOf(goalColor, goalColor.copy(alpha = 0.7f))))
                    )
                }
                Text(
                    s.updateGoalProgressLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it; isError = false },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("${formatGoalValue(goal.currentValue)}", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    suffix = { Text(goal.unit, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = goalColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(goalColor)
                    .clickable {
                        val v = valueText.toDoubleOrNull()
                        if (v == null || v < 0) { isError = true; return@clickable }
                        onConfirm(v.coerceAtMost(goal.targetValue))
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(s.updateGoalButton, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancelButton, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// ─── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun GoalsEmptyState(modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎯", fontSize = 72.sp)
        Spacer(Modifier.height(20.dp))
        Text(s.goalsEmptyTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            s.goalsEmptySubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Helpers ───────────────────────────────────────────────────────────────────

private fun formatGoalValue(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else value.roundTo(1)
