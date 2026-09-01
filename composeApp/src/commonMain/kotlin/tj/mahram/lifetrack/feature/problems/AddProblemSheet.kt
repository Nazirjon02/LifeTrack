package tj.mahram.lifetrack.feature.problems

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.sp
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.domain.model.ProblemPriority
import tj.mahram.lifetrack.ui.components.FieldLabel
import tj.mahram.lifetrack.ui.components.GradientButton
import tj.mahram.lifetrack.ui.components.SheetHandle
import tj.mahram.lifetrack.ui.components.SheetHeader
import tj.mahram.lifetrack.ui.components.SheetTextField
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.components.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProblemSheet(
    editing: Problem?,
    onDismiss: () -> Unit,
    onIntent: (ProblemsIntent) -> Unit
) {
    val s = LocalStrings.current
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    fun dayMillis(d: LocalDate) = d.atStartOfDayIn(tz).toEpochMilliseconds()

    var title by remember { mutableStateOf(editing?.title ?: "") }
    var description by remember { mutableStateOf(editing?.description ?: "") }
    var solutions by remember { mutableStateOf(editing?.solutions ?: "") }
    var actionPlan by remember { mutableStateOf(editing?.actionPlan ?: "") }
    var category by remember { mutableStateOf(editing?.category ?: "") }
    var priority by remember { mutableStateOf(editing?.priority ?: ProblemPriority.MEDIUM) }
    var color by remember { mutableStateOf(editing?.color ?: defaultColorForPriority(ProblemPriority.MEDIUM)) }
    var dueMillis by remember { mutableStateOf(editing?.dueDate?.toEpochMilliseconds()) }
    var titleError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { SheetHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp)) {
            SheetHeader(
                title = if (editing != null) s.editProblemTitle else s.addProblemTitle,
                subtitle = s.addProblemSubtitle,
                onClose = onDismiss
            )
            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FieldLabel(s.addProblemFieldTitle)
                SheetTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    placeholder = s.addProblemPlaceholderTitle,
                    isError = titleError,
                    errorText = if (titleError) s.addProblemErrorTitle else null
                )

                FieldLabel(s.addProblemFieldDescription)
                SheetTextField(value = description, onValueChange = { description = it }, placeholder = s.addProblemDescPlaceholder, singleLine = false)

                FieldLabel(s.problemFieldSolutions)
                SheetTextField(value = solutions, onValueChange = { solutions = it }, placeholder = s.addProblemSolutionsPlaceholder, singleLine = false)

                FieldLabel(s.problemFieldActionPlan)
                SheetTextField(value = actionPlan, onValueChange = { actionPlan = it }, placeholder = s.addProblemActionPlanPlaceholder, singleLine = false)

                FieldLabel(s.problemFieldCategory)
                SheetTextField(value = category, onValueChange = { category = it }, placeholder = s.problemFieldCategoryPlaceholder)

                // Priority selector
                FieldLabel(s.problemPriorityLabel)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ProblemPriority.entries.forEach { p ->
                        val pc = priorityColor(p)
                        val selected = p == priority
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                .background(if (selected) pc.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                .border(width = if (selected) 1.5.dp else 1.dp, color = if (selected) pc else Color.Transparent, shape = RoundedCornerShape(14.dp))
                                .clickable {
                                    priority = p
                                    // keep colour in sync only if the user hasn't overridden it
                                    if (editing == null) color = defaultColorForPriority(p)
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(s.problemPriorityName(p), style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) pc else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Target date quick pick (calendar link)
                FieldLabel(s.problemDueLabel)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DueChip(s.problemNoDue, dueMillis == null) { dueMillis = null }
                    DueChip(s.calendarToday, dueMillis == dayMillis(today)) { dueMillis = dayMillis(today) }
                    DueChip(s.addTaskDueTomorrow, dueMillis == dayMillis(today.plus(1, DateTimeUnit.DAY))) { dueMillis = dayMillis(today.plus(1, DateTimeUnit.DAY)) }
                    DueChip(s.addTaskDueWeek, dueMillis == dayMillis(today.plus(7, DateTimeUnit.DAY))) { dueMillis = dayMillis(today.plus(7, DateTimeUnit.DAY)) }
                }

                // Colour picker
                FieldLabel(s.addHabitColorLabel)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProblemColors.forEach { hex ->
                        val col = parseHexColor(hex)
                        val selected = hex == color
                        Box(
                            modifier = Modifier.size(38.dp).clip(CircleShape).background(col)
                                .then(if (selected) Modifier.border(2.5.dp, Color.White, CircleShape) else Modifier)
                                .clickable { color = hex },
                            contentAlignment = Alignment.Center
                        ) { if (selected) Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))
            GradientButton(
                text = if (editing != null) s.saveButton else s.addProblemCreateButton,
                onClick = {
                    if (title.isBlank()) { titleError = true; return@GradientButton }
                    if (editing != null) {
                        onIntent(
                            ProblemsIntent.Update(
                                original = editing,
                                title = title, description = description, solutions = solutions,
                                actionPlan = actionPlan, priority = priority, category = category,
                                color = color, dueDateMillis = dueMillis
                            )
                        )
                    } else {
                        onIntent(
                            ProblemsIntent.Create(
                                title = title, description = description, solutions = solutions,
                                actionPlan = actionPlan, priority = priority, category = category,
                                color = color, dueDateMillis = dueMillis
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
private fun DueChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
            .then(if (selected) Modifier.background(brandHorizontalGradient()) else Modifier.glassSurface(shape = RoundedCornerShape(12.dp)))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}
