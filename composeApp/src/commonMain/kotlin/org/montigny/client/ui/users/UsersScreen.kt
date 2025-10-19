package org.montigny.client.ui.users


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MoodBad
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.Theme.Danger
import org.montigny.client.ui.Theme.Neutral
import org.montigny.client.ui.Theme.TextMuted
import org.montigny.client.ui.organizations.DashboardCard
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.util.UtilityFunctions.Companion.toStringCustom
import java.util.UUID


@Composable
fun UsersScreen(userViewModel: UserViewModel, navigator: Navigator) {
    val users by userViewModel.users.collectAsState()
    val listState = rememberLazyListState()
    val isSyncing by userViewModel.isSyncing.collectAsState()
    val isDeleting_ by userViewModel.isDeleting.collectAsState()
    val isNetworkAvailable by userViewModel.isNetworkAvailable.collectAsState()
    val isOnlineLoggedIn by userViewModel.isOnlineLoggedIn.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    println("Initial isSyncing ${isSyncing}")
    var userToDelete by remember { mutableStateOf<UUID?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
        // ------- DASHBOARD REGION -------
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
                val totalUsers = users.size
                val approvedUsers = users.count { it.isApproved }
                val notApproved = users.count { !it.isApproved }

                DashboardCard("Total Users", totalUsers,Icons.Outlined.Group)
                DashboardCard("Approved Users", approvedUsers,Icons.Outlined.Done)
                DashboardCard("Not Approved", notApproved,Icons.Outlined.MoodBad)
            }
        }
        // ------- ACTION REGION -------
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
                            navigator.navigateTo(Destination.Login, Destination.Users)
                        }
                        println(" at button click isSyncing ${isSyncing}")
                        userViewModel.synchronizeUserData()
                        println(" after button click isSyncing ${isSyncing}")
                              },
                    enabled = !isSyncing,
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing")
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Users",
                            tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync")
                    }
                }
                Button(
                    onClick = { navigator.navigateTo(Destination.CreateAccount) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create User",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create")
                }
            }
        }
        // ------- LIST REGION -------
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                if (users.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No users available",
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
                        items(users, key = { it.id.toString() }) { user ->
                            UserRow(
                                user = user,
                                onEdit = {
                                    userViewModel.resetUpdateUserState()
                                    navigator.navigateTo(Destination.EditUser(user.id.toString()))
                                },
                                onDelete = { userToDelete = user.id },
                                isDeleting = isDeleting_
                            )
                        }
                    }
                }
         }
    }

}
// Deletion confirmation dialog
if (userToDelete != null) {
    AlertDialog(
        onDismissRequest = { userToDelete = null },
        confirmButton = {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        userViewModel.deleteUserById(userToDelete!!)
                        userToDelete = null
                    }
                }
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = { userToDelete = null }) {
                Text("Cancel")
            }
        },
        title = { Text("Delete User") },
        text = { Text("Are you sure you want to delete this user? This action cannot be undone.") },
        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
    )
}

// Show backend error messages gracefully
val errorMessage by userViewModel.errorMessage.collectAsState()
if (!errorMessage.isNullOrEmpty()) {
    LaunchedEffect(errorMessage) {
        snackbarHostState.showSnackbar(
            message = errorMessage ?: "An unexpected error occurred.",
            duration = SnackbarDuration.Long
        )
        userViewModel.clearErrorMessage()
    }
}
}



@Composable
fun UserRow(
    user: UserResponse,
    onEdit: (userId: String) -> Unit,
    onDelete: (userId: UUID) -> Unit,
    isDeleting: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${user.name} ${user.surname}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text("📧 ${user.email}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Text("📱 ${user.phoneNumber}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Text("Role: ${user.userType?.displayName ?: "NONE"}", style = MaterialTheme.typography.bodySmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = { onEdit(user.id.toString()) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }

                Button(
                    onClick = { onDelete(user.id!!) },
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
                        Icon(Icons.Default.Delete, contentDescription = "Delete"
                        ,tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}