package tj.mahram.lifetrack.feature.planner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import kotlin.time.Clock
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.ui.components.color

private data class DateOption(val label: String, val millis: Long?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    categories: List<Category>,
    initialDueDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (PlannerIntent) -> Unit
) {
    var title              by remember { mutableStateOf("") }
    var description        by remember { mutableStateOf("") }
    var priority           by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var dueDateMillis      by remember { mutableStateOf(initialDueDateMillis) }
    var titleError         by remember { mutableStateOf(false) }

    val now          = remember { Clock.System.now().toEpochMilliseconds() }
    val dayMs        = 24 * 60 * 60 * 1000L
    val dateOptions  = remember(initialDueDateMillis) {
        listOf(
            DateOption("None",      null),
            DateOption("Today",     now),
            DateOption("Tomorrow",  now + dayMs),
            DateOption("Next Week", now + 7 * dayMs)
        ).let { opts ->
            if (initialDueDateMillis != null && opts.none { it.millis == initialDueDateMillis }) {
                listOf(DateOption("Selected", initialDueDateMillis)) + opts.drop(1)
            } else opts
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
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
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── GRADIENT HEADER ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                "✨ New Task",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "What do you want to accomplish?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                                .size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── TITLE INPUT ──────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    "Task title",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value       = title,
                    onValueChange = { title = it; titleError = false },
                    modifier    = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Review project proposal", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    isError     = titleError,
                    supportingText = if (titleError) ({ Text("Title can't be empty", color = MaterialTheme.colorScheme.error) }) else null,
                    singleLine  = true,
                    shape       = RoundedCornerShape(16.dp),
                    textStyle   = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    colors      = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── DESCRIPTION INPUT ────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    "Notes (optional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value       = description,
                    onValueChange = { description = it },
                    modifier    = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add any details…", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    minLines    = 2,
                    maxLines    = 4,
                    shape       = RoundedCornerShape(16.dp),
                    colors      = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── PRIORITY SECTION ─────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    "Priority",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.entries.forEach { p ->
                        PriorityPill(
                            priority   = p,
                            isSelected = priority == p,
                            onClick    = { priority = p },
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── DUE DATE SECTION ─────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    "Due Date",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateOptions.forEach { opt ->
                        DatePill(
                            label      = opt.label,
                            isSelected = dueDateMillis == opt.millis,
                            onClick    = { dueDateMillis = opt.millis },
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── CATEGORY SECTION ─────────────────────────────────────
            if (categories.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        "Category",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick  = { selectedCategoryId = null },
                            label    = { Text("None", style = MaterialTheme.typography.labelMedium) },
                            shape    = RoundedCornerShape(12.dp)
                        )
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick  = { selectedCategoryId = cat.id },
                                label    = {
                                    Text(
                                        "${cat.icon} ${cat.name}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── CREATE BUTTON ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .clickable {
                        if (title.isBlank()) { titleError = true; return@clickable }
                        onConfirm(
                            PlannerIntent.CreateTask(
                                title         = title.trim(),
                                description   = description.trim().ifBlank { null },
                                priority      = priority,
                                categoryId    = selectedCategoryId,
                                dueDateMillis = dueDateMillis
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Create Task",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "→",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PRIORITY PILL
// ═══════════════════════════════════════════════════════════
@Composable
private fun PriorityPill(
    priority: TaskPriority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = priority.color()
    val priorityLabel = when (priority) {
        TaskPriority.LOW      -> "Low"
        TaskPriority.MEDIUM   -> "Med"
        TaskPriority.HIGH     -> "High"
        TaskPriority.CRITICAL -> "Crit"
    }

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) priorityColor.copy(alpha = 0.20f) else Color.Transparent,
        animationSpec = tween(200),
        label = "priBg_${priority.name}"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) priorityColor else priorityColor.copy(alpha = 0.28f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "priBorder_${priority.name}"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(priorityColor)
        )
        Text(
            priorityLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) priorityColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═══════════════════════════════════════════════════════════
// DATE PILL
// ═══════════════════════════════════════════════════════════
@Composable
private fun DatePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200),
        label = "datePillBg_$label"
    )
    val fg by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "datePillFg_$label"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = fg
        )
    }
}
