package tj.mahram.lifetrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.ui.theme.appColors

/**
 * "Delete this item?" dialog, styled like the wallet/investment delete dialogs.
 * Deleting a transaction, task, goal, debt, problem or reminder used to happen
 * instantly on one tap, with no way back.
 */
@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = { Text(s.deleteConfirmTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (itemName.isNotBlank()) {
                    Text(
                        itemName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(s.deleteConfirmMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(MaterialTheme.appColors.danger)
                    .clickable(onClick = onConfirm).padding(horizontal = 22.dp, vertical = 11.dp)
            ) { Text(s.deleteConfirmButton, color = Color.White, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancelButton, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

/** Asks for confirmation before running a delete; see [rememberDeleteConfirmation]. */
class DeleteConfirmation internal constructor(
    private val pending: MutableState<Pair<String, () -> Unit>?>
) {
    /** Shows the dialog for [itemName]; [delete] runs only if the user confirms. */
    fun request(itemName: String, delete: () -> Unit) {
        pending.value = itemName to delete
    }
}

/**
 * Call once at the top of a screen (or card) and route its delete actions
 * through `request(...)`. The dialog is shown from here while a request is
 * pending.
 */
@Composable
fun rememberDeleteConfirmation(): DeleteConfirmation {
    val pending = remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
    pending.value?.let { (name, delete) ->
        ConfirmDeleteDialog(
            itemName = name,
            onConfirm = {
                pending.value = null
                delete()
            },
            onDismiss = { pending.value = null }
        )
    }
    return remember(pending) { DeleteConfirmation(pending) }
}
