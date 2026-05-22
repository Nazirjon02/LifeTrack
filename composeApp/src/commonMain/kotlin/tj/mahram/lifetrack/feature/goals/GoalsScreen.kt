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
import androidx.compose.material.icons.outlined.DeleteOutline
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
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.FieldLabel
import tj.mahram.lifetrack.ui.components.GradientButton
import tj.mahram.lifetrack.ui.components.SheetHandle
import tj.mahram.lifetrack.ui.components.SheetHeader
import tj.mahram.lifetrack.ui.components.SheetTextField
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.brandVividGradient
import tj.mahram.lifetrack.ui.components.glassSurface

private val GoalIcons = listOf("🎯", "🏆", "💪", "📚", "💰", "🚀", "❤️", "🌍", "🎸", "⚽", "🎓", "🏋️")
private val GoalColors = listOf("#7C3AED", "#4F46E5", "#0EA5E9", "#10B981", "#F59E0B", "#EF4444", "#EC4899", "#06B6D4")

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
                        Text(s.goalsTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                        if (state.goals.isNotEmpty()) {
                            Text(
                                s.goalsSubtitle(state.completedGoals.size, state.goals.size),
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
                    .clickable { onIntent(GoalsIntent.ShowAddSheet) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = s.goalsTitle, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.goals.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    emoji = "🎯",
                    title = s.goalsEmptyTitle,
                    subtitle = s.goalsEmptySubtitle,
                    actionLabel = s.goalsTitle,
                    onAction = { onIntent(GoalsIntent.ShowAddSheet) }
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = 96.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "summary") { GoalsSummaryBanner(state = state) }
                if (state.activeGoals.isNotEmpty()) {
                    item(key = "active_header") {
                        Text(s.goalsActiveSection, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
                    }
                    items(state.activeGoals, key = { it.id }) { goal ->
                        GoalCard(goal = goal, onUpdate = { onIntent(GoalsIntent.ShowUpdate(goal)) }, onDelete = { onIntent(GoalsIntent.Delete(goal.id)) }, modifier = Modifier.animateItem())
                    }
                }
                if (state.completedGoals.isNotEmpty()) {
                    item(key = "done_header") {
                        Text(s.goalsCompletedSection, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
                    }
                    items(state.completedGoals, key = { it.id }) { goal ->
                        GoalCard(goal = goal, onUpdate = { onIntent(GoalsIntent.ShowUpdate(goal)) }, onDelete = { onIntent(GoalsIntent.Delete(goal.id)) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }

        if (state.showAddSheet) {
            AddGoalSheet(onDismiss = { onIntent(GoalsIntent.HideAddSheet) }, onConfirm = onIntent)
        }
        state.editingGoal?.let { goal ->
            UpdateProgressDialog(goal = goal, onDismiss = { onIntent(GoalsIntent.HideUpdate) }, onConfirm = { newValue -> onIntent(GoalsIntent.UpdateProgress(goal.id, newValue)) })
        }
    }
}

// ─── Summary Banner ────────────────────────────────────────────────────────────
@Composable
private fun GoalsSummaryBanner(state: GoalsState) {
    val s = LocalStrings.current
    val completionRate = if (state.goals.isEmpty()) 0f else state.completedGoals.size.toFloat() / state.goals.size
    val animRate by animateFloatAsState(targetValue = completionRate, animationSpec = tween(1100, easing = FastOutSlowInEasing), label = "goalRate")

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(brandVividGradient()).padding(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    when {
                        completionRate >= 1f -> s.goalsCompletionAll
                        completionRate >= 0.5f -> s.goalsCompletionHalfway
                        completionRate > 0f -> s.goalsCompletionKeepPushing
                        else -> s.goalsCompletionSet
                    },
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White
                )
                Text(s.goalsAchieved(state.completedGoals.size, state.goals.size), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                if (state.activeGoals.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                        Text(s.goalsActiveCount(state.activeGoals.size), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
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
                    if (animRate > 0f) drawArc(color = Color.White, startAngle = -90f, sweepAngle = 360f * animRate, useCenter = false, style = stroke, topLeft = tl, size = arc)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(animRate * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(s.plannerDoneLabel, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// ─── Goal Card ─────────────────────────────────────────────────────────────────
@Composable
private fun GoalCard(goal: Goal, onUpdate: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val goalColor = parseHabitColor(goal.color)
    val animProgress by animateFloatAsState(targetValue = goal.progressFraction, animationSpec = tween(900, easing = FastOutSlowInEasing), label = "goalProgress_${goal.id}")

    Column(
        modifier = modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(22.dp), glow = if (goal.isCompleted) null else goalColor).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(goalColor.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Text(goal.icon, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    goal.title,
                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                    color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${formatGoalValue(goal.currentValue)} / ${formatGoalValue(goal.targetValue)} ${goal.unit}",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (goal.isCompleted) {
                Text("✅", fontSize = 22.sp)
            } else {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Brush.radialGradient(listOf(goalColor, goalColor.copy(alpha = 0.6f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${goal.progressPercent}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 9.sp)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(goalColor.copy(alpha = 0.15f))) {
            Box(modifier = Modifier.fillMaxWidth(animProgress).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(goalColor, goalColor.copy(alpha = 0.65f)))))
        }

        if (!goal.isCompleted) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(goalColor.copy(alpha = 0.15f)).clickable(onClick = onUpdate).padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(s.goalsUpdateProgress, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = goalColor)
            }
        }
    }
}

// ─── Add Goal Sheet ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalSheet(onDismiss: () -> Unit, onConfirm: (GoalsIntent) -> Unit) {
    val s = LocalStrings.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(GoalIcons.first()) }
    var selectedColor by remember { mutableStateOf(GoalColors.first()) }
    var targetText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var targetError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { SheetHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp)) {
            SheetHeader(title = s.addGoalSheetTitle, subtitle = s.addGoalSheetSubtitle, onClose = onDismiss)

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FieldLabel(s.addGoalFieldTitle)
                SheetTextField(value = title, onValueChange = { title = it; titleError = false }, placeholder = s.addGoalPlaceholderTitle, isError = titleError, errorText = if (titleError) s.addGoalErrorTitleEmpty else null)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FieldLabel(s.addGoalFieldTarget)
                        SheetTextField(value = targetText, onValueChange = { targetText = it; targetError = false }, placeholder = "100", isError = targetError, keyboardType = KeyboardType.Decimal)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FieldLabel(s.addGoalFieldUnit)
                        SheetTextField(value = unit, onValueChange = { unit = it }, placeholder = "km")
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(s.addHabitPickIcon)
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GoalIcons.forEach { icon ->
                            val isSelected = icon == selectedIcon
                            Box(
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(15.dp))
                                    .then(if (isSelected) Modifier.background(brandHorizontalGradient()) else Modifier.glassSurface(shape = RoundedCornerShape(15.dp)))
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) { Text(icon, fontSize = 22.sp) }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(s.addHabitColorLabel)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GoalColors.forEach { hex ->
                            val color = parseHabitColor(hex)
                            val isSelected = hex == selectedColor
                            Box(
                                modifier = Modifier.size(38.dp).clip(CircleShape).background(color)
                                    .then(if (isSelected) Modifier.border(2.5.dp, Color.White, CircleShape) else Modifier)
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) { if (isSelected) Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            GradientButton(
                text = s.addGoalCreateButton,
                onClick = {
                    val target = targetText.toDoubleOrNull()
                    when {
                        title.isBlank() -> titleError = true
                        target == null || target <= 0 -> targetError = true
                        else -> onConfirm(GoalsIntent.Create(title = title.trim(), description = description.trim().ifBlank { null }, icon = selectedIcon, targetValue = target, unit = unit.trim().ifBlank { "units" }, color = selectedColor))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                height = 56.dp
            )
        }
    }
}

// ─── Update Progress Dialog ────────────────────────────────────────────────────
@Composable
private fun UpdateProgressDialog(goal: Goal, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    val s = LocalStrings.current
    val goalColor = parseHabitColor(goal.color)
    var valueText by remember { mutableStateOf(goal.currentValue.let { if (it == 0.0) "" else formatGoalValue(it) }) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(goal.icon + " " + goal.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(s.updateGoalCurrentProgress(formatGoalValue(goal.currentValue), formatGoalValue(goal.targetValue), goal.unit), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(goalColor.copy(alpha = 0.15f))) {
                    Box(modifier = Modifier.fillMaxWidth(goal.progressFraction).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(goalColor, goalColor.copy(alpha = 0.7f)))))
                }
                FieldLabel(s.updateGoalProgressLabel)
                OutlinedTextField(
                    value = valueText, onValueChange = { valueText = it; isError = false },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(formatGoalValue(goal.currentValue), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    suffix = { Text(goal.unit, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    isError = isError, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = goalColor, unfocusedBorderColor = MaterialTheme.colorScheme.outline)
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(goalColor).clickable {
                    val v = valueText.toDoubleOrNull()
                    if (v == null || v < 0) { isError = true; return@clickable }
                    onConfirm(v.coerceAtMost(goal.targetValue))
                }.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(s.updateGoalButton, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancelButton, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

private fun formatGoalValue(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.roundTo(1)
