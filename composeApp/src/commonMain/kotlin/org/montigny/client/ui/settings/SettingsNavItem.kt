package org.montigny.client.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
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
import org.montigny.client.auth.SessionManager
import org.montigny.client.navigation.Destination
import org.montigny.client.ui.common.StatusDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNavItem(sessionManager: SessionManager, onDestinationSelected: (Destination) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    NavigationRailItem(
        selected = false,
        onClick = { expanded = !expanded },
        icon = {
            Box {
                Icon(Icons.Default.Settings, contentDescription = "Settings")

                // Status dot at top right
                StatusDot(
                    isOnline = sessionManager.isLoggedIn,
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                )
            }
        }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        offset = DpOffset(x = 80.dp, y = 0.dp)
    ) {
        if (!sessionManager.isLoggedIn) {
            DropdownMenuItem(
                text = { Text("Login") },
                onClick = {
                    expanded = false
                    onDestinationSelected(Destination.Login)
                }
            )
        }
        DropdownMenuItem(
            text = { Text("Main Settings") },
            onClick = {
                expanded = false
                onDestinationSelected(Destination.MainSettings)
            }
        )
    }
}