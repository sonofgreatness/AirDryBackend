package org.montigny.client.ui.settings.stacks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Groups
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
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.organizations.DashboardCard
import org.montigny.client.ui.organizations.DashboardCardWithAction
import org.montigny.client.ui.settings.stacks.viewModel.StacksViewModel
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import java.util.UUID

@Composable
fun ManageStacksScreen(viewModel: StacksViewModel, navigator: Navigator) {
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val stacks by viewModel.allStacks.collectAsState()
    val stackCodes by viewModel.allStackCodes.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val isOnlineLoggedIn by viewModel.isOnlineLoggedIn.collectAsState()
    var showWorkerDropDown by remember {mutableStateOf(false)}

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var stackToDelete by remember { mutableStateOf<UUID?>(null) }
    LaunchedEffect(Unit){
        viewModel.populateAllStacksList()

    }

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


            if (showWorkerDropDown){
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
                        val totalStack = stacks.size
                        val totalStackCodes = stackCodes.size
                        DashboardCard("Stacks", totalStack, Icons.Outlined.Apartment)
                        DashboardCardWithAction(
                            "Stack Codes", totalStackCodes, Icons.Outlined.Groups
                        ) {
                            navigator.navigateTo(Destination.ViewStackCodes)
                        }
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
                                if (isNetworkAvailable && !isOnlineLoggedIn) {
                                    navigator.navigateTo(Destination.Login, Destination.ManageStacksScreen)
                                }
                                viewModel.populateAllStacksList()
                            },
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
                        Button(onClick = { navigator.navigateTo(Destination.CreateStack) }) {
                            Icon(Icons.Default.Add, contentDescription = "Create Stack")
                            Spacer(Modifier.width(4.dp))
                            Text("Create")
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    if (showWorkerDropDown)
                        Icons.Default.ExpandLess
                    else
                        Icons.Default.ExpandMore
                    , contentDescription = "Opens worker Select"
                    , modifier = Modifier.size(36.dp).clickable{
                        showWorkerDropDown =!showWorkerDropDown
                        if (!showWorkerDropDown){
                            viewModel.populateAllStacksList()
                        }

                    }
                )
            }

            // ----- LIST -----
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                if (stacks.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No stacks available",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    val listState = rememberLazyListState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // --- TABLE HEADER ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Name", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Length (m)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Height (m)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Thick (m)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Pieces", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Volume", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(stacks, key = { it.id!!}) { stack ->
                                StackRow(
                                    stack = stack,
                                    onEdit = { id ->
                                        viewModel.resetUpdateState()
                                        viewModel.initializeStack(id.toUUID())
                                        navigator.navigateTo(Destination.EditStack(id)) },
                                    onDelete = { stackToDelete = stack.stackCode.id },
                                    isDeleting = isDeleting,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    // Delete confirmation
    if (stackToDelete != null) {
        AlertDialog(
            onDismissRequest = { stackToDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        if (isNetworkAvailable && isOnlineLoggedIn) {
                            viewModel.deleteStack(stackToDelete!!.toString())
                            stackToDelete = null
                        }else{
                            navigator.navigateTo(Destination.Login, Destination.ManageStacksScreen)
                        }
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { stackToDelete = null }) { Text("Cancel") } },
            title = { Text("Delete Stack") },
            text = { Text("Are you sure you want to delete this stack? This cannot be undone.") },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
        )
    }
}
