package org.montigny.client.ui.organizations
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.montigny.client.dto.Organization.OrganizationResponse
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.Theme.Danger
import org.montigny.client.ui.Theme.TextMuted
import org.montigny.client.ui.organizations.viewModels.OrganizationViewModel
import java.util.*

@Composable
fun OrganizationScreen(viewModel: OrganizationViewModel, navigator: Navigator) {
    val organizations by viewModel.organizations.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val isNetworkAvailable  by  viewModel.isNetworkAvailable.collectAsState()
    val isOnlineLoggedIn by  viewModel.isOnlineLoggedIn.collectAsState()
    var orgToDelete by remember { mutableStateOf<UUID?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ----- DASHBOARD -----
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val totalOrgs = organizations.size
                    val totalDepartments = organizations.sumOf {it.departments.size }
                    val totalSections = organizations.sumOf {it.departments.sumOf { d -> d.sections.size }}
                    DashboardCard("Organizations", totalOrgs, Icons.Outlined.Apartment)
                    DashboardCard("Departments", totalDepartments, Icons.Outlined.Groups)
                    DashboardCard("Sections", totalSections, Icons.Outlined.Layers)
                }
            }

            // ----- ACTIONS -----
            Surface(
                tonalElevation = 4.dp,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if(isNetworkAvailable && !isOnlineLoggedIn){
                                navigator.navigateTo(Destination.Login, Destination.Organizations)
                            }
                            viewModel.synchronizeOrganizations()},
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Syncing")
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync")
                            Spacer(Modifier.width(4.dp))
                            Text("Sync")
                        }
                    }
                    Button(onClick = { navigator.navigateTo(Destination.CreateOrganization) }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Org")
                        Spacer(Modifier.width(4.dp))
                        Text("Create")
                    }
                }
            }

            // ----- LIST -----
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                if (organizations.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No organizations available",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(organizations, key = { it.id!! }) { org ->
                            OrganizationRow(
                                organization = org,
                                onEdit = { navigator.navigateTo(Destination.EditOrganization(org.id.toString())) },
                                onDelete = { orgToDelete = org.id },
                                isDeleting = isDeleting,
                                navigator = navigator,
                                organizationViewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    // Deletion confirmation
    if (orgToDelete != null) {
        AlertDialog(
            onDismissRequest = { orgToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteOrganization(orgToDelete!!)
                            orgToDelete = null
                        }
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { orgToDelete = null }) { Text("Cancel") }
            },
            title = { Text("Delete Organization") },
            text = { Text("Are you sure you want to delete this organization? This cannot be undone.") },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
        )
    }

    val errorMessage by viewModel.errorMessage.collectAsState()
    if (!errorMessage.isNullOrEmpty()) {
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(message = errorMessage ?: "Error", duration = SnackbarDuration.Long)
            viewModel.clearErrorMessage()
        }
    }
}

@Composable
fun OrganizationRow(
    organization: OrganizationResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDeleting: Boolean,
    navigator: Navigator,
    organizationViewModel: OrganizationViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Organization Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    organization.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Departments: ${organization.departments.size}",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Sections: ${organization.departments.sumOf { it.sections.size }}",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(6.dp))
                OrganizationLogo(organization.logo)
            }

            // Middle Column: View Departments Button
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                ElevatedButton(
                    onClick = {
                        val userId = organizationViewModel.getUserId()
                        if (userId == null) {
                            navigator.navigateTo(Destination.Login, Destination.Organizations)
                        } else {
                            navigator.navigateTo(
                                Destination.ViewOrgWithDepartmentsAndSections(userId = userId)
                            )
                        }
                    },
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View departments",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("View Departments")
                }
            }

            // Right Column: Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }

                Button(
                    onClick = onDelete,
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
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }

}