package org.montigny.client.navigation

import androidx.compose.runtime.Composable
import org.montigny.client.auth.SessionManager
import org.montigny.client.ui.organizations.OrganizationsScreen
import org.montigny.client.ui.reports.ReportsScreen
import org.montigny.client.ui.settings.MainSettingsScreen
import org.montigny.client.ui.settings.SettingsScreen
import org.montigny.client.ui.users.LoginScreen
import org.montigny.client.ui.users.UsersScreen
import org.montigny.client.ui.work.WorkScreen

@Composable
fun NavGraph(currentDestination: Destination ,sessionManager: SessionManager) {
    when (currentDestination) {
        Destination.Users -> UsersScreen()
        Destination.Organizations -> OrganizationsScreen()
        Destination.Work -> WorkScreen()
        Destination.Reports -> ReportsScreen()
        Destination.Settings -> SettingsScreen()
        Destination.Login -> LoginScreen(
            sessionManager = sessionManager)
        Destination.MainSettings -> MainSettingsScreen()
    }
}