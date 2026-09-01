package tj.mahram.lifetrack.feature.problems

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.ProblemStatus
import tj.mahram.lifetrack.domain.model.Task
import tj.mahram.lifetrack.ui.components.glassSurface
import tj.mahram.lifetrack.ui.theme.appColors

@Composable
fun ProblemsCalendarView(state: ProblemsState, onIntent: (ProblemsIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp).padding(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CalendarModeSwitch(state.calendarMode) { onIntent(ProblemsIntent.SetCalendarMode(it)) }
        when (state.calendarMode) {
            CalendarMode.MONTH -> MonthGrid(state, onIntent)
            CalendarMode.WEEK -> WeekStrip(state, onIntent)
            CalendarMode.DAY -> {}
        }
        DayPanel(state, onIntent)
    }
}

@Composable
private fun CalendarModeSwitch(mode: CalendarMode, onSelect: (CalendarMode) -> Unit) {
    val s = LocalStrings.current
    val items = listOf(
        CalendarMode.MONTH to s.calendarMonth,
        CalendarMode.WEEK to s.calendarWeek,
        CalendarMode.DAY to s.calendarDay
    )
    Row(
        modifier = Modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(16.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { (m, label) ->
            val sel = m == mode
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .then(if (sel) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) else Modifier)
                    .clickable { onSelect(m) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MonthGrid(state: ProblemsState, onIntent: (ProblemsIntent) -> Unit) {
    val s = LocalStrings.current
    val year = state.selectedYear
    val month = state.selectedMonth
    val firstOfMonth = LocalDate(year, month, 1)
    val leading = firstOfMonth.dayOfWeek.isoDayNumber - 1
    val daysInMonth = firstOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).day

    Column(modifier = Modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(22.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Month header with navigation
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = {
                val (py, pm) = if (month == 1) year - 1 to 12 else year to month - 1
                onIntent(ProblemsIntent.NavigateMonth(py, pm))
            }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
            Text(
                "${s.plannerMonthFullNames[month - 1]} $year",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = {
                val (ny, nm) = if (month == 12) year + 1 to 1 else year to month + 1
                onIntent(ProblemsIntent.NavigateMonth(ny, nm))
            }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
        }

        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth()) {
            s.plannerWeekDayHeaders.forEach { d ->
                Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Cells
        val cells = buildList {
            repeat(leading) { add(null) }
            for (d in 1..daysInMonth) add(d)
        }
        cells.chunked(7).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { day ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (day != null) DayCell(state, year, month, day, onIntent)
                    }
                }
                repeat(7 - row.size) { Box(modifier = Modifier.weight(1f)) {} }
            }
        }
    }
}

@Composable
private fun DayCell(state: ProblemsState, year: Int, month: Int, day: Int, onIntent: (ProblemsIntent) -> Unit) {
    val date = LocalDate(year, month, day)
    val isToday = date == state.todayDate
    val isSelected = date == state.selectedDate
    val problemCount = state.problemCountForDay(date)
    val taskCount = state.taskCountForDay(date)
    val c = MaterialTheme.appColors

    Column(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp))
            .then(
                when {
                    isSelected -> Modifier.background(MaterialTheme.colorScheme.primary)
                    isToday -> Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                    else -> Modifier
                }
            )
            .clickable { onIntent(ProblemsIntent.SelectDay(year, month, day)) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "$day",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
        if (problemCount > 0 || taskCount > 0) {
            Spacer(Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (problemCount > 0) Box(Modifier.size(5.dp).clip(CircleShape).background(if (isSelected) Color.White else c.danger))
                if (taskCount > 0) Box(Modifier.size(5.dp).clip(CircleShape).background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary))
            }
        }
    }
}

@Composable
private fun WeekStrip(state: ProblemsState, onIntent: (ProblemsIntent) -> Unit) {
    val s = LocalStrings.current
    val selected = state.selectedDate
    val weekStart = selected.minus(selected.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    Row(modifier = Modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(20.dp)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0..6) {
            val date = weekStart.plus(i, DateTimeUnit.DAY)
            val isSel = date == state.selectedDate
            val isToday = date == state.todayDate
            val pc = state.problemCountForDay(date)
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                    .then(if (isSel) Modifier.background(MaterialTheme.colorScheme.primary) else if (isToday) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) else Modifier)
                    .clickable { onIntent(ProblemsIntent.SelectDay(date.year, date.month.number, date.day)) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(s.plannerWeekDayHeaders[i], style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = if (isSel) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${date.day}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                Box(Modifier.size(5.dp).clip(CircleShape).background(if (pc > 0) (if (isSel) Color.White else MaterialTheme.appColors.danger) else Color.Transparent))
            }
        }
    }
}

@Composable
private fun DayPanel(state: ProblemsState, onIntent: (ProblemsIntent) -> Unit) {
    val s = LocalStrings.current
    val date = state.selectedDate
    val problems = state.problemsForDay(date)
    val tasks = state.tasksForDay(date)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "${date.day} ${s.plannerMonthFullNames[date.month.number - 1]} · ${s.plannerDayOfWeekNames[date.dayOfWeek.isoDayNumber - 1]}",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
        )
        if (problems.isEmpty() && tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(18.dp)).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(s.calendarNothing, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        } else {
            if (problems.isNotEmpty()) {
                Text(s.calendarProblemsLabel(problems.size), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.danger)
                problems.forEach { p -> ProblemCard(problem = p, onIntent = onIntent) }
            }
            if (tasks.isNotEmpty()) {
                Text(s.calendarTasksLabel(tasks.size), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                tasks.forEach { t -> CalendarTaskRow(t) }
            }
        }
    }
}

@Composable
private fun CalendarTaskRow(task: Task) {
    Row(
        modifier = Modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(14.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(if (task.isCompleted) "✅" else "⬜", fontSize = 16.sp)
        Text(
            task.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
