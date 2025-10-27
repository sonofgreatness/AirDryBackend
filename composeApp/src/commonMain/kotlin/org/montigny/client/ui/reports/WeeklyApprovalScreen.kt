package org.montigny.client.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.montigny.client.dto.WeeklyApprovalDTO
import org.montigny.client.ui.reports.viewModel.WeekApprovalsViewModel
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import java.util.UUID // Required for the dialog state

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyApprovalScreen(
    viewModel: WeekApprovalsViewModel
) {
    val weeklyApprovals by viewModel.weeklyApprovalsList.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val isUpdatingId by viewModel.isUpdatingId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // NEW STATES: For managing the confirmation dialog and notes input
    var pendingUpdate by remember { mutableStateOf<Pair<UUID, String>?>(null) } // Pair: <ID, Status>
    var currentNotes by remember { mutableStateOf<String>("") }

    // Load initial data and observe snackbar events
    LaunchedEffect(Unit) {
        viewModel.populateWeeklyApprovals(DEFAULT_PAGE_SIZE)

        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Weekly Approval Management") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (weeklyApprovals.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No weekly approval records found.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(weeklyApprovals) { index, approval ->
                        WeeklyApprovalCard(
                            approval = approval,
                            isUpdating = isUpdatingId == approval.id,
                            // MODIFIED: Set pendingUpdate state to show the dialog
                            onApprove = { pendingUpdate = Pair(approval.id, "APPROVED") },
                            onRevoke = { pendingUpdate = Pair(approval.id, "REVOKED") }
                        )

                        // Infinite scrolling logic
                        val hasMorePages = viewModel.currentPage.value < totalPages
                        if (index >= weeklyApprovals.lastIndex - 2 && hasMorePages) {
                            SideEffect {
                                scope.launch {
                                    viewModel.loadNextPage(DEFAULT_PAGE_SIZE)
                                }
                            }
                        }
                    }

                    item {
                        if (viewModel.currentPage.value < totalPages) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
                        } else {
                            Text(
                                "End of Records",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
    // Status Confirmation/Notes Dialog
    pendingUpdate?.let { (id, status) ->
        val title = if (status == "APPROVED") "Approve Weekly Records?" else "Revoke Weekly Records?"
        val confirmText = if (status == "APPROVED") "Approve" else "Revoke"
        val containerColor = if (status == "APPROVED") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        val contentColor = if (status == "APPROVED") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

        AlertDialog(
            onDismissRequest = {
                pendingUpdate = null
                currentNotes = ""
            },
            title = {
                Text(title)
            },
            text = {
                Column {
                    Text(
                        "Please confirm the action. Add notes below (optional for Approve, recommended for Revoke):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = currentNotes,
                        onValueChange = { currentNotes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Pass notes to the ViewModel
                        viewModel.updateApprovalStatus(id, status, currentNotes.takeIf { it.isNotBlank() })
                        pendingUpdate = null
                        currentNotes = ""
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingUpdate = null; currentNotes = "" }) {
                    Text("Cancel")
                }
            },
            containerColor = containerColor
        )
    }
}

// --------------------------------------------------------------------------
// Helper Composable for the List Item
// --------------------------------------------------------------------------

@Composable
fun WeeklyApprovalCard(
    approval: WeeklyApprovalDTO,
    isUpdating: Boolean,
    onApprove: () -> Unit,
    onRevoke: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = getCardColorByStatus(approval.status)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Details Column ---
            Column(Modifier.weight(1f)) {
                Text(
                    "Week: ${approval.weekStartDate} to ${approval.weekEndDate}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Status: ${approval.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                approval.approvedAt?.let {
                    Text(
                        "Approved: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                approval.notes?.let {
                    Text(
                        "Notes: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // --- Actions Row ---
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                // === APPROVE Button ===
                // Show if status is PENDING or REVOKED (Allows re-approval)
                if (approval.status == "PENDING" || approval.status == "REVOKED") {
                    Button(
                        onClick = onApprove,
                        enabled = !isUpdating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        ActionContent(isUpdating, "Approve", Icons.Default.Check)
                    }
                }

                // === REVOKE Button ===
                // Show if status is PENDING or APPROVED (Allows rejection or revocation)
                if (approval.status == "APPROVED" || approval.status == "PENDING") {
                    OutlinedButton(
                        onClick = onRevoke,
                        enabled = !isUpdating,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        // Border logic remains the same
                        border = if (isUpdating) {
                            ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                            )
                        } else {
                            ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(MaterialTheme.colorScheme.error)
                            )
                        }
                    ) {
                        ActionContent(isUpdating, "Revoke", Icons.Default.Refresh)
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// Small Helper Composables (ActionContent and getCardColorByStatus remain the same)
// --------------------------------------------------------------------------

@Composable
private fun ActionContent(isUpdating: Boolean, text: String, icon: ImageVector) {
    if (isUpdating) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = LocalContentColor.current
        )
    } else {
        Icon(icon, contentDescription = text, Modifier.size(20.dp))
        Spacer(Modifier.width(4.dp))
        Text(text)
    }
}

@Composable
private fun getCardColorByStatus(status: String): androidx.compose.ui.graphics.Color {
    return when (status) {
        "APPROVED" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        "REVOKED" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
        "PENDING" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
}