package org.montigny.client

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.montigny.client.auth.SessionManager
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.NavGraph
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.settings.SettingsNavItem
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.slf4j.Logger

@Composable
fun App(sessionManager: SessionManager, authViewModel: AuthViewModel, usersViewModel: UserViewModel) {
    MaterialTheme {
        val logger: Logger  = koinInject()
        logger.info("Desktop app started ")

        val navigator = remember { Navigator() }

        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Spacer(Modifier.height(12.dp))
                NavigationRailItem(
                    selected = navigator.currentDestination.value == Destination.Users,
                    onClick = { navigator.navigateTo(Destination.Users) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Users") }
                )
                NavigationRailItem(
                    selected = navigator.currentDestination.value == Destination.Organizations,
                    onClick = {navigator.navigateTo(Destination.Organizations) },
                    icon = { Icon(Icons.Default.Business, contentDescription = "Organizations") }
                )
                /*NavigationRailItem(
                    selected = currentDestination == Destination.Login,
                    onClick = { currentDestination = Destination.Login },
                    icon = { Icon(Icons.Default.Login, contentDescription = "Login") }
                )*/
                NavigationRailItem(
                    selected = navigator.currentDestination.value == Destination.Work,
                    onClick = { navigator.navigateTo(Destination.Work) },
                    icon = { Icon(Icons.Default.Work, contentDescription = "Work") }
                )

                NavigationRailItem(
                    selected = navigator.currentDestination.value  == Destination.Reports,
                    onClick = { navigator.navigateTo(Destination.Reports) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") }
                )
                Spacer(Modifier.weight(1f))
                SettingsNavItem(sessionManager = sessionManager,navigator)
            }
            // main content area -> delegated to NavGraph
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavGraph(navigator, authViewModel,sessionManager,usersViewModel)
            }
        }
    }
}