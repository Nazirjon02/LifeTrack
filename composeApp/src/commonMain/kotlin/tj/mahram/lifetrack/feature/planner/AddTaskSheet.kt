package tj.mahram.lifetrack.feature.planner

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.ui.components.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (PlannerIntent.CreateTask) -> Unit
) {
    val s = LocalStrings.current

    var title              by remember { mutableStateOf("") }
    var description        by remember { mutableStateOf("") }
    var priority           by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var dueDateMillis      by remember { mutableStateOf<Long?>(null) }
    var titleError         by remember { mutableStateOf(false) }

    val todayMillis    = remember { Clock.System.now().toEpochMilliseconds() }
    val tomorrowMillis = remember { todayMillis + 24 * 60 * 60 * 1000L }
    val weekMillis     = remember { todayMillis + 7  * 24 * 60 * 60 * 1000L }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        s.addTaskTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        s.addTaskSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text(s.addTaskFieldTitle) },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError,
                supportingText = if (titleError) ({ Text(s.addTaskErrorEmpty) }) else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(s.addTaskFieldNote) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Priority section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    s.addTaskPriority,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TaskPriority.entries.forEach { p ->
                        PriorityButton(
                            priority = p,
                            label = s.priorityLabel(p),
                            isSelected = priority == p,
                            onClick = { priority = p },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Due date section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    s.addTaskDueDate,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateChip(s.addTaskDueNone,     isSelected = dueDateMillis == null,          onClick = { dueDateMillis = null })
                    DateChip(s.addTaskDueToday,    isSelected = dueDateMillis == todayMillis,    onClick = { dueDateMillis = todayMillis })
                    DateChip(s.addTaskDueTomorrow, isSelected = dueDateMillis == tomorrowMillis, onClick = { dueDateMillis = tomorrowMillis })
                    DateChip(s.addTaskDueWeek,     isSelected = dueDateMillis == weekMillis,     onClick = { dueDateMillis = weekMillis })
                }
            }

            // Category section
            if (categories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        s.addTaskCategory,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null },
                            label = { Text(s.addTaskCategoryNone) }
                        )
                        categories.take(4).forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = { selectedCategoryId = cat.id },
                                label = {
                                    Text(
                                        "${cat.icon} ${cat.name}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Create button
            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    onConfirm(
                        PlannerIntent.CreateTask(
                            title = title.trim(),
                            description = description.trim().ifBlank { null },
                            priority = priority,
                            categoryId = selectedCategoryId,
                            dueDateMillis = dueDateMillis
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    s.addTaskButton,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PriorityButton(
    priority: TaskPriority,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = priority.color()
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) color.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(200),
        label = "priBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) color else color.copy(alpha = 0.25f),
        animationSpec = tween(200),
        label = "priBorder"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DateChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "dateBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "dateTxt"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}
