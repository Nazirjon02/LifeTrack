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
import androidx.compose.material.icons.outlined.DeleteOutline
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
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.formatDate
import tj.mahram.lifetrack.domain.model.Task
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.ui.theme.PriorityCritical
import tj.mahram.lifetrack.ui.theme.PriorityHigh
import tj.mahram.lifetrack.ui.theme.PriorityLow
import tj.mahram.lifetrack.ui.theme.PriorityMedium
import tj.mahram.lifetrack.ui.theme.appColors

@Composable
fun TaskCard(
    task: Task,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val priorityColor = task.priority.glassColor()

    val contentAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.45f else 1f,
        animationSpec = tween(350),
        label = "contentAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .glassSurface(
                shape = RoundedCornerShape(22.dp),
                glow = if (task.isCompleted) null else priorityColor
            )
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Priority accent stripe
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.verticalGradient(
                        if (task.isCompleted)
                            listOf(priorityColor.copy(alpha = 0.25f), priorityColor.copy(alpha = 0.1f))
                        else
                            listOf(priorityColor, priorityColor.copy(alpha = 0.5f))
                    )
                )
        )

        Spacer(Modifier.width(14.dp))

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (task.isCompleted) 0.4f else 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(9.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TagPill(
                    text = s.priorityLabel(task.priority),
                    color = priorityColor,
                    dim = task.isCompleted
                )
                task.dueDate?.let { date ->
                    val tz = TimeZone.currentSystemDefault()
                    val today = Clock.System.now().toLocalDateTime(tz).date
                    val taskDay = date.toLocalDateTime(tz).date
                    val todayDue = taskDay == today
                    val isOverdue = !task.isCompleted && taskDay < today
                    val dueColor = when {
                        isOverdue -> MaterialTheme.colorScheme.error
                        todayDue && !task.isCompleted -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    TagPill(
                        text = when {
                            isOverdue -> "Overdue"
                            todayDue -> "Today"
                            else -> date.formatDate()
                        },
                        color = dueColor,
                        dim = task.isCompleted && !isOverdue
                    )
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AnimatedCheckbox(
                checked = task.isCompleted,
                onCheckedChange = onToggle,
                color = priorityColor
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun TagPill(text: String, color: Color, dim: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = if (dim) 0.08f else 0.16f))
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color.copy(alpha = if (dim) 0.6f else 1f)
        )
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
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "cbScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (checked) color else Color.Transparent,
        animationSpec = tween(200), label = "cbBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) color else color.copy(alpha = 0.45f),
        animationSpec = tween(200), label = "cbBorder"
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
    val color = priority.glassColor()
    TagPill(text = s.priorityLabel(priority), color = color, dim = false)
}

@Composable
fun TaskPriority.glassColor(): Color {
    val c = MaterialTheme.appColors
    return when (this) {
        TaskPriority.LOW -> c.priorityLow
        TaskPriority.MEDIUM -> c.priorityMedium
        TaskPriority.HIGH -> c.priorityHigh
        TaskPriority.CRITICAL -> c.priorityCritical
    }
}

fun TaskPriority.color(): Color = when (this) {
    TaskPriority.LOW -> PriorityLow
    TaskPriority.MEDIUM -> PriorityMedium
    TaskPriority.HIGH -> PriorityHigh
    TaskPriority.CRITICAL -> PriorityCritical
}
