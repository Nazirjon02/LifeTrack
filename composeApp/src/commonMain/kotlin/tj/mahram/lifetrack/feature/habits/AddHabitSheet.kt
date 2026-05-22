package tj.mahram.lifetrack.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.HabitFrequency
import tj.mahram.lifetrack.ui.components.FieldLabel
import tj.mahram.lifetrack.ui.components.GradientButton
import tj.mahram.lifetrack.ui.components.SheetHandle
import tj.mahram.lifetrack.ui.components.SheetHeader
import tj.mahram.lifetrack.ui.components.SheetTextField
import tj.mahram.lifetrack.ui.components.brandHorizontalGradient
import tj.mahram.lifetrack.ui.components.glassSurface

private val HabitIcons = listOf("🏃", "💪", "📚", "🧘", "💧", "🥗", "😴", "🧠", "✍️", "🎯", "🎵", "🌿", "☀️", "🚴", "🏊")
private val HabitColorPairs = listOf(
    "#7C3AED" to Color(0xFF7C3AED),
    "#3B82F6" to Color(0xFF3B82F6),
    "#10B981" to Color(0xFF10B981),
    "#F59E0B" to Color(0xFFF59E0B),
    "#EF4444" to Color(0xFFEF4444),
    "#EC4899" to Color(0xFFEC4899),
    "#06B6D4" to Color(0xFF06B6D4),
    "#8B5CF6" to Color(0xFF8B5CF6)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitSheet(
    onDismiss: () -> Unit,
    onConfirm: (HabitsIntent) -> Unit
) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🏃") }
    var selectedColor by remember { mutableStateOf("#7C3AED") }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    var nameError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { SheetHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            SheetHeader(title = s.addHabitTitle, onClose = onDismiss)

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(s.addHabitFieldName)
                    SheetTextField(value = name, onValueChange = { name = it; nameError = false }, placeholder = s.addHabitFieldName, isError = nameError, errorText = if (nameError) s.addHabitErrorEmpty else null)
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldLabel(s.addHabitPickIcon)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.height(124.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(HabitIcons) { icon ->
                            val isSelected = selectedIcon == icon
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .then(if (isSelected) Modifier.background(brandHorizontalGradient()) else Modifier.glassSurface(shape = RoundedCornerShape(14.dp)))
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(icon, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldLabel(s.addHabitColorLabel)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HabitColorPairs.forEach { (hex, color) ->
                            val isSelected = selectedColor == hex
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(width = if (isSelected) 3.dp else 0.dp, color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent, shape = CircleShape)
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldLabel(s.addHabitFrequencyLabel)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(HabitFrequency.DAILY, HabitFrequency.WEEKDAYS, HabitFrequency.WEEKENDS).forEach { freq ->
                            val isSelected = selectedFrequency == freq
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(if (isSelected) Modifier.background(brandHorizontalGradient()) else Modifier.glassSurface(shape = RoundedCornerShape(12.dp)))
                                    .clickable { selectedFrequency = freq }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    s.habitFrequencyLabel(freq),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            GradientButton(
                text = s.addHabitCreateButton,
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onConfirm(HabitsIntent.CreateHabit(name.trim(), selectedIcon, selectedColor, selectedFrequency))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                height = 56.dp
            )
        }
    }
}
