package org.montigny.client.ui.users


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MoodBad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.Theme.Danger
import org.montigny.client.ui.Theme.TextMuted
import org.montigny.client.ui.organizations.DashboardCard
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.ui.work.EmptyListPlaceHolder
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import java.util.*


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
    val totalPages by userViewModel.totalPages.collectAsState()
    var userToDelete by remember { mutableStateOf<UUID?>(null) }

    val currentPage by  userViewModel.currentPage.collectAsState()
    val pageSize = DEFAULT_PAGE_SIZE
    val scope = rememberCoroutineScope()

    var showWorkerDropDown by remember {mutableStateOf(false)}
    LaunchedEffect(currentPage) {
        userViewModel.populateUsersExternal(pageSize)
    }
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

            if (showWorkerDropDown) {
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

                        DashboardCard("Total Users", totalUsers, Icons.Outlined.Group)
                        DashboardCard("Approved Users", approvedUsers, Icons.Outlined.Done)
                        DashboardCard("Not Approved", notApproved, Icons.Outlined.MoodBad)
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
                                if (isNetworkAvailable && !isOnlineLoggedIn) {
                                    navigator.navigateTo(Destination.Login, Destination.Users)
                                }
                                userViewModel.synchronizeUserData()
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
                                Icon(
                                    Icons.Default.Refresh, contentDescription = "Sync Users",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync")
                            }
                        }
                        Button(
                            onClick = { navigator.navigateTo(Destination.CreateAccount) }
                        ) {
                            Icon(
                                Icons.Default.Add, contentDescription = "Create User",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create")
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                androidx.compose.material3.Icon(
                    if (showWorkerDropDown)
                        Icons.Default.ExpandLess
                    else
                        Icons.Default.ExpandMore,
                    contentDescription = "Opens worker Select",
                    modifier = Modifier.size(36.dp).clickable {
                        showWorkerDropDown = !showWorkerDropDown
                        if (!showWorkerDropDown) {
                            userViewModel.populateUsersExternal(DEFAULT_PAGE_SIZE)
                        }

                    }
                )
            }

            if (isNetworkAvailable && isOnlineLoggedIn) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Divider
                Divider(
                    modifier = Modifier
                        .weight(1f) // Takes up remaining space
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    thickness = 1.dp
                )

                // Search IconButton
                IconButton(
                    onClick = {navigator.navigateTo(Destination.FilterUsers)}
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Users",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Right Divider
                Divider(
                    modifier = Modifier
                        .weight(1f) // Takes up remaining space
                        .padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
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

                if (users.isEmpty() && isSyncing) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                }
                }
                if (users.isEmpty() && !isSyncing) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyListPlaceHolder("no users available",50 , Icons.Filled.WarningAmber)

                    }

                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(users) {index, user ->
                            UserRow(
                                user = user,
                                onEdit = {
                                    userViewModel.resetUpdateUserState()
                                    navigator.navigateTo(Destination.EditUser(user.id.toString()))
                                },
                                {
                                    navigator.navigateTo(
                                        Destination.ViewUserWork(user.id.toString()),
                                        Destination.Users
                                    )
                                },
                                onDelete = { userToDelete = user.id },
                                isDeleting = isDeleting_
                            )
                            val hasMorePages = currentPage < totalPages
                            if (index >= users.lastIndex - 2 && hasMorePages) {
                                scope.launch {userViewModel.loadNextPage(pageSize = pageSize)
                                }
                            }
                        }
                        item{
                            if (currentPage < totalPages) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
                            } else {
                                Text("End of Records", modifier = Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.labelMedium)
                            }
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
    moveToWork: (userId: String) -> Unit,
    onDelete: (userId: UUID) -> Unit,
    isDeleting: Boolean
) {
    val isUserActive = user.isActive ?: false
    val isUserApproved = user.isApproved ?: false

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- User Info Column ---
            Column(Modifier.weight(1f)) {
                // 1. Name and Surname
                Text(
                    "${user.name} ${user.surname}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                // 2. Contact and Role
                Text("📧 ${user.email}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Text("📱 ${user.phoneNumber}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Text("Role: ${user.userType?.displayName ?: "NONE"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("Department: ${user.department?.displayName ?: "NONE"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("Section: ${user.section?.displayName ?: "NONE"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                // 3. Status Icons Row (Cleaner Feel)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // isActive Status
                    StatusPill(
                        isSet = isUserActive,
                        label = "Active",
                        activeIcon = Icons.Default.CheckCircle,
                        inactiveIcon = Icons.Default.Cancel
                    )
                    // isApproved Status
                    StatusPill(
                        isSet = isUserApproved,
                        label = "Approved",
                        activeIcon = Icons.Default.VerifiedUser,
                        inactiveIcon = Icons.Default.HourglassEmpty
                    )
                }
            }

            // --- Action Buttons Column ---
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View Work Button
                Button(
                    onClick = { moveToWork(user.id.toString()) },
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Default.Work, contentDescription = "View work", Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Work")
                }

                // Edit Button
                OutlinedButton(
                    onClick = { onEdit(user.id.toString()) },
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }

                // Delete Button
                Button(
                    onClick = { onDelete(user.id!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    enabled = !isDeleting,
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

// Helper Composable for clean status display (Pill/Chip)
@Composable
private fun StatusPill(
    isSet: Boolean,
    label: String,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val color = if (isSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val icon = if (isSet) activeIcon else inactiveIcon
    val text = if (isSet) label else "Not $label"

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = color
        )
    }
}