package org.montigny.client.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.Alignment
import org.koin.compose.koinInject
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionManager
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.common.StatusDot
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.slf4j.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNavItem(
    sessionManager: SessionManager,
    offlineSessionManager: OfflineSessionManager,
    navigator: Navigator
) {
    val authViewModel: AuthViewModel = koinInject()
    var expanded by remember { mutableStateOf(false) }
    val isOnlineLoggedIn by sessionManager.isLoggedInFlow.collectAsState()
    val isOfflineLoggedIn by offlineSessionManager.isLoggedInFlow.collectAsState()
    val logger: Logger = koinInject()

    val isLoggedIn = isOnlineLoggedIn || isOfflineLoggedIn
    val isOfflineMode = isOfflineLoggedIn && !isOnlineLoggedIn

    val dotColor = when {
        isOnlineLoggedIn -> MaterialTheme.colorScheme.primary   // online = blue/green
        isOfflineLoggedIn -> MaterialTheme.colorScheme.tertiary // offline = orange/yellow
        else -> MaterialTheme.colorScheme.outline               // not logged in = gray
    }

    logger.info("Online=$isOnlineLoggedIn Offline=$isOfflineLoggedIn")

    NavigationRailItem(
        selected = false,
        onClick = { expanded = !expanded },
        icon = {
            Box {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
                StatusDot(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                    color = dotColor
                )
            }
        }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        offset = DpOffset(x = 80.dp, y = 0.dp)
    ) {
        if (!isLoggedIn) {
            DropdownMenuItem(
                text = { Text("Login") },
                onClick = {
                    expanded = false
                    authViewModel.resetState()
                    authViewModel.clearFields()
                    navigator.navigateTo(Destination.Login)
                }
            )
        } else {
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = "Logout") },
                text = { Text(if (isOfflineMode) "Logout (Offline)" else "Logout") },
                onClick = {
                    /*if (isOnlineLoggedIn) sessionManager.logout()
                    if (isOfflineLoggedIn) offlineSessionManager.logout()*/
                    sessionManager.logout()
                    offlineSessionManager.logout()
                    expanded = false
                    authViewModel.resetState()
                    authViewModel.clearFields()
                    navigator.navigateTo(Destination.Login)
                }
            )
        }

        DropdownMenuItem(
            text = { Text("Main Settings") },
            onClick = {
                expanded = false
                navigator.navigateTo(Destination.MainSettings)
            }
        )
        DropdownMenuItem(
            text = { Text("Manage Stacks") },
            onClick = {
                expanded = false
                navigator.navigateTo(Destination.ManageStacksScreen)
            }
        )
    }
}
