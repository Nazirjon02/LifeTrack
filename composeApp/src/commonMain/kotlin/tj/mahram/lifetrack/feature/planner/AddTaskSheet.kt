package tj.mahram.lifetrack.feature.planner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.ui.components.FieldLabel
import tj.mahram.lifetrack.ui.components.GradientButton
import tj.mahram.lifetrack.ui.components.SheetHandle
import tj.mahram.lifetrack.ui.components.SheetHeader
import tj.mahram.lifetrack.ui.components.SheetTextField
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.glassColor
import tj.mahram.lifetrack.ui.components.glassSurface

private data class DateOption(val label: String, val millis: Long?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    categories: List<Category>,
    initialDueDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (PlannerIntent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var dueDateMillis by remember { mutableStateOf(initialDueDateMillis) }
    var titleError by remember { mutableStateOf(false) }

    val now = remember { Clock.System.now().toEpochMilliseconds() }
    val dayMs = 24 * 60 * 60 * 1000L
    val dateOptions = remember(initialDueDateMillis) {
        listOf(
            DateOption("None", null),
            DateOption("Today", now),
            DateOption("Tomorrow", now + dayMs),
            DateOption("Next Week", now + 7 * dayMs)
        ).let { opts ->
            if (initialDueDateMillis != null && opts.none { it.millis == initialDueDateMillis }) {
                listOf(DateOption("Selected", initialDueDateMillis)) + opts.drop(1)
            } else opts
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { SheetHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp)) {
            SheetHeader(title = "✨ New Task", subtitle = "What do you want to accomplish?", onClose = onDismiss)

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Task title")
                SheetTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    placeholder = "e.g. Review project proposal",
                    isError = titleError,
                    errorText = "Title can't be empty"
                )
            }

            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Notes (optional)")
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add any details…", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    minLines = 2, maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FieldLabel("Priority")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { p ->
                        PriorityPill(priority = p, isSelected = priority == p, onClick = { priority = p }, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FieldLabel("Due Date")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dateOptions.forEach { opt ->
                        DatePill(label = opt.label, isSelected = dueDateMillis == opt.millis, onClick = { dueDateMillis = opt.millis }, modifier = Modifier.weight(1f))
                    }
                }
            }

            if (categories.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldLabel("Category")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DatePill(label = "None", isSelected = selectedCategoryId == null, onClick = { selectedCategoryId = null }, modifier = Modifier.weight(1f))
                        categories.take(3).forEach { cat ->
                            DatePill(label = "${cat.icon} ${cat.name}", isSelected = selectedCategoryId == cat.id, onClick = { selectedCategoryId = cat.id }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            GradientButton(
                text = "Create Task",
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onConfirm(
                            PlannerIntent.CreateTask(
                                title = title.trim(),
                                description = description.trim().ifBlank { null },
                                priority = priority,
                                categoryId = selectedCategoryId,
                                dueDateMillis = dueDateMillis
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                height = 56.dp
            )
        }
    }
}

@Composable
private fun PriorityPill(priority: TaskPriority, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val priorityColor = priority.glassColor()
    val priorityLabel = when (priority) {
        TaskPriority.LOW -> "Low"
        TaskPriority.MEDIUM -> "Med"
        TaskPriority.HIGH -> "High"
        TaskPriority.CRITICAL -> "Crit"
    }
    val bgColor by animateColorAsState(targetValue = if (isSelected) priorityColor.copy(alpha = 0.2f) else Color.Transparent, animationSpec = tween(200), label = "priBg_${priority.name}")
    val borderColor by animateColorAsState(targetValue = if (isSelected) priorityColor else priorityColor.copy(alpha = 0.3f), animationSpec = tween(200), label = "priBorder_${priority.name}")

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(width = if (isSelected) 1.5.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(priorityColor))
        Text(
            priorityLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) priorityColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DatePill(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val brand = brandHorizontalGradient()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(if (isSelected) Modifier.background(brand, RoundedCornerShape(12.dp)) else Modifier.glassSurface(shape = RoundedCornerShape(12.dp)))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
