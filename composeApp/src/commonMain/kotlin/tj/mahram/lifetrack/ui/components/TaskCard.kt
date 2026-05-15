package tj.mahram.lifetrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.formatDate
import tj.mahram.lifetrack.core.util.isToday
import tj.mahram.lifetrack.domain.model.Task
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.ui.theme.PriorityCritical
import tj.mahram.lifetrack.ui.theme.PriorityHigh
import tj.mahram.lifetrack.ui.theme.PriorityLow
import tj.mahram.lifetrack.ui.theme.PriorityMedium

@Composable
fun TaskCard(
    task: Task,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val priorityColor = task.priority.color()

    val bgColor by animateColorAsState(
        targetValue = if (task.isCompleted)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(350),
        label = "taskBg"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.45f else 1f,
        animationSpec = tween(350),
        label = "contentAlpha"
    )
    val elevation by animateFloatAsState(
        targetValue = if (task.isCompleted) 0f else 2f,
        label = "elevation"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority gradient left stripe
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = if (task.isCompleted)
                                listOf(priorityColor.copy(alpha = 0.2f), priorityColor.copy(alpha = 0.1f))
                            else
                                listOf(priorityColor, priorityColor.copy(alpha = 0.6f))
                        )
                    )
            )

            Spacer(Modifier.width(14.dp))

            // Task content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!task.description.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (task.isCompleted) 0.35f else 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = if (task.isCompleted) 0.07f else 0.14f)
                    ) {
                        Text(
                            text = s.priorityLabel(task.priority),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor.copy(alpha = contentAlpha)
                        )
                    }
                    // Due date badge
                    task.dueDate?.let { date ->
                        val todayDue = date.isToday()
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                todayDue && !task.isCompleted -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                                else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            }
                        ) {
                            Text(
                                text = if (todayDue) "Today" else date.formatDate(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    todayDue && !task.isCompleted -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // Right side: animated checkbox + delete
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnimatedCheckbox(
                    checked = task.isCompleted,
                    onCheckedChange = onToggle,
                    color = priorityColor
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.45f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cbScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (checked) color else Color.Transparent,
        animationSpec = tween(200),
        label = "cbBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) color else color.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "cbBorder"
    )

    Box(
        modifier = modifier
            .size(28.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor)
            .border(width = 2.dp, color = borderColor, shape = CircleShape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun PriorityChip(priority: TaskPriority) {
    val s = LocalStrings.current
    val color = priority.color()
    SuggestionChip(
        onClick = {},
        label = {
            Text(
                s.priorityLabel(priority),
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.13f)
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = color.copy(alpha = 0.35f)
        )
    )
}

fun TaskPriority.color(): Color = when (this) {
    TaskPriority.LOW      -> PriorityLow
    TaskPriority.MEDIUM   -> PriorityMedium
    TaskPriority.HIGH     -> PriorityHigh
    TaskPriority.CRITICAL -> PriorityCritical
}
