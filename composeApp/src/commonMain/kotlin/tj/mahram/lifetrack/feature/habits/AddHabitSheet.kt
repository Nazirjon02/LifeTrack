package tj.mahram.lifetrack.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.HabitFrequency

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
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                s.addHabitTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text(s.addHabitFieldName) },
                isError = nameError,
                supportingText = if (nameError) { { Text(s.addHabitErrorEmpty) } } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Icon picker
            Text(s.addHabitPickIcon, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HabitIcons) { icon ->
                    val isSelected = selectedIcon == icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedIcon = icon },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icon, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // Color picker
            Text(s.addHabitColorLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HabitColorPairs.forEach { (hex, color) ->
                    val isSelected = selectedColor == hex
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = hex }
                    )
                }
            }

            // Frequency
            Text(s.addHabitFrequencyLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(HabitFrequency.DAILY, HabitFrequency.WEEKDAYS, HabitFrequency.WEEKENDS).forEach { freq ->
                    val isSelected = selectedFrequency == freq
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFrequency = freq },
                        label = { Text(s.habitFrequencyLabel(freq), style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    onConfirm(HabitsIntent.CreateHabit(name.trim(), selectedIcon, selectedColor, selectedFrequency))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(s.addHabitCreateButton, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
