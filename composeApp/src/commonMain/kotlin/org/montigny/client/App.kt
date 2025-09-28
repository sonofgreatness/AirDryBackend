package org.montigny.client

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.montigny.client.auth.SessionManager
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.NavGraph
import org.montigny.client.ui.settings.SettingsNavItem

@Composable
fun App(sessionManager: SessionManager) {
    MaterialTheme {
        var currentDestination by remember { mutableStateOf<Destination>(Destination.Users) }

        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Spacer(Modifier.height(12.dp))
                NavigationRailItem(
                    selected = currentDestination == Destination.Users,
                    onClick = { currentDestination = Destination.Users },
                    icon = { Icon(Icons.Default.People, contentDescription = "Users") }
                )
                NavigationRailItem(
                    selected = currentDestination == Destination.Organizations,
                    onClick = { currentDestination = Destination.Organizations },
                    icon = { Icon(Icons.Default.Business, contentDescription = "Organizations") }
                )
                /*NavigationRailItem(
                    selected = currentDestination == Destination.Login,
                    onClick = { currentDestination = Destination.Login },
                    icon = { Icon(Icons.Default.Login, contentDescription = "Login") }
                )*/
                NavigationRailItem(
                    selected = currentDestination == Destination.Work,
                    onClick = { currentDestination = Destination.Work },
                    icon = { Icon(Icons.Default.Work, contentDescription = "Work") }
                )

                NavigationRailItem(
                    selected = currentDestination == Destination.Reports,
                    onClick = { currentDestination = Destination.Reports },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") }
                )
                Spacer(Modifier.weight(1f))
                SettingsNavItem(sessionManager = sessionManager) { destination ->
                    currentDestination = destination
                }
            }
            // main content area -> delegated to NavGraph
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavGraph(currentDestination,sessionManager)
            }
        }
    }
}