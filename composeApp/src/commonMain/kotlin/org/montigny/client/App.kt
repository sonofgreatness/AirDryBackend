package org.montigny.client

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionManager
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.NavGraph
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.Theme.Danger
import org.montigny.client.ui.Theme.Neutral
import org.montigny.client.ui.Theme.TimberTheme
import org.montigny.client.ui.settings.SettingsNavItem
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.util.ConnectivityObserver
import org.slf4j.Logger

@Composable
fun App(sessionManager: SessionManager, authViewModel: AuthViewModel,
        usersViewModel: UserViewModel, offlineSessionManager: OfflineSessionManager,
        connectivityObserver: ConnectivityObserver) {
    val isOffLineLoggedIn by offlineSessionManager.isLoggedInFlow.collectAsState(initial = false)
    val isOnlineLoggedIn by sessionManager.isLoggedInFlow.collectAsState(initial = false)
    val isNetworkAvailable by connectivityObserver.isServerReachable.collectAsState(initial = false)

    TimberTheme {
        val logger: Logger = koinInject()
        logger.info("Desktop app started ")

        val navigator = remember { Navigator() }

        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Spacer(Modifier.height(16.dp))

                // USERS
                NavigationRailItem(
                    selected = navigator.currentDestination.value == Destination.Users,
                    onClick = { navigator.navigateTo(Destination.Users) },
                    icon = {
                        Icon(
                            Icons.Outlined.People,
                            contentDescription = "Users",
                            tint = if (navigator.currentDestination.value == Destination.Users)
                                MaterialTheme.colorScheme.primary else Neutral
                        )
                    },
                    label = { Text("Users", style = MaterialTheme.typography.labelSmall) }
                )

                // ORGANIZATIONS
                NavigationRailItem(
                    selected = navigator.currentDestination.value == Destination.Organizations,
                    onClick = { navigator.navigateTo(Destination.Organizations) },
                    icon = {
                        Icon(
                            Icons.Outlined.Business,
                            contentDescription = "Organizations",
                            tint = if (navigator.currentDestination.value == Destination.Organizations)
                                MaterialTheme.colorScheme.primary else Neutral
                        )
                    },
                    label = { Text("Orgs", style = MaterialTheme.typography.labelSmall) }
                )

                // WORK
                NavigationRailItem(
                    selected = navigator.currentDestination.value == Destination.Work,
                    onClick = { navigator.navigateTo(Destination.Work) },
                    icon = {
                        Icon(
                            Icons.Outlined.Workspaces,
                            contentDescription = "Work",
                            tint = if (navigator.currentDestination.value == Destination.Work)
                                MaterialTheme.colorScheme.primary else Neutral
                        )
                    },
                    label = { Text("Work", style = MaterialTheme.typography.labelSmall) }
                )

                // REPORTS
                NavigationRailItem(
                    selected = navigator.currentDestination.value == Destination.Reports,
                    onClick = { navigator.navigateTo(Destination.Reports) },
                    icon = {
                        Icon(
                            Icons.Outlined.Assessment,
                            contentDescription = "Reports",
                            tint = if (navigator.currentDestination.value == Destination.Reports)
                                MaterialTheme.colorScheme.primary else Neutral
                        )
                    },
                    label = { Text("Reports", style = MaterialTheme.typography.labelSmall) }
                )


                ConnectivityStatusItem(
                    isNetworkAvailable,
                    isOnlineLoggedIn,
                    isOffLineLoggedIn,
                    {navigator.navigateTo(Destination.Login)}
                )
                Spacer(Modifier.weight(1f))

                // SETTINGS AT BOTTOM
                SettingsNavItem(sessionManager = sessionManager, navigator = navigator, offlineSessionManager = offlineSessionManager)
            }

            // ------- MAIN CONTENT AREA -------
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavGraph(navigator, authViewModel, sessionManager, usersViewModel)
            }
        }
    }
}






@Composable
fun ConnectivityStatusItem(
    isNetworkAvailable: Boolean,
    isOnlineLoggedIn: Boolean,
    isOfflineLoggedIn: Boolean,
    onClick: () -> Unit = {}
) {
    when {
        isNetworkAvailable && !isOnlineLoggedIn -> {
            NavigationRailItem(
                selected = false,
                onClick = onClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Login required",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                label = {
                    Text(
                        "Login",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }

        !isNetworkAvailable && !isOfflineLoggedIn -> {
            NavigationRailItem(
                selected = false,
                onClick = onClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.CloudOff,
                        contentDescription = "No connection",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                label = {
                    Text(
                        "No Conn",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }

        isNetworkAvailable && isOnlineLoggedIn -> {
            NavigationRailItem(
                selected = false,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.CloudDone,
                        contentDescription = "Connected",
                        tint = Color(0xFF2E7D32) // green
                    )
                },
                label = {
                    Text(
                        "Connected",
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }

        !isNetworkAvailable && isOfflineLoggedIn -> {
            NavigationRailItem(
                selected = false,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.CloudQueue,
                        contentDescription = "Offline mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = {
                    Text(
                        "Offline",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}