package tj.mahram.lifetrack.feature.problems

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.core.util.formatDate
import tj.mahram.lifetrack.core.util.formatDateTime
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.domain.model.ProblemHistoryEntry
import tj.mahram.lifetrack.domain.model.ProblemStatus
import tj.mahram.lifetrack.ui.components.SheetHandle
import tj.mahram.lifetrack.ui.components.SheetHeader
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.theme.appColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDetailSheet(
    problem: Problem,
    history: List<ProblemHistoryEntry>,
    onDismiss: () -> Unit,
    onIntent: (ProblemsIntent) -> Unit
) {
    val s = LocalStrings.current
    val accent = statusColor(problem.status)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { SheetHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp)) {
            SheetHeader(title = problem.title, subtitle = null, onClose = onDismiss)
            Spacer(Modifier.height(18.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Status + priority + category chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(text = s.problemStatusLabel(problem.status), color = accent)
                    Chip(text = s.problemPriorityName(problem.priority), color = priorityColor(problem.priority))
                    if (problem.category.isNotBlank()) Chip(text = problem.category, color = MaterialTheme.colorScheme.primary)
                }

                // Progress
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(s.problemProgressLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${problem.progress}%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = accent)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(accent.copy(alpha = 0.15f))) {
                        Box(modifier = Modifier.fillMaxWidth(problem.progressFraction).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.6f)))))
                    }
                }

                problem.dueDate?.let {
                    DetailField(s.problemDueLabel, it.formatDate())
                }
                problem.description?.let { DetailField(s.addProblemFieldDescription, it) }
                problem.solutions?.let { DetailField(s.problemFieldSolutions, it) }
                problem.actionPlan?.let { DetailField(s.problemFieldActionPlan, it) }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DetailButton(text = s.problemUpdateProgress, color = accent, modifier = Modifier.weight(1f)) { onIntent(ProblemsIntent.ShowProgress(problem)) }
                    if (problem.status != ProblemStatus.RESOLVED) {
                        DetailButton(text = s.problemMarkResolved, color = MaterialTheme.appColors.success, filled = true, modifier = Modifier.weight(1f)) {
                            onIntent(ProblemsIntent.SetStatus(problem, ProblemStatus.RESOLVED))
                        }
                    } else {
                        DetailButton(text = s.problemReopen, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f)) {
                            onIntent(ProblemsIntent.SetStatus(problem, ProblemStatus.IN_PROGRESS))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DetailButton(text = s.problemEditAction, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f)) { onIntent(ProblemsIntent.ShowEdit(problem)) }
                    DetailButton(text = s.problemDeleteAction, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f)) {
                        onIntent(ProblemsIntent.Delete(problem.id)); onDismiss()
                    }
                }

                // Change history
                Text(s.problemHistoryTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 4.dp))
                if (history.isEmpty()) {
                    Text(s.problemNoHistory, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        history.forEach { entry -> HistoryRow(entry) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(modifier = Modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(14.dp)).padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DetailButton(text: String, color: Color, modifier: Modifier = Modifier, filled: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(14.dp))
            .then(if (filled) Modifier.background(color) else Modifier.background(color.copy(alpha = 0.14f)))
            .clickable(onClick = onClick).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (filled) Color.White else color, maxLines = 1)
    }
}

@Composable
private fun HistoryRow(entry: ProblemHistoryEntry) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(entry.createdAt.formatDateTime(), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
