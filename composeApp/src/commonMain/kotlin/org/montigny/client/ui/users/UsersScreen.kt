package org.montigny.client.ui.users


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.stylusHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.koinInject
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.ui.Theme.Danger
import org.montigny.client.ui.Theme.Info
import org.montigny.client.ui.Theme.Merriweather
import org.montigny.client.ui.Theme.Neutral
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import org.montigny.client.ui.Theme.TextMuted
import org.montigny.client.ui.Theme.TimberTypography
import org.montigny.client.ui.users.viewModels.UserViewModel




@Composable
fun UsersScreen(userViewModel: UserViewModel) {
    val users by userViewModel.users.collectAsState()
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ------- DASHBOARD REGION (unchanged) -------
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

                DashboardCard("Total Users", totalUsers)
                DashboardCard("Approved Users", approvedUsers)
                DashboardCard("Not Approved", notApproved)
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
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { userViewModel.populateUsers() }) {
                    Text("Create New User")
                }
            }
        }

        // ------- LIST REGION (takes remaining space and scrolls) -------
        Surface(
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // important: gives the list a constrained height so LazyColumn can scroll
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items = users, key = { it.id.toString() }) { user ->
                    UserRow(
                        user = user,
                        onEdit = { /* TODO */ },
                        onDelete = { /* TODO */ }
                    )
                }
            }
        }
    }
}


@Composable
fun DashboardCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Outlined.Group, // default user-group icon
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
    }
) {
    Card(
        modifier = modifier
            .padding(12.dp)
            .size(160.dp),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(
            width = 4.dp,
            color = MaterialTheme.colorScheme.primary
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun UserRow(user: UserResponse, onEdit: () -> Unit, onDelete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${user.name} ${user.surname}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text("📧 ${user.email}", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                Text("📱 ${user.phoneNumber}", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                Text("Role: ${user.userType?.displayName ?: "NONE"}", style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = Neutral)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
        Divider(color = TextMuted.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(top = 8.dp))
    }
}