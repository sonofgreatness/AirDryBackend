package org.montigny.client.ui.settings.stacks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.ui.Theme.Danger
import java.util.*

@Composable
fun StackRow(
    stack: GetStackDTO,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    isDeleting: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {

            // TABULAR ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stack.displayName, style = MaterialTheme.typography.bodyMedium)
                Text("${stack.length ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                Text("${stack.height ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                Text("${stack.thickness ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                Text("${stack.numberOfPieces ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                Text("${stack.volume ?: "-"}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))

            // ACTION BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = { onEdit(stack.id!!.toString()) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Stack", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {onDelete(stack.stackCode.id.toString())},
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}
